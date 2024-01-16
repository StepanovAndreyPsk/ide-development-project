package idelang.lexer

import language.lexer.Token
import language.lexer.TokenParser
import language.lexer.Tokenizer


class SymbolParser(private val tokens: List<SymbolToken>): TokenParser<SymbolToken> {
    override fun Tokenizer.parse(): SymbolToken? {
        for (token in tokens) {
            val start = this.curIndex

            val text = StringBuilder()
            var i = 0

            while (!isEOF() && i < token.symbol.length && peek() == token.symbol[i]) {
                text.append(peek())
                i += 1
                inc()
            }

            if (token.symbol == text.toString()) {
                return token
            } else {
                curIndex = start
            }
        }

        return null
    }
}

open class RegexpTokenParser<T: Token>(private val matcher: RegexpTokenMatcher<T>): TokenParser<T> {
    override fun Tokenizer.parse(): T? {
        val regex = Regex(matcher.pattern)

        val match = regex.matchAt(input, curIndex)

        if (match != null && match.range.start == curIndex) {
            curIndex = match.range.endInclusive
            inc()
            return matcher.onMatch(match.value)
        }
        return null
    }
}


class StringLiteralParser: TokenParser<StringLiteralToken> {
    override fun Tokenizer.parse(): StringLiteralToken? {
        if (!isEOF() && peek() == '\"') {
            inc()

            val text = takeWhile { it != '\"' }
            if (!isEOF() && peek() == '\"') {
                inc()
                return StringLiteralToken(text.toString())
            }
        }
        return null
    }
}