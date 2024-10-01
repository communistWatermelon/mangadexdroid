package com.melonhead.feature_manga_list.ui.scenes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.melonhead.lib_core.extensions.Previews
import com.melonhead.data_shared.models.ui.UIManga
import com.melonhead.lib_core.theme.MangadexFollowerTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun MangaCoverListItem(
    modifier: Modifier = Modifier,
    uiManga: UIManga,
    onTapped: (uiManga: UIManga) -> Unit,
) {
    Row(modifier.combinedClickable(onClick = { onTapped(uiManga) })) {
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
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = uiManga.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = 20.sp)

                Text(
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp),
                    text = uiManga.status.uppercase(),
                    maxLines = 1,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    fontSize = 10.sp)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (uiManga.contentRating != "safe") {
                    TagText(
                        text = uiManga.contentRating.replaceFirstChar { it.uppercase() },
                        overrideBackgroundColor = backgroundColorForTag(uiManga.contentRating),
                        overrideTextColor = textColorForTag(tag = uiManga.contentRating)
                    )
                }
                for (tag in uiManga.tags) {
                    TagText(
                        text = tag,
                        overrideBackgroundColor = backgroundColorForTag(tag),
                        overrideTextColor = textColorForTag(tag = tag)
                    )
                }
            }

        }
    }
}

@Composable
private fun backgroundColorForTag(tag: String): Color? {
    return when (tag.lowercase()) {
        "suggestive" -> return MaterialTheme.colorScheme.tertiary
        "erotica" -> return MaterialTheme.colorScheme.error
        "pornographic" -> return MaterialTheme.colorScheme.error
        "long strip" -> return MaterialTheme.colorScheme.secondary
        else -> null
    }
}

@Composable
private fun textColorForTag(tag: String): Color? {
    return when (tag.lowercase()) {
        "suggestive" -> return MaterialTheme.colorScheme.onTertiary
        "erotica" -> return MaterialTheme.colorScheme.onError
        "pornographic" -> return MaterialTheme.colorScheme.onError
        "long strip" -> return MaterialTheme.colorScheme.onSecondary
        else -> null
    }
}


@Composable
private fun TagText(
    text: String,
    overrideBackgroundColor: Color? = null,
    overrideTextColor: Color? = null,
) {
    Text(
        modifier = Modifier
            .background(
                overrideBackgroundColor ?: MaterialTheme.colorScheme.surfaceTint,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp),
        text = text,
        color = overrideTextColor ?: MaterialTheme.colorScheme.surface,
        fontSize = 12.sp
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MangaPreview() {
    MangadexFollowerTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            MangaCoverListItem(
                uiManga = Previews.previewUIManga(),
                onTapped = { }
            )
            MangaCoverListItem(
                uiManga = Previews.previewUIManga()
                    .copy(title = "Test Manga with a really long name that causes the name to clip a little"),
                onTapped = { }
            )
        }
    }
}
