package idelang.highlighting

import idelang.ast.Program
import idelang.grammar.AProgram
import idelang.lexer.AScriptLexer
import idelang.IdeLang
import language.ast.ASTNode
import language.structures.ASTBuilder
import language.lexer.ConcreteToken
import language.lexer.Token

object AScriptHighlightingBuilder: HighlightingBuilder<Program>(IdeLang) {
    override fun buildHighlighting(ast: ASTNode, tokens: List<ConcreteToken<out Token>>): List<HToken> {
        val curAst = ast as? Program ?: return emptyList()
        return HighlighterVisitor(tokens, curAst).generateHighlighing()
    }

    override fun tokenize(input: CharSequence): List<ConcreteToken<Token>> {
        return AScriptLexer().tokenize(input)
    }

    override fun buildAst(tokens: List<ConcreteToken<Token>>): Result<Program> {
        return ASTBuilder(AProgram(), tokens).build()
    }
}