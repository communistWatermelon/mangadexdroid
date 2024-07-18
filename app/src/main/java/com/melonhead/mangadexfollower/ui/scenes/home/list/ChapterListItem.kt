package com.melonhead.mangadexfollower.ui.scenes.home.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melonhead.mangadexfollower.extensions.Previews
import com.melonhead.mangadexfollower.extensions.dateOrTimeString
import com.melonhead.mangadexfollower.models.ui.*
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChapterListItem(
    modifier: Modifier = Modifier,
    uiChapter: UIChapter,
    uiManga: UIManga,
    refreshStatus: MangaRefreshStatus,
    onChapterClicked: (UIManga, UIChapter) -> Unit,
    onChapterLongPressed: (UIManga, UIChapter) -> Unit,
) {
    Card(modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onChapterClicked(uiManga, uiChapter) },
            onLongClick = { onChapterLongPressed(uiManga, uiChapter) }
        )) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Chapter ${uiChapter.chapter}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp)

                    if (uiManga.lastChapter == uiChapter.chapter) {
                        Text(
                            text = "End",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(text = uiChapter.title ?: "",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
                Text(text = Instant.fromEpochSeconds(uiChapter.createdDate).dateOrTimeString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )
            }
            if (refreshStatus !is None && uiChapter.read != true) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp),
                    strokeWidth = 2.dp)
            } else {
                Text(modifier = Modifier.align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.primary,
                    text = if (uiChapter.read != true) "NEW" else "",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterPreview() {
    MangadexFollowerTheme {
        val manga = Previews.previewUIManga()
        Column {
            ChapterListItem(uiChapter = Previews.previewUIChapters().first(), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
            ChapterListItem(uiChapter = Previews.previewUIChapters().first().copy(read = false), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
            ChapterListItem(uiChapter = Previews.previewUIChapters().first().copy(read = false), uiManga = manga, refreshStatus = None, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
            ChapterListItem(uiChapter = Previews.previewUIChapters().first().copy(chapter = "101"), uiManga = manga.copy(lastChapter = "101"), refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
            ChapterListItem(uiChapter = Previews.previewUIChapters().first().copy(title = "Test Title with an extremely long title that may or may not wrap"), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
        }
    }
}
