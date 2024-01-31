package idelang.parser

import idelang.ast.Program
import idelang.grammar.AProgram
import idelang.lexer.IdeLexer
import language.structures.ASTBuilder


fun buildAst(input: CharSequence) : Result<Program> {
    val tokens = IdeLexer().tokenize(input)
    println(tokens)
    return ASTBuilder(AProgram(), tokens).build()
}


fun main() {
    val myProgramm =
        """  
        func add(a: number, b: number)
        {
           return a + b;
        }
        func main() {
        }
        """

    buildAst(myProgramm).onSuccess {
        println(it)
    }.onFailure {
        println(it)
    }
}