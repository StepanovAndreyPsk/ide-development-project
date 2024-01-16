package idelang.ast

import idelang.lexer.*
import language.ast.ANode
import language.structures.ASTNode
import language.lexer.Location


data class StmtList(val list: List<Stmt>, override val location: Location): ASTNode

sealed class Stmt: ASTNode {
    class VarDeclaration(
        val symbol: Expr.SymbolName,
        val expr: Expr,
        override val location: Location
    ) : Stmt()


    class Assignment(
        val symbol: Expr.SymbolName,
        val expr: Expr,
        override val location: Location
    ) : Stmt()

    class IfStatement(
        val condition: Expr,
        val thenBlock: Block,
        val elseBlock: Block?,
        override val location: Location

    ) : Stmt()

    class WhileStatement(
        val condition: Expr,
        val block: Block,
        override val location: Location
    ) : Stmt()

    data class Block(
        val statements: List<Stmt>,
        override val location: Location
    ) : Stmt()

    class PrintStatement(
        val expr: Expr,
        override val location: Location
    ) : Stmt()



    class FuncDeclaration(
        val symbol: Expr.SymbolName,
        val parameters: List<FunParameter>,
        val block: Block,
        override val location: Location
    ) : Stmt()

    class ReturnStatement(
        val expr: Expr,
        override val location: Location
    ) : Stmt()

    class ProcDeclaration(
        val symbol: Expr.SymbolName,
        val parameters: List<FunParameter>,
        val block: Block,
        override val location: Location
    ) : Stmt()

    class ProcCall(
        val symbol: Expr.SymbolName,
        val arguments: List<Expr>,
        override val location: Location
    ) : Stmt()
}

sealed class ArgType: ASTNode {
    class BooleanType(override val location: Location): ArgType()

    class StringType(override val location: Location): ArgType()

    class NumberType(override val location: Location): ArgType()
}


data class FunParameter(
    val symbol: Expr.SymbolName,
    val type: ArgType,
    override val location: Location
) : ASTNode

data class Program(
    val statementList: StmtList,
    override val location: Location
) : ASTNode



class Term(val expr: Expr, override val location: Location): ASTNode

class Factor(val expr: Expr, override val location: Location) : ASTNode

class Arguments(val arguments: Argument?, override val location: Location): ASTNode

class Argument(val exprs: List<Expr>, override val location: Location): ASTNode

class FunParameters(val parameter: List<FunParameter>, override val location: Location): ASTNode

sealed class Expr: ASTNode {
    class SymbolName(
        val identifier: IdentifierToken,
        override val location: Location
    ) : Expr()

    class BinaryOpExpr(
        val leftExpr: Expr,
        val rightExpr: Expr,
        val opToken:  ANode.Terminal.Token<OpToken>,
        override val location: Location
    ): Expr()

    class UnaryOp(
        val operation: OpToken, val expr: Expr,
        override val location: Location
    ): Expr()

    class FuncCall(
        val symbolName: SymbolName,
        val arguments: List<Expr>,
        override val location: Location
    ) : Expr()


    class BooleanValue(
        val value: BooleanToken,
        override val location: Location
    ) : Expr()


    class StringLiteral(
        val value: StringLiteralToken,
        override val location: Location
    ) : Expr()

    class NumberValue(
        val value: NumberToken,
        override val location: Location
    ) : Expr()
}