package com.melonhead.lib_core.scenes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseBanner(
    title: String,
    modifier: Modifier = Modifier,
    onDoneTapped: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = title)
        },
        colors =  TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceTint,
            titleContentColor = MaterialTheme.colorScheme.surface,
            actionIconContentColor = MaterialTheme.colorScheme.surface
        ),
        actions = {
            IconButton(onClick = { onDoneTapped() }) {
                Image(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Close Button",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
                )
            }
        }
    )
}

@Preview
@Composable
private fun CloseBannerPreview() {
    Surface {
        Column {
            CloseBanner(title = "") { }
            CloseBanner("Test Title") { }
        }
    }
}
