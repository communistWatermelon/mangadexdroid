package com.melonhead.mangadexfollower.ui.scenes.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CloseBanner(title: String? = null, onDoneTapped: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceTint)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (title != null) {
            Text(text = title, color = MaterialTheme.colorScheme.surface)
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = { onDoneTapped() }) {
            Image(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Button",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Preview
@Composable
private fun CloseBannerPreview() {
    Surface {
        Column {
            CloseBanner { }
            CloseBanner("Test Title") { }
        }
    }
}
