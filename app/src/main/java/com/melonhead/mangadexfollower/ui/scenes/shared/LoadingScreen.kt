package com.melonhead.mangadexfollower.ui.scenes.shared

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
import com.melonhead.mangadexfollower.models.ui.MangaRefreshStatus
import com.melonhead.mangadexfollower.models.ui.None
import com.melonhead.mangadexfollower.models.ui.ReadStatus
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme

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
    MangadexFollowerTheme {
        LoadingScreen(null)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    MangadexFollowerTheme {
        LoadingScreen(ReadStatus)
    }
}
