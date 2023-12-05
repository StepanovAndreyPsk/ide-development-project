package util.rope

import java.util.ArrayDeque
import java.util.ArrayList

internal const val splitLength = 199

private val FIBONACCI_SEQUENCE: LongArray = buildList {
    var a = 0L
    var b = 1L
    repeat(10000) {
        val tmp = a + b
        a = b
        b = tmp
        add(b)
    }
}.toLongArray()

internal fun <Metrics> concat(left: RopeNode<Metrics>, right: RopeNode<Metrics>): RopeNode<Metrics> {
    if (left.length == 0) {
        return right
    }
    if (right.length == 0) {
        return left
    }
    if (left !is ConcatNode) {
        if (right is ConcatNode) {
            val rightChild = right.left
            if (left.length + rightChild.length < splitLength) {
                val stringBuilder = StringBuilder()
                left.joinToString(stringBuilder)
                rightChild.joinToString(stringBuilder)
                return rebalance(
                    ConcatNode(
                        LeafNode(stringBuilder.toString(), right.metricsCalculator),
                        right.right,
                        right.metricsCalculator
                    )
                )
            }
        }
    }
    if (right !is ConcatNode) {
        if (left is ConcatNode) {
            val leftChild = left.right
            if (right.length + leftChild.length < splitLength) {
                val stringBuilder = StringBuilder()
                leftChild.joinToString(stringBuilder)
                right.joinToString(stringBuilder)
                return rebalance(
                    ConcatNode(
                        left.left,
                        LeafNode(stringBuilder.toString(), left.metricsCalculator),
                        left.metricsCalculator
                    )
                )
            }

        }
    }
    return rebalance(ConcatNode(left, right, left.metricsCalculator))
}

private fun <Metrics> isBalanced(rope: RopeNode<Metrics>): Boolean {
    val depth = rope.depth()
    return (depth + 2 < FIBONACCI_SEQUENCE.size && FIBONACCI_SEQUENCE[depth + 2] <= rope.length)
}

internal fun <Metrics> rebalance(node: RopeNode<Metrics>): RopeNode<Metrics> {
    return if (!isBalanced(node)) {
        val leaves = collectLeaves(node)
        merge(leaves, 0, leaves.size)
    } else node
}

internal fun <Metrics> merge(
    leaves: List<RopeNode<Metrics>>,
    start: Int = 0,
    end: Int = leaves.size
): RopeNode<Metrics> {
    val range = end - start
    require(range != 0) { "It is not possible to merge 0 leaves" }

    if (range == 1) {
        return leaves[start]
    }
    if (range == 2) {
        return ConcatNode(leaves[start], leaves[start + 1], leaves[start].metricsCalculator)
    }
    val mid = start + range / 2
    return ConcatNode(
        merge(leaves, start, mid),
        merge(leaves, mid, end),
        leaves[0].metricsCalculator
    )
}

internal fun <Metrics> collectLeaves(ropeNode: RopeNode<Metrics>): List<RopeNode<Metrics>> {
    val stack = ArrayDeque<RopeNode<Metrics>>()
    val result = ArrayList<RopeNode<Metrics>>(100)

    var leftmostNode: RopeNode<Metrics>? = ropeNode
    while (leftmostNode != null) {
        stack.push(leftmostNode)
        leftmostNode = leftmostNode.left
    }

    while (!stack.isEmpty()) {
        val currentNode = stack.pop()
        if (currentNode is LeafNode) {
            result.add(currentNode)
        }
        var leftmost = currentNode.right
        while (leftmost != null) {
            stack.push(leftmost)
            leftmost = leftmost.left
        }
    }
    return result
}

internal val <Metrics> RopeNode<Metrics>.left: RopeNode<Metrics>?
    get() = (this as? ConcatNode)?.left

internal val <Metrics> RopeNode<Metrics>.right: RopeNode<Metrics>?
    get() = (this as? ConcatNode)?.right
