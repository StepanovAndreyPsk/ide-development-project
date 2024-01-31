package ui.styles

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

object AppTheme {
    val colors: Colors = Colors()

    val code: Code = Code()

    class Colors(
        val backgroundDark: Color = Color(0xFF2B2B2B),
        val backgroundMedium: Color = Color(0xFF3C3F41),

        val material: androidx.compose.material.Colors = darkColors(
            background = backgroundDark,
            surface = backgroundMedium,
            primary = Color.White
        ),
    )

    class Code(
        val simple: SpanStyle = SpanStyle(Color(0xFFA9B7C6)),
        val value: SpanStyle = SpanStyle(Color(0xFF6897BB)),
        val keyword: SpanStyle = SpanStyle(Color(0xFFCC7832)),
        val blockBrackets: SpanStyle = SpanStyle(Color(0xFFEBC88E)),
        val symbolName: SpanStyle = SpanStyle(Color(0xFFAF9CFF)),
        val type: SpanStyle = SpanStyle(Color(0xFF79ADE3))
    )
}