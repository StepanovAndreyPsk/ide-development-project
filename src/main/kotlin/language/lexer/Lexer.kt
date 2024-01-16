package language.lexer

interface Lexer {
    fun tokenize(input: CharSequence): List<ConcreteToken<Token>>
}