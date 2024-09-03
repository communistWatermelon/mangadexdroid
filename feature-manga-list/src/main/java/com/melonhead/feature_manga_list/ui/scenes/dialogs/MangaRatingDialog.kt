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
import com.melonhead.data_shared.models.ui.UIManga
import com.melonhead.lib_core.extensions.Previews
import com.melonhead.lib_core.theme.MangadexFollowerTheme

@Composable
internal fun MangaRatingDialog(
    manga: UIManga?,
    onRatingChanged: (UIManga, Int) -> Unit,
    onDismissed: () -> Unit,
) {
    fun nameForRating(rating: Int): String? {
        return when (rating) {
            1 -> "Appalling"
            2 -> "Horrible"
            3 -> "Very Bad"
            4 -> "Bad"
            5 -> "Average"
            6 -> "Fine"
            7 -> "Good"
            8 -> "Very Good"
            9 -> "Great"
            10 -> "Masterpiece"
            else -> null
        }
    }

    if (manga != null) {
        var selectedRating by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { onDismissed() },
            title = {
                Text(text = "Rate ${manga.title}")
            },
            text = {
                Column {
                    for (i in (1..10).reversed()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (i == selectedRating),
                                    onClick = { selectedRating = i }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (i == selectedRating),
                                onClick = { selectedRating = i }
                            )
                            Text(
                                text = "($i) ${nameForRating(i)}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onRatingChanged(manga, selectedRating!!) }, enabled = selectedRating != null) {
                    Text(text = "Set Rating")
                }
            },
            dismissButton = {
                Button(onClick = onDismissed) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuRatingDialogPreview() {
    MangadexFollowerTheme {
        MangaRatingDialog(
            Previews.previewUIManga(),
            { _, _ -> },
            {},
        )
    }
}
