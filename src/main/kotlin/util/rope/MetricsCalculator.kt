package util.rope

interface MetricsCalculator<Metrics> {
    fun getMetrics(charSequence: CharSequence): Metrics

    fun joinMetrics(left: Metrics, right: Metrics): Metrics
}