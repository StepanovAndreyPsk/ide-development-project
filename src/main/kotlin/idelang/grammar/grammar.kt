package idelang.grammar

import idelang.ast.*
import idelang.lexer.*
import language.ast.*
import language.structures.*
import language.lexer.Location


val AEps = ANode.Terminal.Eps
val AIdentifier = IdentifierToken::class.asANode()
val ANumber = NumberToken::class.asANode()
val AString = StringLiteralToken::class.asANode()
val ABoolean = BooleanToken::class.asANode()
val APlusOp = PlusOpToken::class.asANode()
val AMinusOp = MinusOpToken::class.asANode()
val AMulOp = MulOpToken::class.asANode()
val ADivOp = DivOpToken::class.asANode()
val AStringConcatOp = StringConcatOpToken::class.asANode()
val ANotOp = NotOpToken::class.asANode()
val AAndOp = AndOpToken::class.asANode()
val AOrOp = OrOpToken::class.asANode()
val ARelationOp = RelationOp::class.asANode()
val ARoundBracketOpen = RoundBracketOpenToken::class.asANode()
val ARoundBracketClose = RoundBracketCloseToken::class.asANode()
val ACurlyBracketOpen = CurlyBracketOpenToken::class.asANode()
val ACurlyBracketClose = CurlyBracketCloseToken::class.asANode()
val ASemicolon = SemicolonToken::class.asANode()
val AColon = ColonToken::class.asANode()
val AComma = CommaToken::class.asANode()
val AAssign = AssignToken::class.asANode()
val AVar = VarToken::class.asANode()
val AFuncKeyword = FuncKeywordToken::class.asANode()
val AProcKeyword = ProcKeywordToken::class.asANode()
val AIfKeyword = IfKeywordToken::class.asANode()
val AElseKeyword = ElseKeywordToken::class.asANode()
val AWhileKeyword = WhileKeywordToken::class.asANode()
val APrintKeyword = PrintKeywordToken::class.asANode()
val AReturnKeyword = ReturnKeywordToken::class.asANode()
val ANumberType = NumberTypeToken::class.asANode()
val AStringType = StringTypeToken::class.asANode()
val ABooleanType = BoolTypeToken::class.asANode()


fun <T> ErrorOnElse(location: Location) : Result<T> {
    throw UnexpectedExpressionError(location)
    return Result.failure<T>(UnexpectedExpressionError(location))
}

class AExpr(): ANode.Terminal.Rule<Expr>() {
    override val pattern: ANode
        get() = (
                (ATerm() and APlusOp().asParam() and AExpr()).asParam()
                        or (ATerm() and AOrOp().asParam() and AExpr()).asParam()
                        or (ATerm() and AStringConcatOp().asParam() and AExpr()).asParam()
                        or (ATerm() and AOrOp().asParam() and AExpr()).asParam()
                        or ATerm()
                        or (AExpr() and ARelationOp().asParam() and AExpr()).asParam()
                )

    override fun onMatch(location: Location, vararg args: Any): Result<Expr> {
        val param = args.toList()[0]

        return when (param) {
            is List.AndList -> {
                val nodes = param.nodes.filterIsInstance<ParamResult<*>>().map { it.value }
                val leftResult = nodes[0]
                val leftExpr = when(leftResult) {
                    is Expr -> {
                        leftResult
                    }
                    is Term -> {
                        leftResult.expr
                    }
                    else -> {
                        return ErrorOnElse(location)
                    }
                }
                val rightExp = nodes[2] as Expr
                val operation = nodes[1] as ANode.Terminal.Token<OpToken>
                Result.success(Expr.BinaryOpExpr(leftExpr, rightExp, operation, location))
            }
            is Term -> {
                Result.success(param.expr)
            }
            else -> {
                ErrorOnElse(location)
            }
        }
    }
}

class ATerm: ANode.Terminal.Rule<Term>() {
    override val pattern: ANode
        get() = ((AFactor() and AMulOp().asParam() and ATerm()).asParam()
                or (AFactor() and ADivOp().asParam() and ATerm()).asParam()
                or (AFactor() and AAndOp().asParam() and ATerm()).asParam()
                or AFactor())

