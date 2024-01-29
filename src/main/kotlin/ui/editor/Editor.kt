package ui.editor

import util.File
import util.LineMetrics
import util.rope.Rope

class Editor(
    val file: File,
    val fileName: String,
    val rope: Rope<LineMetrics>,
    private val selectedEditor: SelectedEditor,
    private val close: (Editor) -> Unit
) {
    val isActive: Boolean
        get() = selectedEditor.selected === this

    fun activate() {
        selectedEditor.selected = this
    }

    fun close() {
        close(this)
    }
}

fun Editor(file: File, selection: SelectedEditor, close: (Editor) -> Unit) = Editor(file, file.name, file.read(), selection, close)