package ui.editor

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import util.lineCount
import util.lineLength
import kotlin.math.min

@Composable
internal fun Modifier.pointerInput(editorState: EditorState): Modifier {
    var oldDragOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val cursorPosition = editorState.cursorPosition

    return pointerInput(Unit) {
        detectTapGestures(onPress = { offset ->
            cursorPosition.value = editorState.setCursorByCodePosition(offset)
            editorState.resetCursorBlinking()
        })
    }.pointerInput(Unit) { // TODO: selection
        detectDragGestures(
            onDragStart = { offset ->
                oldDragOffset = offset
                cursorPosition.value = editorState.setCursorByCodePosition(offset)
                editorState.resetCursorBlinking()
            },
            onDrag = { _, offset ->
                val newOffset = oldDragOffset + offset
                oldDragOffset = newOffset
                cursorPosition.value = editorState.setCursorByCodePosition(newOffset)
                editorState.resetCursorBlinking()
            })
    }
}

private fun EditorState.setCursorByCodePosition(offset: Offset): CursorPosition {
    val (x, y) = viewportToCode(offset)
    val newY = min(y, rope.lineCount - 1)
    val lineLength = rope.lineLength(newY)
    val newX = min(x, lineLength)
    return CursorPosition(CodePosition(newX, newY), newX)
}

internal fun Modifier.keyboardInput(editorState: EditorState): Modifier =
    onKeyEvent { keyEvent ->
        val cursorPosition = editorState.cursorPosition
        if (keyEvent.hasModifiers || keyEvent.type == KeyEventType.KeyUp) return@onKeyEvent false
        when (keyEvent.key) {
            Key.DirectionRight -> {
                cursorPosition.value = cursorPosition.value.let { (codePosition, _) ->
                    val lineLength = editorState.rope.lineLength(codePosition.y)
                    if (codePosition.x + 1 <= lineLength) {
                        CursorPosition(codePosition.copy(x = codePosition.x + 1), codePosition.x + 1)
                    } else if (codePosition.y + 1 < editorState.rope.lineCount) {
                        CursorPosition(CodePosition(0, codePosition.y + 1), 0)
                    } else {
                        CursorPosition(codePosition, codePosition.x)
                    }
                }
            }

            Key.DirectionLeft -> {
                cursorPosition.value = cursorPosition.value.let { (codePosition, _) ->
                    if (codePosition.x - 1 >= 0) {
                        CursorPosition(codePosition.copy(x = codePosition.x - 1), codePosition.x - 1)
                    } else if (codePosition.y - 1 >= 0) {
                        val lineLength = editorState.rope.lineLength(codePosition.y - 1)
                        CursorPosition(CodePosition(lineLength, codePosition.y - 1), lineLength)
                    } else {
                        CursorPosition(codePosition, codePosition.x)
                    }
                }
            }

            Key.DirectionUp -> {
                cursorPosition.value = cursorPosition.value.let { (codePosition, wantedX) ->
                    if (codePosition.y - 1 >= 0) {
                        val lineLength = editorState.rope.lineLength(codePosition.y - 1)
                        val newCodePosition = CodePosition(min(wantedX, lineLength), codePosition.y - 1)
                        CursorPosition(newCodePosition, wantedX)
                    } else {
                        CursorPosition(CodePosition(0, 0), 0)
                    }
                }
            }

            Key.DirectionDown -> {
                cursorPosition.value = cursorPosition.value.let { (codePosition, wantedX) ->
                    if (codePosition.y + 1 < editorState.rope.lineCount) {
                        val lineLength = editorState.rope.lineLength(codePosition.y + 1)
                        val newCodePosition = CodePosition(min(wantedX, lineLength), codePosition.y + 1)
                        CursorPosition(newCodePosition, wantedX)
                    } else {
                        val lineLength = editorState.rope.lineLength(codePosition.y)
                        CursorPosition(CodePosition(lineLength, codePosition.y), lineLength)
                    }
                }
            }

            else -> {
                return@onKeyEvent false
            }
        }
        editorState.resetCursorBlinking()
        return@onKeyEvent true
    }

private val KeyEvent.hasModifiers
    get() = isCtrlPressed || isAltPressed || isShiftPressed || isMetaPressed

private fun EditorState.resetCursorBlinking() {
    isCursorVisible.value = true to isCursorVisible.value.second + 1
}