    override fun onMatch(location: Location, vararg args: Any): Result<Term> {
        val param = args.toList()[0]
        return when (param) {
            is List.AndList -> {
                val nodes = param.nodes.filterIsInstance<ParamResult<*>>().map { it.value }
                val term = nodes[2] as Term
                val operation = nodes[1] as ANode.Terminal.Token<OpToken>
                val factor = nodes[0] as Factor
                Result.success(Term(Expr.BinaryOpExpr(term.expr, factor.expr, operation, term.location), location))
            }
            is Factor -> {
                Result.success(Term(param.expr, location))
            } else -> {
                ErrorOnElse(location)
            }
        }
    }
}


class AFactor: ANode.Terminal.Rule<Factor>() {
    override val pattern: ANode
        get() = ((ANotOp().asParam() and AFactor()).asParam()
                or (AMinusOp().asParam() and AFactor()).asParam()
                or ANumber().asParam() or AString().asParam()
                or ABoolean().asParam() or AIdentifier().asParam()
                or (ARoundBracketOpen().asParam() and AExpr() and ARoundBracketClose().asParam()).asParam()
                or AFuncCall()) // DONE

    override fun onMatch(location: Location, vararg args: Any): Result<Factor> {
        val param = args.toList()[0]
        return when (param) {
            is List.AndList -> {
                val nodes = param.nodes.filterIsInstance<ParamResult<*>>().map { it.value }
                return when(nodes.size) {
                    2 -> {
                        val operator = nodes[0] as ANode.Terminal.Token<OpToken>
                        val factor = nodes[1] as Factor
                        Result.success(Factor(Expr.UnaryOp(operator.value!!.token, factor.expr, operator.location!!), location))
                    }
                    3 -> {
                        val expr = nodes[1] as Expr
                        Result.success(Factor(expr, location))
                    }
                    else -> {
                        ErrorOnElse(location)
                    }
                }
            }
            is ANode.Terminal.Token<*> -> {
                val expr: Expr? = when (val token = param.value!!.token) {
                    is StringLiteralToken -> {
                        Expr.StringLiteral(token, location)
                    }
                    is BooleanToken -> {
                        Expr.BooleanValue(token, location)
                    }
                    is NumberToken -> {
                        Expr.NumberValue(token, location)
                    }
                    is IdentifierToken -> {
                        Expr.SymbolName(token, location)
                    }
                    else -> {
                        null
                    }
                }
                if (expr == null) {
                    return ErrorOnElse(location)
                } else {
                    return Result.success(Factor(expr, location))
                }
            } else -> {
                ErrorOnElse(location)
            }
        }
    }
}

class AArguments: ANode.Terminal.Rule<Arguments>() {
    override val pattern: ANode
        get() = AArgument() or AEps

    override fun onMatch(location: Location, vararg args: Any): Result<Arguments> {
        val params = args.toList()

        if (params.isEmpty()) {
            return Result.success(Arguments(null, location))
        }

        val argument = params[0] as Argument
        return Result.success(Arguments(argument, location))
    }
}

class AArgument: ANode.Terminal.Rule<Argument>() {
    override val pattern: ANode
        get() = AExpr().repeatable(REPEAT_CONDITION.ONE_AND_MORE).separatedBy(AComma())

    override fun onMatch(location: Location, vararg args: Any): Result<Argument> {
        return when(args[0]) {
            AEps -> {
                Result.failure(EpsError())
            } else -> {
                val params = args.toList()
                val exprs = params.map { (it as Expr) }
                Result.success(Argument(exprs, location))
            }
        }
    }
}

class AFuncCall: ANode.Terminal.Rule<Expr.FuncCall>() {
    override val pattern: ANode
        get() = AIdentifier().asParam() and ARoundBracketOpen() and AArguments() and ARoundBracketClose()

    override fun onMatch(location: Location, vararg args: Any): Result<Expr.FuncCall> {
        val params = args.toList()
        val id = params[0] as ANode.Terminal.Token<IdentifierToken>
        val arguments = params[1] as Arguments
        val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
        return Result.success(Expr.FuncCall(symbolName, arguments.arguments?.exprs ?: emptyList(), location))
    }
}

class AProcCall(): ANode.Terminal.Rule<Stmt.ProcCall>() {
    override val pattern: ANode
        get() = AIdentifier().asParam() and ARoundBracketOpen() and AArguments() and ARoundBracketClose() and ASemicolon()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.ProcCall> {
        val params = args.toList()
        val id = params[0] as ANode.Terminal.Token<IdentifierToken>
        val arguments = params[1] as Arguments
        val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
        return Result.success(
            Stmt.ProcCall(symbolName, arguments.arguments?.exprs ?: emptyList(), location)
        )
    }
}

