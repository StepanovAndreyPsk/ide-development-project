package ui.editor.highlightling

import androidx.compose.ui.text.SpanStyle
import idelang.highlighting.HToken


data class StyledRange(val style: SpanStyle, val startOffset: Int, val endOffset: Int)

class Highlighter(_tokens: List<HToken>) {
    val tokens = _tokens.sortedBy { it.concreteToken.location.startOffset }

    fun highlightRange(start: Int, end: Int): List<StyledRange> {
        val styles = mutableListOf<StyledRange>()
        for (htoken in tokens) {
            val loc = htoken.concreteToken.location
            if (loc.endOffset <= start) {
                continue
            } else if (loc.startOffset >= end) {
                continue
            }
            val left = maxOf(loc.startOffset, start) - start
            val right = minOf(loc.endOffset, end) - start
            styles.add(StyledRange(htoken.element.color, left, right))
        }
        return styles
    }
}