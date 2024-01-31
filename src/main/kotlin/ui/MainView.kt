package ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import util.HomeFolder
import ui.styles.AppTheme
import ui.styles.Settings
import ui.editor.Editors
import ui.filetree.FileTree

@Composable
fun MainView() {
    val codeViewer = remember {
        val editors = Editors()

        CodeViewer(
            editors = editors,
            fileTree = FileTree(HomeFolder, editors),
            settings = Settings()
        )
    }

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(codeViewer)
            }
        }
    }
}