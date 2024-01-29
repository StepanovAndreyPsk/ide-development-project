package language.structures

import language.ast.ANode
import language.ast.ASTNode
import language.ast.REPEAT_CONDITION
import language.lexer.ConcreteToken
import language.lexer.Location
import language.lexer.Token


fun <T : ANode> T.asParam(isParameter: Boolean = true): T {
    this.isParameter = isParameter
    return this
}

// extract rules and build ASTNode by parameters
interface Rule<T : ASTNode> {
    abstract val pattern: ANode
    abstract fun onMatch(location: Location, vararg args: Any): Result<T>

    fun onMatchWithParamResult(location: Location, vararg args: Any): Result<ANode.Terminal.ParamResult<T>> {
        return onMatch(location, *args).fold(
            onSuccess = { Result.success(ANode.Terminal.ParamResult(it)) },
            onFailure = { Result.failure(it) },
        )
    }
}


class ASTBuilder<T : ASTNode>(private val rule: Rule<T>, private val tokens: List<ConcreteToken<out Token>>) {
    private var curPos = 0

    fun build(): Result<T> {

        return processRule(rule).fold(
            onSuccess = {
                Result.success(it.value as T)
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    private fun processRule(rule: Rule<*>): Result<ANode.Terminal.ParamResult<*>> {
        val start = curPos
        val result = processNodes(rule.pattern)

        return result.fold(
            onSuccess = { nodes ->
                val args = nodes.filterIsInstance<ANode.Terminal.ParamResult<Any>>().map { it.value }
                val paramRes = rule.onMatchWithParamResult(Location(start, curPos), *args.toTypedArray())
                paramRes
            },
            onFailure = {
                curPos = start
                Result.failure(it)
            }
        )
    }


    private fun processNodes(node: ANode): Result<List<ANode>> {
        return when (node) {
            is ANode.Terminal -> {
                processTerminal(node)
            }

            is ANode.List -> {
                processList(node)
            }
        }
    }


    private fun processList(pattern: ANode.List): Result<List<ANode>> {
        return when (pattern) {
            is ANode.List.AndList -> {
                processAndList(pattern).fold(
                    onSuccess = {
                        if (!pattern.isParameter) {
                            Result.success(it.nodes)
                        } else {
                            Result.success(
                                listOf(it.asParam(pattern.isParameter).toParamResult())
                            )
                        }
                    },
                    onFailure = { Result.failure(it) })
            }

            is ANode.List.OrList -> {
                processOrList(pattern)
            }

            is ANode.List.Repeatable -> {
                processRepeatable(pattern)
            }
        }
    }

    private fun processOrList(orList: ANode.List.OrList): Result<List<ANode>> {
        if (orList.nodes.isEmpty()) {
            return Result.success(emptyList())
        }

        val start = curPos

        for (node in orList.nodes) {
            val result = processNodes(node)

            result.onSuccess {
                return Result.success(it)
            }.onFailure {
                curPos = start
            }
        }
        curPos = start
        return Result.failure(LexicalError(currentOffset(), ErrorType.UNEXPECTED_EXPRESSION))
    }

    private fun processAndList(pattern: ANode.List.AndList): Result<ANode.List.AndList> {
        val start = curPos
        val nodes: MutableList<ANode> = mutableListOf()
        for (node in pattern.nodes) {
            val result = processNodes(node)

            result.onFailure {
                curPos = start
                return Result.failure(it)
            }.onSuccess {
                nodes.addAll(it)
            }
        }
        return Result.success(
            ANode.List.AndList(
                nodes.filterIsInstance<ANode.Terminal.ParamResult<*>>().toMutableList(), Location(start, curPos)
            )
        )
    }

    private fun processRepeatable(pattern: ANode.List.Repeatable): Result<List<ANode>> {
        val start = curPos
        val separator = pattern.separator
        val condition = pattern.condition

        val nodes = mutableListOf<ANode>()

        while (!isEnd()) {
            val posBeforeNode = curPos
            val aNodes = processNodes(pattern.node)

            aNodes.onSuccess {
                nodes.addAll(it)
            }

            if (aNodes.isFailure) {
                curPos = posBeforeNode
                break
            }


            if (separator != null) {
                val posBeforeSeparator = curPos
                val separatorResult = processNodes(separator)

                if (separatorResult.isFailure) {
                    curPos = posBeforeSeparator
                    break
                }
            }
        }

        return when (condition) {
            REPEAT_CONDITION.ONE_AND_MORE -> {
                if (nodes.isEmpty()) {
                    curPos = start
                    Result.failure(
                        ExpectedExpression(pattern.node)
                    )
                } else {
                    Result.success(nodes)
                }
            }

            REPEAT_CONDITION.UNLIMITED -> {
                Result.success(nodes)
            }
        }
    }


    private fun peek(): ConcreteToken<out Token>? = tokens.getOrNull(curPos)

    private fun processTerminal(pattern: ANode.Terminal): Result<List<ANode>> {
        val start = curPos
        return when (pattern) {
            is ANode.Terminal.Rule<*> -> {
                processRule(pattern).fold(
                    onSuccess = {
                        Result.success(listOf(it))
                    },
                    onFailure = {
                        curPos = start
                        Result.failure(it)
                    },
                )
            }

            is ANode.Terminal.Token<*> -> {
                processToken(pattern).fold(
                    onSuccess = {
                        Result.success(listOf(it.asParam(pattern.isParameter).toParamResult()))
                    },
                    onFailure = {
                        curPos = start
                        Result.failure(it)
                    }
                )
            }

            ANode.Terminal.Eps -> {
                Result.success(listOf(ANode.Terminal.Eps.asParam(pattern.isParameter).toParamResult()))
            }

            is ANode.Terminal.Optional -> {
                processOptional(pattern)
            }

            is ANode.Terminal.ParamResult<*> -> {
                Result.success(listOf(pattern))
            }
        }
    }

    private fun processOptional(pattern: ANode): Result<List<ANode>> {
        val start = curPos

        val result = processNodes(pattern)
        return result.fold(
            onSuccess = { list ->
                Result.success(list)
            },
            onFailure = {
                curPos = start
                Result.success(emptyList())
            }
        )
    }

    private fun <T : ANode> T.toParamResult(): ANode {
        return if (this.isParameter) {
            ANode.Terminal.ParamResult(this)
        } else this
    }

    private fun <T : Token> processToken(expectedToken: ANode.Terminal.Token<T>): Result<ANode.Terminal.Token<T>> {
        if (!isEnd()) {
            val concreteToken = peek()
            if (concreteToken != null) {
                if (expectedToken.tokenType.isInstance(concreteToken.token)) {
                    curPos += 1
                    return Result.success(
                        ANode.Terminal.Token(
                            expectedToken.tokenType,
                            concreteToken as ConcreteToken<T>,
                            concreteToken.location
                        )
                    )
                }
            }
        }

        return Result.failure(ExpectedTokenError(expectedToken.tokenType, currentOffset()))
    }

    private fun currentOffset(): Int {
        return tokens.getOrNull(curPos)?.location?.startOffset ?: tokens.getOrNull(curPos - 1)?.location?.endOffset ?: 0
    }


    private fun isEnd() = curPos >= tokens.size
}