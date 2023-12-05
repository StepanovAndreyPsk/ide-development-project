package util.rope


class Rope<Metrics> {
    internal val base: RopeNode<Metrics>

    private constructor(base: RopeNode<Metrics>) {
        this.base = base
    }

    constructor(sequence: CharSequence, metricsCalculator: MetricsCalculator<Metrics>) {
        base = if (sequence.length < splitLength) {
            LeafNode(sequence, metricsCalculator)
        } else {
            sequence.chunked(splitLength).map { LeafNode(it, metricsCalculator) }.let { merge(it) }
        }
    }

    val length: Int
            get() = base.length

    operator fun get(index: Int): Char = base[index]

    fun slice(startIndex: Int, endIndex: Int): Rope<Metrics> {
        return Rope(base.slice(startIndex, endIndex))
    }

    override fun toString(): String {
        return base.joinToString().toString()
    }

    fun insert(idx: Int, sequence: CharSequence): Rope<Metrics> {
        if (idx == 0) {
            return prepend(sequence)
        }
        if (idx == length) {
            return append(sequence)
        }
        val (left, right) = base.split(idx)
        return Rope(
            concat(
                concat(left, LeafNode(sequence, base.metricsCalculator)),
                right
            )
        )
    }

    fun prepend(sequence: CharSequence): Rope<Metrics> {
        val newNode = concat(LeafNode(sequence, base.metricsCalculator), base)
        return Rope(newNode)
    }

    fun append(sequence: CharSequence): Rope<Metrics> {
        val newNode = concat(base, LeafNode(sequence, base.metricsCalculator))
        return Rope(newNode)
    }

    fun delete(start: Int, end: Int): Rope<Metrics> {
        return Rope(base.delete(start, end - start))
    }
}