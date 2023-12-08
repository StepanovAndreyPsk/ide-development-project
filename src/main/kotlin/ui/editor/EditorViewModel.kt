package ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.styles.AppTheme
import ui.styles.Fonts
import ui.styles.Settings
import util.LineMetrics
import util.getLines
import util.lineCount
import util.maxLineLength
import util.rope.Rope
import kotlin.math.ceil
import kotlin.math.floor

const val GUTTER_TEXT_OFFSET = 5
const val GUTTER_SIZE = GUTTER_TEXT_OFFSET + 2
const val TOP_MARGIN = 0.5f

@Composable
fun BoxScope.EditorView(model: Editor, settings: Settings) = key(model) {
    val textMeasurer = rememberTextMeasurer()
    var renderedText by remember { mutableStateOf<RenderedText?>(null) }

    var verticalScrollOffset by remember { mutableStateOf(0f) }
    var horizontalScrollOffset by remember { mutableStateOf(0f) }

    val textSize = textMeasurer.measure("t", style = getTextStyle(settings)).size
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val rope = model.rope

    val verticalScrollState = rememberScrollableState { delta ->
        val newScrollOffset = coerceVerticalOffset(verticalScrollOffset - delta, rope.lineCount, textSize, canvasSize)
        val scrollConsumed = verticalScrollOffset - newScrollOffset
        verticalScrollOffset = newScrollOffset
        scrollConsumed
    }
    val horizontalScrollState = rememberScrollableState { delta ->
        val newScrollOffset =
            coerceHorizontalOffset(horizontalScrollOffset - delta, rope.maxLineLength, textSize, canvasSize)
        val scrollConsumed = horizontalScrollOffset - newScrollOffset
        horizontalScrollOffset = newScrollOffset
        scrollConsumed
    }
    LaunchedEffect(verticalScrollOffset) {
        withContext(Dispatchers.Default) {
            val currentRenderedText = renderedText
            val verticalOffset = verticalScrollOffset

            if (currentRenderedText == null
                || currentRenderedText.textSize != settings.fontSize
                || (currentRenderedText.from > 0 && verticalOffset - currentRenderedText.from * textSize.height < canvasSize.height / 2)
                || (currentRenderedText.to < rope.lineCount && currentRenderedText.to * textSize.height - verticalOffset - canvasSize.height < canvasSize.height / 2)
            ) {
                val from = floor((verticalOffset - canvasSize.height) / textSize.height).toInt().coerceAtLeast(0)
                val to = ceil((verticalOffset + 2 * canvasSize.height) / textSize.height).toInt()
                    .coerceAtMost(rope.lineCount)
                // in case text size has changed we want to maintain correct verticalScrollOffset
                verticalScrollOffset = coerceVerticalOffset(verticalOffset, rope.lineCount, textSize, canvasSize)
                println("Relayout from $from to $to")
                renderedText = textMeasurer.layoutLines(rope, from, to, settings)
            }
        }
    }
    Canvas(Modifier
        .fillMaxSize()
        .clipToBounds()
        .onSizeChanged { canvasSize = it }
        .scrollable(verticalScrollState, Orientation.Vertical)
        .scrollable(horizontalScrollState, Orientation.Horizontal)
    ) {
        val verticalOffset = verticalScrollOffset

        drawRect(AppTheme.colors.material.background, size = this.size)

        renderedText?.let {
            drawText(
                it.textLayoutResult,
                topLeft = Offset(
                    -horizontalScrollOffset + (GUTTER_TEXT_OFFSET + 4) * textSize.width,
                    -verticalOffset + (it.from + TOP_MARGIN) * textSize.height
                )
            )
        }

        drawGutter(settings, textSize, verticalOffset, textMeasurer, rope.lineCount)
    }

    VerticalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = getMaxVerticalScroll(
                    rope.lineCount,
                    textSize.height,
                    canvasSize.height
                ).toDouble() + viewportSize
            override val scrollOffset: Double
                get() = verticalScrollOffset.toDouble()
            override val viewportSize: Double
                get() = canvasSize.height.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                verticalScrollOffset =
                    coerceVerticalOffset(scrollOffset.toFloat(), rope.lineCount, textSize, canvasSize)
            }

        },
        Modifier.align(Alignment.CenterEnd)
    )

    HorizontalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = getMaxHorizontalScroll(
                    rope.maxLineLength,
                    textSize.width,
                    canvasSize.width
                ) + viewportSize
            override val scrollOffset: Double
                get() = horizontalScrollOffset.toDouble()
            override val viewportSize: Double
                get() = (canvasSize.width - GUTTER_SIZE * textSize.width).toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                horizontalScrollOffset =
                    coerceHorizontalOffset(scrollOffset.toFloat(), rope.maxLineLength, textSize, canvasSize)
            }

        },
        // idk why / 2, it just doesn't work without it
        Modifier.align(Alignment.BottomCenter).absolutePadding(left = (GUTTER_SIZE * textSize.width / 2).dp)
    )
}

