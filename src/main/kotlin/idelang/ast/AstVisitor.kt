package idelang.ast

interface AstVisitor<T, R> {
    fun visit(node: StmtList, context: T): R
    fun visit(node: Stmt.VarDeclaration, context: T): R
    fun visit(node: Stmt.Assignment, context: T): R
    fun visit(node: Stmt.IfStatement, context: T): R
    fun visit(node: Stmt.WhileStatement, context: T): R
    fun visit(node: Stmt.Block, context: T): R
    fun visit(node: Stmt.PrintStatement, context: T): R
    fun visit(node: Stmt.FuncDeclaration, context: T): R
    fun visit(node: Stmt.ReturnStatement, context: T): R
    fun visit(node: Stmt.ProcDeclaration, context: T): R
    fun visit(node: Stmt.ProcCall, context: T): R
    fun visit(node: ArgType.BooleanType, context: T): R
    fun visit(node: ArgType.StringType, context: T): R
    fun visit(node: ArgType.NumberType, context: T): R
    fun visit(node: FunParameter, context: T): R
    fun visit(node: Program, context: T): R
    fun visit(node: Factor, context: T): R
    fun visit(node: Arguments, context: T): R
    fun visit(node: Argument, context: T): R
    fun visit(node: FunParameters, context: T): R
    fun visit(node: Expr.SymbolName, context: T): R
    fun visit(node: Expr.NumberValue, context: T): R
    fun visit(node: Expr.StringLiteral, context: T): R
    fun visit(node: Expr.BooleanValue, context: T): R
    fun visit(node: Expr.FuncCall, context: T): R
    fun visit(node: Expr.UnaryOp, context: T): R
    fun visit(binaryOpExpr: Expr.BinaryOpExpr, context: T): R
    fun visit(node: Term, context: T): R
}