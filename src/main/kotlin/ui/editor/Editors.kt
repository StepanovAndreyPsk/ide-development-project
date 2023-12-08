package ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import util.File

class Editors {
    private val selectedEditor = SelectedEditor()

    var editors = mutableStateListOf<Editor>()
        private set

    val active: Editor? get() = selectedEditor.selected

    fun open(file: File) {
        val editor = Editor(file, selectedEditor) { close(it) }
        editors.add(editor)
        editor.activate()
    }

    private fun close(editor: Editor) {
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selectedEditor.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }
}

class SelectedEditor {
    var selected: Editor? by mutableStateOf(null)
}