class AType(): ANode.Terminal.Rule<ArgType>() {
    override val pattern: ANode
        get() = ANumberType().asParam() or ABooleanType().asParam() or AStringType().asParam()

    override fun onMatch(location: Location, vararg args: Any): Result<ArgType> {
        val token = args[0] as ANode.Terminal.Token<TypeToken>
        return when(val type = token.value!!.token) {
            is StringTypeToken -> {
                return Result.success(ArgType.StringType(location))
            }
            is NumberTypeToken -> {
                return Result.success(ArgType.NumberType(location))
            }
            is BoolTypeToken -> {
                return Result.success(ArgType.BooleanType(location))
            } else -> {
                ErrorOnElse(location)
            }
        }
    }
}

class AParameters: ANode.Terminal.Rule<FunParameters>() {
    override val pattern: ANode
        get() = (AIdentifier().asParam() and AColon() and AType()).asParam().repeatable(REPEAT_CONDITION.ONE_AND_MORE)
            .separatedBy(AComma()) or AEps.asParam()

    override fun onMatch(location: Location, vararg args: Any): Result<FunParameters> {
        when (args[0]) {
            AEps -> {
                return Result.success(FunParameters(emptyList(), location))
            }
            else -> {
                val parameters = args.toList().map { list ->
                    when (list) {
                        is List.AndList -> {
                            val nodes = list.nodes.filterIsInstance<ParamResult<*>>().map { it.value }
                            val id = nodes[0] as ANode.Terminal.Token<IdentifierToken>
                            val type = nodes[1] as ArgType
                            val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
                            FunParameter(symbolName, type, list.location!!)
                        }
                        else -> {
                            return ErrorOnElse(location)
                        }
                    }
                }
                return Result.success(FunParameters(parameters, location))
            }
        }
    }
}

class AReturnStatement: ANode.Terminal.Rule<Stmt.ReturnStatement>() {
    override val pattern: ANode
        get() = AReturnKeyword() and AExpr() and ASemicolon()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.ReturnStatement> {
        val expr = args[0] as Expr
        return Result.success(Stmt.ReturnStatement(expr, location))
    }
}

class AStatement: ANode.Terminal.Rule<Stmt>() {
    override val pattern: ANode
        get() = AVarDeclaration() or AAssignment() or AIfStatement() or AWhileStatement() or ABlock() or APrintStatement() or  AFuncDeclaration() or AReturnStatement() or AProcDeclaration() or AProcCall()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt> {
        return when(val value = args[0]) {
            is Stmt -> {
                return Result.success(value)
            } else -> {
                ErrorOnElse(location)
            }
        }

    }
}

class AVarDeclaration(): ANode.Terminal.Rule<Stmt.VarDeclaration>() {
    override val pattern: ANode
        get() = AVar() and AIdentifier().asParam() and AAssign() and AExpr() and ASemicolon()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.VarDeclaration> {
        val params = args.toList()
        return if (params.size != 2) {
            ErrorOnElse(location)
        } else {
            val id = params[0] as ANode.Terminal.Token<IdentifierToken>
            val expr = params[1] as Expr
            val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
            Result.success(Stmt.VarDeclaration(symbolName, expr, location))
        }
    }
}

class AStatementList: ANode.Terminal.Rule<StmtList>() {
    override val pattern: ANode
        get() = AStatement().repeatable(REPEAT_CONDITION.ONE_AND_MORE) or AEps

    override fun onMatch(location: Location, vararg args: Any): Result<StmtList> {
        val stmts = args.toList().mapNotNull { value ->
            when (value) {
                is Stmt -> {
                    value
                }
                AEps -> {
                    return@mapNotNull null
                }
                else -> {
                    return ErrorOnElse(location)
                }
            }
        }

        return Result.success(StmtList(stmts, location))
    }
}


class ABlock: ANode.Terminal.Rule<Stmt.Block>() {
    override val pattern: ANode
        get() = ACurlyBracketOpen() and AStatementList() and ACurlyBracketClose()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.Block> {
        val value = args[0]
        return when(value) {
            is StmtList -> {
                return Result.success(Stmt.Block(value.list, location))
            }
            else -> {
                ErrorOnElse(location)
            }
        }
    }
}

