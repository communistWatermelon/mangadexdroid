package com.melonhead.mangadexfollower.ui.scenes.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.mangadexfollower.extensions.Previews
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme

@Composable
internal fun MarkChapterReadDialog(
    mangaChapterPair: Pair<UIManga, UIChapter>?,
    onToggleChapterRead: (UIManga, UIChapter) -> Unit,
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
    MangadexFollowerTheme {
        MarkChapterReadDialog(
            Previews.previewUIManga() to Previews.previewUIChapters().first(),
            onToggleChapterRead = { _, _ -> },
            onDismissed = { })
    }
}
