package idelang.highlighting

import idelang.ast.Program
import idelang.grammar.AProgram
import idelang.lexer.IdeLexer
import idelang.IdeLang
import language.ast.ASTNode
import language.structures.ASTBuilder
import language.lexer.ConcreteToken
import language.lexer.Token

object IdeHighlightingBuilder: HighlightingBuilder<Program>(IdeLang) {
    override fun buildHighlighting(ast: ASTNode, tokens: List<ConcreteToken<out Token>>): List<HToken> {
        val curAst = ast as? Program ?: return emptyList()
        return HighlighterVisitor(tokens, curAst).generateHighlighing()
    }

    override fun tokenize(input: CharSequence): List<ConcreteToken<Token>> {
        return IdeLexer().tokenize(input)
    }

    override fun buildAst(tokens: List<ConcreteToken<Token>>): Result<Program> {
        return ASTBuilder(AProgram(), tokens).build()
    }
}