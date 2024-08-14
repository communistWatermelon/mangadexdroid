package com.melonhead.mangadexfollower.ui.scenes.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.core_ui.extensions.Previews

@Composable
internal fun ToggleRenderTypeDialog(
    uiManga: com.melonhead.data_core_manga_ui.UIManga?,
    onRenderTypeToggled: (com.melonhead.data_core_manga_ui.UIManga) -> Unit,
    onDismissed: () -> Unit,
) {
    if (uiManga != null) {
        AlertDialog(
            onDismissRequest = { onDismissed() },
            title = {
                Text(text = uiManga.title)
            },
            text = {
                Text(text = "Switch to ${if (uiManga.useWebview) "native" else "webView"} reader for manga?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenderTypeToggled(uiManga)
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
private fun ToggleRenderTypePreview() {
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        ToggleRenderTypeDialog(
            Previews.previewUIManga(),
            onRenderTypeToggled = { },
            onDismissed = { })
    }
}
