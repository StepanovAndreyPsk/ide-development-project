package idelang.lexer

import language.lexer.Token
import language.lexer.TokenParser


abstract class SymbolToken(val symbol: String) : Token


interface RoundBracketToken
object RoundBracketOpenToken : SymbolToken("("), RoundBracketToken
object RoundBracketCloseToken : SymbolToken(")"), RoundBracketToken

interface CurlyBracketToken
object CurlyBracketOpenToken : SymbolToken("{"), CurlyBracketToken
object CurlyBracketCloseToken : SymbolToken("}"), CurlyBracketToken

object SemicolonToken : SymbolToken(";")
object ColonToken : SymbolToken(":")
object CommaToken : SymbolToken(",")
object AssignToken : SymbolToken("=")


val MARKUP_TOKENS = listOf(
    RoundBracketOpenToken,
    RoundBracketCloseToken,
    CurlyBracketOpenToken,
    CurlyBracketCloseToken,
    SemicolonToken,
    ColonToken,
    CommaToken,
    AssignToken
)


class BooleanToken(val value: Boolean): Token

object BooleanTokenMatcher: RegexpTokenMatcher<BooleanToken>("(true|false)") {
    override fun onMatch(matchedValue: String) : BooleanToken {
        return BooleanToken(matchedValue == "true")
    }
}


abstract class OpToken(symbol: String, val maybeUnary: Boolean, val maybeBinary: Boolean) : SymbolToken(symbol)

// Concatenation of Strings
object StringConcatOpToken : OpToken("%", false, true)

// Operations on numbers
interface OnNumbers: Token

// Operations return number
interface RetNumbers: Token

// result of operators is boolean
interface RetBoolean: Token

// Operations on booleans
interface OnBooleans: Token


interface ArithmeticOp: Token
object PlusOpToken : OpToken("+", false, true), OnNumbers, RetNumbers, ArithmeticOp
object MinusOpToken : OpToken("-", true, true), OnNumbers, RetNumbers, ArithmeticOp
object MulOpToken : OpToken("*", false, true), OnNumbers, RetNumbers, ArithmeticOp
object DivOpToken : OpToken("/", false, true), OnNumbers, RetNumbers, ArithmeticOp


interface LogicalOp : Token
object NotOpToken : OpToken("!", true, false), OnBooleans, RetBoolean, LogicalOp
object OrOpToken : OpToken("||", false, true), OnBooleans, RetBoolean, LogicalOp
object AndOpToken : OpToken("&&", false, true), OnBooleans, RetBoolean, LogicalOp


// Operations on objects
interface OnObjects : Token

interface RelationOp: Token
interface EqualityOp : Token
object EqOpToken : OpToken("==", false, true), OnObjects, RetBoolean, EqualityOp, RelationOp
object NotEqOpToken : OpToken("!=", false, true), OnObjects, RetBoolean, EqualityOp, RelationOp


interface CompOperatorOp : Token
object LessOpToken : OpToken("<", false, true), OnNumbers, RetBoolean, CompOperatorOp, RelationOp
object LessOrEqualOpToken : OpToken("<=", false, true), OnNumbers, RetBoolean, CompOperatorOp, RelationOp
object GreaterOpToken : OpToken(">", false, true), OnNumbers, RetBoolean, CompOperatorOp, RelationOp
object GreaterOrEqualOpToken : OpToken(">=", false, true), OnNumbers, RetBoolean, CompOperatorOp, RelationOp


val ONE_CHAR_OP_TOKENS = listOf(
    StringConcatOpToken,
    PlusOpToken,
    MinusOpToken,
    MulOpToken,
    DivOpToken,
    NotOpToken,
    LessOpToken,
    GreaterOpToken,
)

val TWO_CHAR_OP_TOKENS: List<OpToken> = listOf(
    OrOpToken,
    AndOpToken,
    EqOpToken,
    NotEqOpToken,
    LessOrEqualOpToken,
    GreaterOrEqualOpToken,
)


abstract class KeywordToken(symbol: String): SymbolToken(symbol)

object IfKeywordToken: KeywordToken("if")
object ElseKeywordToken: KeywordToken("else")
object VarToken: KeywordToken("var")
object WhileKeywordToken: KeywordToken("while")
object FuncKeywordToken: KeywordToken("func")
object ReturnKeywordToken: KeywordToken("return")
object ProcKeywordToken: KeywordToken("proc")
object PrintKeywordToken: KeywordToken("print")


val KEYWORD_TOKENS = listOf(
    IfKeywordToken,
    ElseKeywordToken,
    VarToken,
    WhileKeywordToken,
    FuncKeywordToken,
    ReturnKeywordToken,
    ProcKeywordToken,
    PrintKeywordToken
)


abstract class TypeToken(symbol: String) : SymbolToken(symbol)

object NumberTypeToken : TypeToken("number")
object StringTypeToken : TypeToken("string")
object BoolTypeToken : TypeToken("bool")

val TYPE_TOKENS = listOf(
    NumberTypeToken,
    StringTypeToken,
    BoolTypeToken,
)


abstract class RegexpTokenMatcher<T>(val pattern: String): Token {
    abstract fun onMatch(matchedValue: String): T
}

class IdentifierToken(val value: String): Token

object IdentifierTokenMatcher: RegexpTokenMatcher<IdentifierToken>("[a-zA-Z_][a-zA-Z0-9_]*") {
    override fun onMatch(matchedValue: String) : IdentifierToken {
        return IdentifierToken(matchedValue)
    }
}

class NumberToken(val value: Int): Token

object NumberTokenMatcher: RegexpTokenMatcher<NumberToken>("(0|[1-9][0-9_]*)") {
    override fun onMatch(matchedValue: String): NumberToken {
        return NumberToken(matchedValue.toInt())
    }
}


data class StringLiteralToken(val value: String): Token



internal val TOKENS_PARSER: List<TokenParser<out Token>> = listOf(
    StringLiteralParser(),
    SymbolParser(KEYWORD_TOKENS),
    SymbolParser(TYPE_TOKENS),
    RegexpTokenParser(BooleanTokenMatcher),
    SymbolParser(TWO_CHAR_OP_TOKENS),
    SymbolParser(ONE_CHAR_OP_TOKENS),
    SymbolParser(MARKUP_TOKENS),
    RegexpTokenParser(NumberTokenMatcher),
    RegexpTokenParser(IdentifierTokenMatcher),
)




