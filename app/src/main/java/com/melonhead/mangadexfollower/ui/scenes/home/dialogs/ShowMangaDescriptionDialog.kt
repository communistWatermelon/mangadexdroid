package com.melonhead.mangadexfollower.ui.scenes.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.core_ui.extensions.Previews

@Composable
internal fun ShowMangaDescriptionDialog(
    uiManga: com.melonhead.data_core_manga_ui.UIManga?,
    onDismissed: () -> Unit,
) {
    val description = uiManga?.description
    if (description != null) {
        AlertDialog(
            onDismissRequest = { onDismissed() },
            title = {
                Text(text = uiManga.title)
            },
            text = {
                Text(text = description)
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
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        ShowMangaDescriptionDialog(
            Previews.previewUIManga(),
            onDismissed = { })
    }
}