class AFuncDeclaration(): ANode.Terminal.Rule<Stmt.FuncDeclaration>() {
    override val pattern: ANode
        get() = AFuncKeyword() and AIdentifier().asParam() and ARoundBracketOpen() and AParameters() and ARoundBracketClose() and ABlock()


    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.FuncDeclaration> {
        val params = args.toList()
        return if (params.size == 3) {
            val id = params[0] as ANode.Terminal.Token<IdentifierToken>
            val parameters = params[1] as FunParameters
            val block = params[2] as Stmt.Block
            val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
            Result.success(Stmt.FuncDeclaration(symbolName, parameters.parameter, block, location))
        } else {
            ErrorOnElse(location)
        }
    }
}

class APrintStatement(): ANode.Terminal.Rule<Stmt.PrintStatement>() {
    override val pattern: ANode
        get() = APrintKeyword() and ARoundBracketOpen() and AExpr() and ARoundBracketClose() and ASemicolon()


    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.PrintStatement> {
        val value = args.toList()[0]
        return when(value) {
            is Expr -> {
                Result.success(Stmt.PrintStatement(value, location))
            } else -> {
                ErrorOnElse(location)
            }
        }
    }
}



class AWhileStatement(): ANode.Terminal.Rule<Stmt.WhileStatement>() {
    override val pattern: ANode
        get() = AWhileKeyword() and ARoundBracketOpen() and AExpr() and ARoundBracketClose() and ABlock()


    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.WhileStatement> {
        val params = args.toList()
        return when(params.size) {
            2 -> {
                val expr = params[0] as Expr
                val block = params[1] as Stmt.Block
                Result.success(Stmt.WhileStatement(expr, block, location))
            }
            else -> ErrorOnElse(location)
        }
    }
}

class AIfStatement(): ANode.Terminal.Rule<Stmt.IfStatement>() {
    override val pattern: ANode
        get() = AIfKeyword() and ARoundBracketOpen() and AExpr() and ARoundBracketClose() and ABlock() and (AElseKeyword() and ABlock()).asParam().optional() // DONE

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.IfStatement> {
        val params = args.toList()
        if (params.size < 2) {
            return ErrorOnElse(location)
        }
        val expr = params[0] as Expr
        val thenExpr = params[1] as Stmt.Block
        val elseExpr = params.getOrNull(2) as? Stmt.Block
        return Result.success(Stmt.IfStatement(expr, thenExpr, elseExpr, location))
    }
}


class AAssignment(): ANode.Terminal.Rule<Stmt.Assignment>() {
    override val pattern: ANode
        get() = AIdentifier().asParam() and AAssign() and AExpr() and ASemicolon()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.Assignment> {
        val params = args.toList()
        if (params.size != 2) {
            return ErrorOnElse(location)
        }

        val id = params[0] as ANode.Terminal.Token<IdentifierToken>
        val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
        val expr = params[1] as Expr

        return Result.success(Stmt.Assignment(symbolName, expr, location))
    }
}


class AProcDeclaration(): ANode.Terminal.Rule<Stmt.ProcDeclaration>() {
    override val pattern: ANode
        get() = AProcKeyword() and AIdentifier().asParam() and ARoundBracketOpen() and AParameters() and ARoundBracketClose() and ABlock()

    override fun onMatch(location: Location, vararg args: Any): Result<Stmt.ProcDeclaration> {
        val params = args.toList()
        if (params.size != 3) {
            return ErrorOnElse(location)
        }

        val id = params[0] as ANode.Terminal.Token<IdentifierToken>
        val symbolName = Expr.SymbolName(id.value!!.token, id.location!!)
        val parameters = params[1] as FunParameters
        val block = params[2] as Stmt.Block

        return Result.success(Stmt.ProcDeclaration(symbolName, parameters.parameter, block, location))
    }
}


class AProgram(): ANode.Terminal.Rule<Program>() {
    override val pattern: ANode
        get() = AStatementList()

    override fun onMatch(location: Location, vararg args: Any): Result<Program> {
        val params = args.toList()
        if (params.size != 1) {
            return ErrorOnElse(location)
        }

        val stmtList = params[0] as StmtList

        return Result.success(Program(stmtList, location))
    }
}

