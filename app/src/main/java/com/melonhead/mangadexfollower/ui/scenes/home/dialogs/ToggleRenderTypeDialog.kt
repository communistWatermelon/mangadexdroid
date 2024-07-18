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
internal fun ToggleRenderTypeDialog(
    uiManga: UIManga?,
    onRenderTypeToggled: (UIManga) -> Unit,
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
    MangadexFollowerTheme {
        ToggleRenderTypeDialog(Previews.previewUIManga(), onRenderTypeToggled = { }, onDismissed = { })
    }
}
