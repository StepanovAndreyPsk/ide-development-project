package language.lexer


sealed class Either<A, B> {
    class Left<A, B>(val left: A) : Either<A, B>()
    class Right<A, B>(val right: B) : Either<A, B>()
}

data class Location(val startOffset: Int, val endOffset: Int = startOffset)

interface Token

data class ConcreteToken<T: Token>(val token: T, val location: Location)

data class TokenError(val offset: Int, val textOfError: String = "Unexpected token, offset: $offset"): Exception(textOfError)


class Tokenizer(val input: CharSequence, private val parsers: List<TokenParser<out Token>>) {
    var curIndex = 0

    fun skipWhitespaces() {
        while (!isEOF() && peek().isWhitespace()) {
            curIndex += 1
        }
    }

    fun tokenize(): List<ConcreteToken<Token>> {
        val tokens = mutableListOf<ConcreteToken<Token>>()

        while (!isEOF()) {
            skipWhitespaces()
            when(val result = recognizeToken()) {
                is Either.Left -> {
                    tokens.add(result.left)
                }
                is Either.Right -> {
                    throw Exception(result.right)
                }
            }
            skipWhitespaces()
        }
        return tokens
    }

    private fun recognizeToken(): Either<ConcreteToken<Token>, TokenError> {
        val startOffset = curIndex

        for (parser in parsers) {


            val result = parser.run { this@Tokenizer.parse() }

            if (result != null) {
                val endOffset = curIndex
                return Either.Left(ConcreteToken(result, Location(startOffset, endOffset)))
            } else {
                curIndex = startOffset
            }
        }



        return Either.Right(TokenError(curIndex))
    }

    fun takeWhile(f: (Char) -> Boolean): CharSequence {
        val stringBuilder = StringBuilder()

        while(!isEOF() && f(peek())) {
            stringBuilder.append(peek())
            curIndex += 1
        }

        return stringBuilder.toString()
    }

    fun isEOF() = curIndex >= input.length

    fun inc() { curIndex += 1 }


    fun peek(): Char {
        return input[curIndex]
    }

    fun hasNext(): Boolean = curIndex + 1 < input.length
}