package com.melonhead.feature_manga_list.ui.scenes.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.melonhead.lib_core.extensions.Previews
import com.melonhead.lib_core.models.UIManga

@Composable
internal fun TitleChangeDialog(
    uiManga: UIManga?,
    onChangeMangaTitle: (UIManga, String) -> Unit,
    onDismissed: () -> Unit,
) {
    if (uiManga != null) {
        val options = uiManga.altTitles
        var selectedOption by remember { mutableStateOf(uiManga.title) }

        AlertDialog(
            onDismissRequest = { onDismissed() },
            confirmButton = {
                Button(onClick = {
                    onChangeMangaTitle(uiManga, selectedOption)
                    onDismissed()
                }) {
                    Text("Change Title")
                }
            },
            title = { Text("Select an Option") },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { selectedOption = option }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = { selectedOption = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}

@Preview
@Composable
private fun TitleChangePreview() {
    com.melonhead.lib_core.theme.MangadexFollowerTheme {
        TitleChangeDialog(
            Previews.previewUIManga(),
            onChangeMangaTitle = { _, _ -> },
            onDismissed = { })
    }
}
