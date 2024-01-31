package idelang.lexer

import language.lexer.Lexer
import language.lexer.*


class IdeLexer: Lexer {
    override fun tokenize(input: CharSequence): List<ConcreteToken<Token>> = Tokenizer(input, TOKENS_PARSER).tokenize()
}