package com.melonhead.core_ui.scenes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melonhead.data_core_manga_ui.MangaRefreshStatus
import com.melonhead.data_core_manga_ui.None
import com.melonhead.data_core_manga_ui.ReadStatus

@Composable
fun LoadingScreen(refreshStatus: MangaRefreshStatus?) {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        if (refreshStatus != null && refreshStatus !is None)
            Text(text = refreshStatus.text, fontSize = 16.sp, modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreviewNoStatus() {
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        LoadingScreen(null)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        LoadingScreen(ReadStatus)
    }
}
