package com.melonhead.mangadexfollower.ui.scenes.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.core_ui.extensions.Previews

@Composable
internal fun MarkChapterReadDialog(
    mangaChapterPair: Pair<com.melonhead.data_core_manga_ui.UIManga, com.melonhead.data_core_manga_ui.UIChapter>?,
    onToggleChapterRead: (com.melonhead.data_core_manga_ui.UIManga, com.melonhead.data_core_manga_ui.UIChapter) -> Unit,
    onDismissed: () -> Unit,
) {
    if (mangaChapterPair != null) {
        val manga = mangaChapterPair.first
        val chapter = mangaChapterPair.second
        AlertDialog(
            onDismissRequest = { onDismissed() },
            title = {
                Text(text = manga.title)
            },
            text = {
                Text(text = "Mark chapter ${chapter.chapter} as ${if (chapter.read == true) "unread" else "read"}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onToggleChapterRead(manga, chapter)
                    onDismissed()
                }) {
                    Text("Okay")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissed()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
private fun MarkChapterReadPreview() {
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        MarkChapterReadDialog(
            Previews.previewUIManga() to Previews.previewUIChapters().first(),
            onToggleChapterRead = { _, _ -> },
            onDismissed = { })
    }
}
