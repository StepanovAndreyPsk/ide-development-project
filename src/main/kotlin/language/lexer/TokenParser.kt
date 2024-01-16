package language.lexer

interface TokenParser<T: Token> {
    fun Tokenizer.parse(): T?
}