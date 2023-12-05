package util.rope

internal class LeafNode<Metrics>(
    val charSequence: CharSequence,
    override val metricsCalculator: MetricsCalculator<Metrics>
) : RopeNode<Metrics> {
    override val length: Int = charSequence.length

    override fun depth(): Int = 0

    override val metrics: Metrics by lazy { metricsCalculator.getMetrics(charSequence) }

    override fun delete(start: Int, length: Int): RopeNode<Metrics> {
        return LeafNode(charSequence.removeRange(start, start + length), metricsCalculator)
    }

    override fun split(idx: Int): Pair<RopeNode<Metrics>, RopeNode<Metrics>> {
        return LeafNode(charSequence.subSequence(0, idx), metricsCalculator) to
                LeafNode(charSequence.subSequence(idx, length), metricsCalculator)
    }

    override fun get(index: Int): Char = charSequence[index]

    override fun slice(startIndex: Int, endIndex: Int): RopeNode<Metrics> {
        return if (startIndex == 0 && endIndex == length) this
        else LeafNode(
            charSequence.subSequence(startIndex, endIndex),
            metricsCalculator
        )
    }

    override fun joinToString(stringBuilder: StringBuilder): StringBuilder {
        return stringBuilder.append(charSequence)
    }
}