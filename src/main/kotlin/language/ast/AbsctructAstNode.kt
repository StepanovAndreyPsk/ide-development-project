package language.ast
import language.lexer.ConcreteToken
import language.lexer.Location
import language.lexer.Token
import kotlin.reflect.KClass

// Abstract Node
sealed class ANode(var location: Location? = null, var isParameter: Boolean = false) {
    sealed class List(location: Location? = null): ANode(location) {
        class OrList(val nodes: MutableList<ANode>, location: Location? = null) : List(location)

        class AndList(val nodes: MutableList<ANode>, location: Location? = null) : List(location)

        class Repeatable(val node: ANode, val condition: REPEAT_CONDITION = REPEAT_CONDITION.UNLIMITED, val separator: ANode? = null): List()
    }


    sealed class Terminal(location: Location? = null): ANode(location) {

        data class Token<T: language.lexer.Token>(val tokenType: KClass<out T>, var value: ConcreteToken<T>? = null, val loc: Location? = null) : Terminal(loc) {
            operator fun invoke() : Terminal.Token<T> {
                return copy()
            }
        }

        object Eps: Terminal()

        data class Optional(val node: ANode): Terminal()

        abstract class Rule<T: ASTNode>: Terminal(), language.structures.Rule<T>

        class ParamResult<T>(val value: T): Terminal()
    }

}

sealed class REPEAT_CONDITION {
    object UNLIMITED: REPEAT_CONDITION()
    object ONE_AND_MORE: REPEAT_CONDITION()
}

infix fun ANode.Terminal.or(node: ANode): ANode.List.OrList {
    return (ANode.List.OrList(mutableListOf(this)) or node)
}

infix fun ANode.Terminal.and(node: ANode) : ANode.List.AndList {
    return ANode.List.AndList(mutableListOf(this)) and node
}

infix fun ANode.List.OrList.or(node: ANode): ANode.List.OrList {
    this.nodes.add(node)
    return this
}

infix fun ANode.List.OrList.and(node: ANode): ANode.List.AndList {
    return ANode.List.AndList(mutableListOf(this, node))
}

infix fun ANode.List.AndList.and(node: ANode): ANode.List.AndList {
    this.nodes.add(node)
    return this
}

infix fun ANode.List.AndList.or(node: ANode): ANode.List.OrList {
    return ANode.List.OrList(mutableListOf(this, node))
}

infix fun ANode.List.Repeatable.and(node: ANode): ANode.List.AndList {
    return ANode.List.AndList(mutableListOf(this, node))
}

infix fun ANode.List.Repeatable.or(node: ANode): ANode.List.OrList {
    return ANode.List.OrList(mutableListOf(this, node))
}

fun <T: Token> KClass<T>.asANode(): ANode.Terminal.Token<T> {
    return ANode.Terminal.Token(this)
}


fun ANode.repeatable(condition: REPEAT_CONDITION = REPEAT_CONDITION.UNLIMITED) : ANode.List.Repeatable {
    return ANode.List.Repeatable(this, condition = condition)
}


fun ANode.List.Repeatable.separatedBy(separator: ANode): ANode.List.Repeatable {
    return ANode.List.Repeatable(this.node, this.condition, separator)
}


fun ANode.optional(): ANode.Terminal.Optional {
    return ANode.Terminal.Optional(this)
}