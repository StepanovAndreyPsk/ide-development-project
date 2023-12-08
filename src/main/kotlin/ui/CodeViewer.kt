package ui

import ui.styles.Settings
import ui.editor.Editors
import ui.filetree.FileTree

class CodeViewer(
    val editors: Editors,
    val fileTree: FileTree,
    val settings: Settings
)