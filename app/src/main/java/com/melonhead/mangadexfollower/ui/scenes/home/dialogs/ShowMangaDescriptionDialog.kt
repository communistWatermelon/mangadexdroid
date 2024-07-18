package com.melonhead.mangadexfollower.ui.scenes.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.mangadexfollower.extensions.Previews
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme

@Composable
internal fun ShowMangaDescriptionDialog(
    uiManga: UIManga?,
    onDismissed: () -> Unit,
) {
    if (uiManga?.description != null) {
        AlertDialog(
            onDismissRequest = { onDismissed() },
            title = {
                Text(text = uiManga.title)
            },
            text = {
                Text(text = uiManga.description)
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissed()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Preview
@Composable
private fun MarkChapterReadPreview() {
    MangadexFollowerTheme {
        ShowMangaDescriptionDialog(
            Previews.previewUIManga(),
            onDismissed = { })
    }
}
