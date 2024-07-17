package com.melonhead.mangadexfollower.ui.scenes.home.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import kotlinx.datetime.Clock

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MangaCoverListItem(modifier: Modifier = Modifier, uiManga: UIManga, onLongPress: (uiManga: UIManga) -> Unit, onTitleLongPress: (uiManga: UIManga) -> Unit) {
    Row(modifier.combinedClickable(onClick = { }, onLongClick = { onLongPress(uiManga) })) {
        Box(
            modifier
                .padding(horizontal = 10.dp)
                .height(110.dp)) {
            SubcomposeAsyncImage(modifier = Modifier
                .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiManga.coverAddress)
                    .crossfade(true)
                    .build(),
                loading = {
                    Box(modifier = Modifier.width(100.dp)) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                },
                error = {
                    Box(
                        Modifier
                            .width(100.dp)
                            .background(Color.DarkGray)) {
                        Text(text = "No Image",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center))
                    }
                },
                contentDescription = "${uiManga.title} cover"
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Top)
                .padding(top = 20.dp)
                .fillMaxWidth(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier.combinedClickable(onClick = { }, onLongClick = { onTitleLongPress(uiManga) }),
                text = uiManga.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                fontSize = 20.sp)
            Text(text = if (uiManga.useWebview) "WebView" else "Native",
                overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangaPreview() {
    val testChapters = listOf(UIChapter("", "101", "Test Title", Clock.System.now().epochSeconds, true), UIChapter("", "102", "Test Title 2", Clock.System.now().epochSeconds, false))
    MangadexFollowerTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            MangaCoverListItem(uiManga = UIManga("", "Test Manga", testChapters, null, false, altTitles = listOf("Test Manga")), onLongPress = { }, onTitleLongPress = { })
            MangaCoverListItem(uiManga = UIManga("", "Test Manga with a really long name that causes the name to clip a little", testChapters, null, false, altTitles = listOf("Test Manga")), onLongPress = { }, onTitleLongPress = { })
        }
    }
}
