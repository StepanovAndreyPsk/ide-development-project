package util.rope

internal interface RopeNode<Metrics> {
    val metrics: Metrics
    val metricsCalculator: MetricsCalculator<Metrics>

    val length: Int

    fun slice(startIndex: Int, endIndex: Int): RopeNode<Metrics>
    operator fun get(index: Int): Char

    fun delete(start: Int, length: Int): RopeNode<Metrics>
    fun split(idx: Int): Pair<RopeNode<Metrics>, RopeNode<Metrics>>
    fun depth(): Int

    fun joinToString(stringBuilder: StringBuilder = StringBuilder()): StringBuilder
}
