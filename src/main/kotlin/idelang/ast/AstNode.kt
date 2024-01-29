package idelang.ast

import idelang.lexer.*
import language.ast.ASTNode
import language.ast.ANode
import language.lexer.Location


class StmtList(val list: List<Stmt>, override val location: Location): ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

sealed class Stmt: ASTNode {
    class VarDeclaration(
        val symbol: Expr.SymbolName,
        val expr: Expr,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }


    class Assignment(
        val symbol: Expr.SymbolName,
        val expr: Expr,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class IfStatement(
        val condition: Expr,
        val thenBlock: Block,
        val elseBlock: Block?,
        override val location: Location

    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class WhileStatement(
        val condition: Expr,
        val block: Block,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    data class Block(
        val statements: List<Stmt>,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class PrintStatement(
        val expr: Expr,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }


    class FuncDeclaration(
        val symbol: Expr.SymbolName,
        val parameters: List<FunParameter>,
        val block: Block,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class ReturnStatement(
        val expr: Expr,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class ProcDeclaration(
        val symbol: Expr.SymbolName,
        val parameters: List<FunParameter>,
        val block: Block,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class ProcCall(
        val symbol: Expr.SymbolName,
        val arguments: Arguments,
        override val location: Location
    ) : Stmt() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }
}
sealed class ArgType: ASTNode {
    class BooleanType(override val location: Location): ArgType() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class StringType(override val location: Location): ArgType() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class NumberType(override val location: Location): ArgType() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }
}


data class FunParameter(
    val symbol: Expr.SymbolName,
    val type: ArgType,
    override val location: Location
) : ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

data class Program(
    val statementList: StmtList,
    override val location: Location
) : ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}


class Term(val expr: Expr, override val location: Location): ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

class Factor(val expr: Expr, override val location: Location) : ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

class Arguments(val arguments: List<Argument>, override val location: Location): ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

class Argument(val expr: Expr, override val location: Location): ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

class FunParameters(val parameter: List<FunParameter>, override val location: Location): ASTNode {
    override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
        return visitor.visit(this, context)
    }
}

sealed class Expr: ASTNode {
    class SymbolName(
        val identifier: IdentifierToken,
        override val location: Location
    ) : Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class BinaryOpExpr(
        val leftExpr: Expr,
        val rightExpr: Expr,
        val opToken:  ANode.Terminal.Token<OpToken>,
        override val location: Location
    ): Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class UnaryOp(
        val operation: OpToken, val expr: Expr,
        override val location: Location
    ): Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class FuncCall(
        val symbolName: SymbolName,
        val arguments: Arguments,
        override val location: Location
    ) : Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }


    class BooleanValue(
        val value: BooleanToken,
        override val location: Location
    ) : Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }


    class StringLiteral(
        val value: StringLiteralToken,
        override val location: Location
    ) : Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }

    class NumberValue(
        val value: NumberToken,
        override val location: Location
    ) : Expr() {
        override fun <T, R> accept(visitor: AstVisitor<T, R>, context: T) : R {
            return visitor.visit(this, context)
        }
    }
}