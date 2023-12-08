package ui.filetree

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import util.File
import ui.editor.Editors

class FileTree(root: File, private val editors: Editors) {
    private val expandableRoot = Node(root, 0).apply {
        toggleExpanded()
    }

    val nodes: List<Node>
        get() = expandableRoot.traverse()

    inner class Node(
        private val file: File,
        val level: Int = 0
    ) {
        val name: String get() = file.name
        var children: List<Node> by mutableStateOf(emptyList())

        val type: NodeType
            get() = if (file.isDirectory) {
                NodeType.Folder(isExpanded = children.isNotEmpty(), canExpand = canExpand)
            } else {
                NodeType.File(extension = file.name.substringAfterLast(".").lowercase())
            }

        fun open() = when (type) {
            is NodeType.Folder -> toggleExpanded()
            is NodeType.File -> editors.open(file)
        }

        private val canExpand: Boolean get() = file.hasChildren

        fun toggleExpanded() {
            children = if (children.isEmpty()) {
                file.children
                    .map { Node(it, level + 1) }
                    .sortedWith(compareBy({ !it.file.isDirectory }, { it.file.name }))
            } else {
                emptyList()
            }
        }

    }

    sealed class NodeType {
        class Folder(val isExpanded: Boolean, val canExpand: Boolean) : NodeType()
        class File(val extension: String) : NodeType()
    }

    private fun Node.traverse(list: MutableList<Node> = mutableListOf()): List<Node> {
        list.add(this)
        children.forEach { it.traverse(list) }
        return list
    }
}
