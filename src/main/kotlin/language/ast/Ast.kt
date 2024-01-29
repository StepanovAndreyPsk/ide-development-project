package language.ast

import idelang.ast.AstVisitor
import language.lexer.Location

interface ASTNode {
    val location: Location
    fun <T: Any?, R> accept(visitor: AstVisitor<T, R>, context: T): R
}