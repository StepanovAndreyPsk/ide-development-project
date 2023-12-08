package util

import util.rope.ConcatNode
import util.rope.LeafNode
import util.rope.MetricsCalculator
import util.rope.Rope

class LineMetricsCalculator : MetricsCalculator<LineMetrics> {
    override fun getMetrics(charSequence: CharSequence): LineMetrics {
        val lengthMetric = if (charSequence.contains('\n')) {
            LineWithBreaks(
                charSequence.indexOf('\n'),
                charSequence.lines().maxOf { it.length },
                charSequence.length - charSequence.indexOfLast { it == '\n' } - 1
            )
        } else {
            LineWithoutBreaks(charSequence.length)
        }

        return LineMetrics(charSequence.count { it == '\n' }, lengthMetric)
    }

    override fun joinMetrics(left: LineMetrics, right: LineMetrics): LineMetrics {
        val lengthMetric = when (val leftLength = left.lineLengthMetric) {
            is LineWithBreaks -> {
                when (val rightLength = right.lineLengthMetric) {
                    is LineWithBreaks -> LineWithBreaks(
                        leftLength.leftLineLength,
                        maxOf(
                            leftLength.maxInnerLineLength,
                            leftLength.rightLineLength + rightLength.leftLineLength,
                            rightLength.maxInnerLineLength
                        ),
                        rightLength.rightLineLength
                    )

                    is LineWithoutBreaks -> leftLength.copy(rightLineLength = leftLength.rightLineLength + rightLength.length)
                }
            }

            is LineWithoutBreaks -> {
                when (val rightLength = right.lineLengthMetric) {
                    is LineWithBreaks -> rightLength.copy(leftLineLength = leftLength.length + rightLength.leftLineLength)
                    is LineWithoutBreaks -> LineWithoutBreaks(leftLength.length + rightLength.length)
                }
            }
        }

        return LineMetrics(left.newLinesCount + right.newLinesCount, lengthMetric)
    }
}

data class LineMetrics(
    val newLinesCount: Int,
    val lineLengthMetric: LineLengthMetric
)

sealed class LineLengthMetric
data class LineWithoutBreaks(val length: Int) : LineLengthMetric()
data class LineWithBreaks(val leftLineLength: Int, val maxInnerLineLength: Int, val rightLineLength: Int) :
    LineLengthMetric()

fun Rope<LineMetrics>.getLines(from: Int, to: Int): String {
    return this.slice(getIndexOfKthLine(from), getIndexOfKthLine(to)).toString()
}

private fun Rope<LineMetrics>.getIndexOfKthLine(k: Int): Int {
    if (k == 0) return 0

    var index = 0
    var lineBreaks = 0
    var currentNode = base
    while (currentNode is ConcatNode) {
        val leftNewLineCount = currentNode.left.metrics.newLinesCount
        if (leftNewLineCount + lineBreaks >= k) currentNode = currentNode.left
        else {
            index += currentNode.left.length
            lineBreaks += leftNewLineCount
            currentNode = currentNode.right
        }
    }
    val charSequence = (currentNode as LeafNode).charSequence

    var start = 0
    for (i in lineBreaks until k) {
        start = charSequence.indexOf('\n', start + 1)
        if (start == -1) return index + charSequence.length
    }
    return index + start + 1
}

val Rope<LineMetrics>.lineCount: Int
    get() = this.base.metrics.newLinesCount + 1

val Rope<LineMetrics>.maxLineLength: Int
    get() = when (val maxLength = this.base.metrics.lineLengthMetric) {
        is LineWithBreaks -> maxOf(maxLength.leftLineLength, maxLength.maxInnerLineLength, maxLength.rightLineLength)
        is LineWithoutBreaks -> maxLength.length
    }