private fun coerceVerticalOffset(
    offset: Float,
    textLength: Int,
    textSize: IntSize,
    canvasSize: IntSize
) = offset
    .coerceAtLeast(0f)
    .coerceAtMost(getMaxVerticalScroll(textLength, textSize.height, canvasSize.height).toFloat())

private fun coerceHorizontalOffset(
    offset: Float,
    maxLineLength: Int,
    textSize: IntSize,
    canvasSize: IntSize
) = offset
    .coerceAtLeast(0f)
    .coerceAtMost(getMaxHorizontalScroll(maxLineLength, textSize.width, canvasSize.width).toFloat())

private fun getMaxVerticalScroll(textLength: Int, textHeight: Int, canvasHeight: Int) =
    ((textLength + 5) * textHeight - canvasHeight).coerceAtLeast(0)

private fun getMaxHorizontalScroll(maxLineLength: Int, textWidth: Int, canvasWidth: Int) =
    (maxLineLength * textWidth - canvasWidth + GUTTER_SIZE * textWidth).coerceAtLeast(0)

private fun DrawScope.drawGutter(
    settings: Settings,
    textSize: IntSize,
    verticalScrollOffset: Float,
    textMeasurer: TextMeasurer,
    textLength: Int
) {
    drawRect(
        AppTheme.colors.material.background,
        size = Size((GUTTER_SIZE * textSize.width).toFloat(), size.height)
    )

    val textStyle = getTextStyle(settings).copy(color = AppTheme.code.simple.color.copy(alpha = 0.3f))
    val minLineNumber = (floor(verticalScrollOffset / textSize.height).toInt() - 3).coerceAtLeast(0)
    val maxLineNumber =
        (ceil((verticalScrollOffset + size.height) / textSize.height).toInt() + 3).coerceAtMost(textLength)
    for (lineNumber in minLineNumber until maxLineNumber) {
        val lineNumberString = (lineNumber + 1).toString()
        val xOffset = (GUTTER_TEXT_OFFSET - lineNumberString.length) * textSize.width
        val yOffset = -verticalScrollOffset + (lineNumber + TOP_MARGIN) * textSize.height
        drawText(
            textMeasurer.measure(lineNumberString, textStyle),
            topLeft = Offset(xOffset.toFloat(), yOffset),
        )
    }

    drawLine(
        Color.Gray,
        Offset((GUTTER_SIZE * textSize.width).toFloat(), 0f),
        Offset((GUTTER_SIZE * textSize.width).toFloat(), size.height)
    )
}

private data class RenderedText(
    val textLayoutResult: TextLayoutResult,
    val from: Int,
    val to: Int,
    val textSize: TextUnit
)

private fun TextMeasurer.layoutLines(
    rope: Rope<LineMetrics>,
    from: Int,
    to: Int,
    settings: Settings
): RenderedText {
    val builder = AnnotatedString.Builder()
    val text = rope.getLines(from, to)

    builder.append(text)
    builder.addStyle(AppTheme.code.simple, 0, text.length)

    val textLayoutResult = measure(
        builder.toAnnotatedString(),
        getTextStyle(settings),
    )
    return RenderedText(textLayoutResult, from, to, settings.fontSize)
}

private fun getTextStyle(settings: Settings) =
    TextStyle(fontFamily = Fonts.jetbrainsMono(), fontSize = settings.fontSize)
