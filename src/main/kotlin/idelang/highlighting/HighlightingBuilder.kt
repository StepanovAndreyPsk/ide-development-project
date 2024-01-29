package idelang.highlighting

import language.Language
import language.ast.ASTNode
import language.lexer.ConcreteToken
import language.lexer.Token


abstract class HighlightingBuilder<T: ASTNode>(val language: Language) {
    abstract fun tokenize(input: CharSequence): List<ConcreteToken<Token>>
    abstract fun buildAst(tokens: List<ConcreteToken<Token>>): Result<T>
    abstract fun buildHighlighting(ast: ASTNode, tokens: List<ConcreteToken<out Token>>): List<HToken>
}

val HighlightingBuilders = listOf<HighlightingBuilder<out ASTNode>>(AScriptHighlightingBuilder)