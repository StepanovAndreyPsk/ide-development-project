package idelang.highlighting

import idelang.ast.*
import language.lexer.ConcreteToken
import language.lexer.Token

sealed class HighlightElement {
    sealed class Keyword: HighlightElement()  {
        object Var: Keyword()
        object If: Keyword()
        object While: Keyword()
        object Print: Keyword()
        object Func: Keyword()
        object Return: Keyword()
        object Proc: Keyword()
    }

    sealed class Type: HighlightElement() {
        object BoolType: Type()
        object StringType: Type()
        object NumberType: Type()
    }

    class SymbolName(val name: String): HighlightElement()

    object Operator: HighlightElement()

    sealed class Value: HighlightElement() {
        object BooleanValue: Value()
        object NumberValue: Value()
        object StringLiteral: Value()
    }

    sealed class Markup: HighlightElement() {
        object Bracket: Markup()
    }
}



data class HToken(val concreteToken: ConcreteToken<out Token>, val element: HighlightElement)


class HighlighterVisitor(val tokens: List<ConcreteToken<Token>>): AstVisitor<Any, MutableList<HToken>> {
    override fun visit(node: StmtList, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()

        node.list.forEach {
            elements.addAll(it.accept(this, context))
        }

        return elements
    }

    override fun visit(node: Stmt.VarDeclaration, context: Any): MutableList<HToken> {
        val elements : MutableList<HToken> = mutableListOf()

        val varToken = tokens[node.location.startOffset]

        elements.add(HToken(varToken, HighlightElement.Keyword.Var))
        elements.addAll(node.symbol.accept(this, context))
        elements.addAll(node.expr.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.Assignment, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.symbol.accept(this, context))
        elements.addAll(node.expr.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.IfStatement, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val ifToken = tokens[node.location.startOffset]
        elements.add(HToken(ifToken, HighlightElement.Keyword.If))
        elements.addAll(node.condition.accept(this, context))
        elements.addAll(node.thenBlock.accept(this, context))
        node.elseBlock?.let { elements.addAll(it.accept(this, context))}
        return elements
    }

    override fun visit(node: Stmt.WhileStatement, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val whileToken = tokens[node.location.startOffset]
        elements.add(HToken(whileToken, HighlightElement.Keyword.While))
        elements.addAll(node.condition.accept(this, context))
        elements.addAll(node.block.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.Block, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val bracketTokenOpen = tokens[node.location.startOffset]
        elements.add(HToken(bracketTokenOpen, HighlightElement.Markup.Bracket))
        for (statement in node.statements) {
            elements.addAll(statement.accept(this, context))
        }
        val bracketTokenClose = tokens[node.location.endOffset]
        elements.add(HToken(bracketTokenClose, HighlightElement.Markup.Bracket))
        return elements
    }

    override fun visit(node: Stmt.PrintStatement, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val printToken = tokens[node.location.startOffset]
        elements.add(HToken(printToken, HighlightElement.Keyword.Print))
        elements.addAll(node.expr.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.FuncDeclaration, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val funcToken = tokens[node.location.startOffset]
        elements.add(HToken(funcToken, HighlightElement.Keyword.Func))
        elements.addAll(node.symbol.accept(this, context))

        for (parameter in node.parameters) {
            elements.addAll(parameter.accept(this, context))
        }
        elements.addAll(node.block.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.ReturnStatement, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val returnToken = tokens[node.location.startOffset]
        elements.add(HToken(returnToken, HighlightElement.Keyword.Return))
        elements.addAll(node.expr.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.ProcDeclaration, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val procToken = tokens[node.location.startOffset]
        elements.add(HToken(procToken, HighlightElement.Keyword.Proc))
        elements.addAll(node.symbol.accept(this, context))

        for (parameter in node.parameters) {
            elements.addAll(parameter.accept(this, context))
        }

        elements.addAll(node.block.accept(this, context))
        return elements
    }

    override fun visit(node: Stmt.ProcCall, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.symbol.accept(this, context))

        for (arg in node.arguments) {
            elements.addAll(arg.accept(this, context))
        }

        return elements
    }

    override fun visit(node: ArgType.BooleanType, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val boolToken = tokens[node.location.startOffset]
        elements.add(HToken(boolToken, HighlightElement.Type.BoolType))
        return elements
    }

    override fun visit(node: ArgType.StringType, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val stringToken = tokens[node.location.startOffset]
        elements.add(HToken(stringToken, HighlightElement.Type.StringType))
        return elements
    }

    override fun visit(node: ArgType.NumberType, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val numberToken = tokens[node.location.startOffset]
        elements.add(HToken(numberToken, HighlightElement.Type.NumberType))
        return elements
    }

    override fun visit(node: FunParameter, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.symbol.accept(this, context))
        elements.addAll(node.type.accept(this, context))
        return elements
    }

    override fun visit(node: Program, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()

        node.statementList.list.forEach {
            elements.addAll(it.accept(this, context))
        }

        return elements
    }

    override fun visit(node: Factor, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.accept(this, context))
        return elements
    }

    override fun visit(node: Arguments, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()

        node.arguments?.exprs?.forEach {
            elements.addAll(it.accept(this, context))
        }

        return elements
    }

    override fun visit(node: Argument, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        node.exprs.forEach {
            elements.addAll(it.accept(this, context))
        }
        return elements
    }

    override fun visit(node: FunParameters, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        node.parameter.forEach {
            elements.addAll(it.accept(this, context))
        }
        return elements
    }

    override fun visit(node: Expr.SymbolName, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val symbolToken = tokens[node.location.startOffset]
        elements.add(HToken(symbolToken, HighlightElement.SymbolName(node.identifier.value)))
        return elements
    }

    override fun visit(node: Expr.BinaryOpExpr, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.leftExpr.accept(this, context))
        val operatorToken = node.opToken.value!!
        elements.add(HToken(operatorToken, HighlightElement.Operator))
        elements.addAll(node.rightExpr.accept(this, context))
        return elements
    }

    override fun visit(node: Expr.UnaryOp, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.add(HToken(tokens[node.location.startOffset], HighlightElement.Operator))
        elements.addAll(node.expr.accept(this, context))
        return elements
    }

    override fun visit(node: Expr.FuncCall, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.symbolName.accept(this, context))

        node.arguments.forEach {
            elements.addAll(it.accept(this, context))
        }
        return elements
    }

    override fun visit(node: Expr.BooleanValue, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val booleanToken = tokens[node.location.startOffset]
        elements.add(HToken(booleanToken, HighlightElement.Value.BooleanValue))
        return elements
    }

    override fun visit(node: Expr.StringLiteral, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val stringToken = tokens[node.location.startOffset]
        elements.add(HToken(stringToken, HighlightElement.Value.StringLiteral))
        return elements
    }

    override fun visit(node: Expr.NumberValue, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        val numberToken = tokens[node.location.startOffset]
        elements.add(HToken(numberToken, HighlightElement.Value.NumberValue))
        return elements
    }

    override fun visit(node: Term, context: Any): MutableList<HToken> {
        val elements: MutableList<HToken> = mutableListOf()
        elements.addAll(node.expr.accept(this, context))
        return elements
    }
}