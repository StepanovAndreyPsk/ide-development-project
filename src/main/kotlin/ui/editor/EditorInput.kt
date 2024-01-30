package ui.editor

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import java.awt.event.KeyEvent as AwtKeyEvent
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import idelang.highlighting.HighlightingBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import util.lineCount
import util.lineLength
import java.io.File
import java.util.logging.Logger
import kotlin.math.min
@Composable
internal fun Modifier.pointerInput(editorState: EditorState): Modifier {
    var oldDragOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val cursorPosition = editorState.cursorPosition

    return pointerInput(Unit) {
        detectTapGestures(onPress = { offset ->
            editorState.clearSelection()
            cursorPosition.value = editorState.setCursorByCodePosition(offset)
            editorState.resetCursorBlinking()
        })
    }.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                oldDragOffset = offset
                editorState.startSelection()
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
    val newY = min(y, rope.value.lineCount - 1)
    val lineLength = rope.value.lineLength(newY)
    val newX = min(x, lineLength)
    return CursorPosition(CodePosition(newX, newY), newX)
}

internal fun Modifier.keyboardInput(editorState: EditorState, clipboardManager: ClipboardManager): Modifier =
    onKeyEvent { keyEvent ->
        keyEvent.awtEventOrNull?.let { awtEvent ->
            if (awtEvent.id == AwtKeyEvent.KEY_TYPED) {
                println("`${awtEvent.keyChar}` ${awtEvent.keyChar.code}")

                val hasDeletedSelection = editorState.deleteSelection()

                val rope = editorState.rope.value
                val cursorPosition = editorState.cursorPosition.value

                val ropeIndex = rope.indexOf(cursorPosition.codePosition)
                val (newRope, newCursorPosition) = when (awtEvent.keyChar.code) {
                    AwtKeyEvent.VK_ENTER -> {
                        val newRope = rope.insert(ropeIndex, awtEvent.keyChar)
                        val newCodePosition = cursorPosition.codePosition.let { it.copy(x = 0, y = it.y + 1) }
                        newRope to CursorPosition(newCodePosition, newCodePosition.x)
                    }

                    AwtKeyEvent.VK_BACK_SPACE -> {
                        if (hasDeletedSelection) {
                            rope to cursorPosition
                        } else if (ropeIndex != 0) {
                            val newCodePosition = rope.codePositionOf(ropeIndex - 1)
                            val newRope = rope.delete(ropeIndex - 1, ropeIndex)
                            newRope to CursorPosition(newCodePosition, newCodePosition.x)
                        } else {
                            rope to cursorPosition.copy(wantedX = 0)
                        }
                    }

                    AwtKeyEvent.VK_DELETE -> {
                        if (hasDeletedSelection) {
                            rope to cursorPosition
                        } else if (ropeIndex != rope.length) {
                            rope.delete(ropeIndex, ropeIndex + 1)
                        } else {
                            rope
                        } to cursorPosition.copy(wantedX = cursorPosition.codePosition.x)
                    }

                    else -> {
                        val newRope = rope.insert(
                            editorState.rope.value.indexOf(cursorPosition.codePosition),
                            awtEvent.keyChar
                        )
                        val newCodePosition = cursorPosition.codePosition.let { it.copy(x = it.x + 1) }
                        newRope to CursorPosition(newCodePosition, newCodePosition.x)
                    }
                }
                editorState.rope.value = newRope
                editorState.cursorPosition.value = newCursorPosition
            }
        }

        if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

        val rope = editorState.rope.value
        val cursorPosition = editorState.cursorPosition.value

        when (keyEvent.key) {
            Key.DirectionRight -> {
                if (keyEvent.isCtrlPressed || keyEvent.isAltPressed || keyEvent.isMetaPressed) return@onKeyEvent false

                val clearedSelectionCursors = handleSelectionOnArrows(keyEvent, editorState)
                editorState.cursorPosition.value = if (clearedSelectionCursors != null) {
                    clearedSelectionCursors.second
                } else {
                    val newIndex = rope.indexOf(cursorPosition.codePosition) + 1
                    rope.codePositionOf(newIndex, coerce = true)
                }.let { CursorPosition(it, it.x) }
            }

            Key.DirectionLeft -> {
                if (keyEvent.isCtrlPressed || keyEvent.isAltPressed || keyEvent.isMetaPressed) return@onKeyEvent false

                val clearedSelectionCursors = handleSelectionOnArrows(keyEvent, editorState)
                editorState.cursorPosition.value = if (clearedSelectionCursors != null) {
                    clearedSelectionCursors.first
                } else {
                    val newIndex = rope.indexOf(cursorPosition.codePosition) - 1
                    rope.codePositionOf(newIndex, coerce = true)
                }.let { CursorPosition(it, it.x) }
            }

            Key.DirectionUp -> {
                if (keyEvent.isCtrlPressed || keyEvent.isAltPressed || keyEvent.isMetaPressed) return@onKeyEvent false

                val clearedSelectionCursors = handleSelectionOnArrows(keyEvent, editorState)
                val relevantCursor = clearedSelectionCursors?.first?.let { CursorPosition(it, it.x) } ?: cursorPosition

                editorState.cursorPosition.value = relevantCursor.let { (codePosition, wantedX) ->
                    if (codePosition.y - 1 >= 0) {
                        val lineLength = rope.lineLength(codePosition.y - 1)
                        val newCodePosition = CodePosition(min(wantedX, lineLength), codePosition.y - 1)
                        CursorPosition(newCodePosition, wantedX)
                    } else {
                        CursorPosition(CodePosition(0, 0), 0)
                    }
                }
            }

            Key.DirectionDown -> {
                if (keyEvent.isCtrlPressed || keyEvent.isAltPressed || keyEvent.isMetaPressed) return@onKeyEvent false

                val clearedSelectionCursors = handleSelectionOnArrows(keyEvent, editorState)
                val relevantCursor = clearedSelectionCursors?.second?.let { CursorPosition(it, it.x) } ?: cursorPosition

                editorState.cursorPosition.value = relevantCursor.let { (codePosition, wantedX) ->
                    if (codePosition.y + 1 < rope.lineCount) {
                        val lineLength = rope.lineLength(codePosition.y + 1)
                        val newCodePosition = CodePosition(min(wantedX, lineLength), codePosition.y + 1)
                        CursorPosition(newCodePosition, wantedX)
                    } else {
                        val lineLength = rope.lineLength(codePosition.y)
                        CursorPosition(CodePosition(lineLength, codePosition.y), lineLength)
                    }
                }
            }

            Key.C -> {
                if ((keyEvent.isMetaPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                    || (keyEvent.isCtrlPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                ) {
                    editorState.copySelection(clipboardManager)
                }
            }

            Key.V -> {
                if ((keyEvent.isMetaPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                    || (keyEvent.isCtrlPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                ) {
                    editorState.deleteSelection()
                    editorState.clearSelection()
                    editorState.paste(clipboardManager)
                }
            }

            Key.X -> {
                if ((keyEvent.isMetaPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                    || (keyEvent.isCtrlPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                ) {
                    editorState.copySelection(clipboardManager)
                    editorState.deleteSelection()
                }
            }

            Key.S -> {
                if ((keyEvent.isMetaPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                    || (keyEvent.isCtrlPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isShiftPressed)
                ) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val filename = editorState.file.value.absolutePath
                        val file = File(filename)
                        if (file.exists() && file.canWrite()) {
                            file.writeText(editorState.rope.value.toString())
                            println("file successfully saved")
                        } else if (!file.exists()) {
                            file.writeText(editorState.rope.value.toString())
                            println("file successfully created and saved")
                        } else {
                            println("Error. Could not write to file")
                        }
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
private fun handleSelectionOnArrows(
    keyEvent: KeyEvent,
    editorState: EditorState,
): Pair<CodePosition, CodePosition>? = if (keyEvent.isShiftPressed) {
    editorState.startOrContinueSelection()
    null
} else {
    val position = editorState.getSelection()
    editorState.clearSelection()
    position
}

private fun EditorState.resetCursorBlinking() {
    isCursorVisible.value = true to isCursorVisible.value.second + 1
}

private fun EditorState.startSelection() {
    selectionStart.value = cursorPosition.value.codePosition
}

private fun EditorState.startOrContinueSelection() {
    selectionStart.value = selectionStart.value ?: cursorPosition.value.codePosition
}

private fun EditorState.clearSelection() {
    selectionStart.value = null
}

private fun EditorState.deleteSelection(): Boolean {
    val (startPosition, endPosition) = getSelection() ?: return false

    val currentRope = rope.value
    val startIndex = currentRope.indexOf(startPosition)
    val endIndex = currentRope.indexOf(endPosition)

    selectionStart.value = null
    cursorPosition.value = CursorPosition(startPosition, startPosition.x)
    rope.value = currentRope.delete(startIndex, endIndex)
    return true
}

private fun EditorState.copySelection(clipboardManager: ClipboardManager) {
    val (startPosition, endPosition) = getSelection() ?: return

    val currentRope = rope.value
    val startIndex = currentRope.indexOf(startPosition)
    val endIndex = currentRope.indexOf(endPosition)

    val text = currentRope.slice(startIndex, endIndex).toString()
    clipboardManager.setText(AnnotatedString((text)))
}

private fun EditorState.paste(clipboardManager: ClipboardManager) {
    clipboardManager.getText()?.let { text ->
        val currentRope = rope.value
        val cursorIndex = currentRope.indexOf(cursorPosition.value.codePosition)
        val newRope = currentRope.insert(cursorIndex, text.text)
        val codePosition = newRope.codePositionOf(cursorIndex + text.text.length)

        rope.value = newRope
        cursorPosition.value = CursorPosition(codePosition, codePosition.x)
    }
}
