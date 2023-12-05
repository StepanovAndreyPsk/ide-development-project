package util.rope

import kotlin.text.StringBuilder
import kotlin.math.max


internal class ConcatNode<Metrics>(val left: RopeNode<Metrics>,
                                   val right: RopeNode<Metrics>,
                                   override val metricsCalculator: MetricsCalculator<Metrics>) : RopeNode<Metrics> {
    override fun depth(): Int = max(left.depth(), right.depth()) + 1

    override val length: Int by lazy { left.length + right.length }

    override val metrics: Metrics by lazy { metricsCalculator.joinMetrics(left.metrics, right.metrics) }

    override fun delete(start: Int, length: Int): RopeNode<Metrics> {
        val (left, _) = split(start)
        val (_, right) = split(start + length)
        return rebalance(ConcatNode(left, right, metricsCalculator))
    }

    override fun split(idx: Int): Pair<RopeNode<Metrics>, RopeNode<Metrics>> {
        return if (idx < left.length) {
            val (splitLeft, splitRight) = left.split(idx)
            rebalance(splitLeft) to rebalance(ConcatNode(splitRight, right, metricsCalculator))
        } else if (idx > left.length) {
            val (splitLeft, splitRight) = right.split(idx - left.length)
            rebalance(ConcatNode(left, splitLeft, metricsCalculator)) to rebalance(splitRight)
        } else {
            left to right
        }
    }

    override fun get(index: Int): Char {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("Index out of range: $index.  Max range: $length")
        }
        return if (index < left.length) left[index] else right[index - left.length]
    }

    override fun slice(startIndex: Int, endIndex: Int): RopeNode<Metrics> {
        if (startIndex == 0 && endIndex == length) {
            return this
        }
        val leftLength = left.length
        if (endIndex <= leftLength) {
            return left.slice(startIndex, endIndex)
        }
        if (startIndex >= leftLength) {
            return right.slice(startIndex - leftLength, endIndex - leftLength)
        }
        val leftSequence = left.slice(startIndex, leftLength)
        val rightSequence = right.slice(0, endIndex - leftLength)
        return concat(leftSequence, rightSequence)
    }

    override fun joinToString(stringBuilder: StringBuilder): StringBuilder {
        left.joinToString(stringBuilder)
        right.joinToString(stringBuilder)
        return stringBuilder
    }
}