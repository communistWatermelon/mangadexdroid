package com.melonhead.feature_native_chapter_viewer.ui.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.melonhead.lib_core.scenes.CloseBanner
import com.melonhead.lib_core.scenes.LoadingScreen

@Composable
internal fun LoadingChapterView(
    onDoneTapped: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner("Loading...", onDoneTapped = onDoneTapped)
            LoadingScreen(refreshStatus = null)
        }
    }
}
