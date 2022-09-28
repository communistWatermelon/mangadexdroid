package com.melonhead.mangadexfollower.ui.scenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melonhead.mangadexfollower.models.UIManga
import com.melonhead.mangadexfollower.models.content.Chapter
import com.melonhead.mangadexfollower.models.content.ChapterAttributes
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangadexFollowerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)
                    val manga by viewModel.manga.observeAsState(listOf())

                    Content(isLoggedIn = isLoggedIn,
                        manga = manga,
                        loginClicked = { username, password -> viewModel.authenticate(username, password) })
                }
            }
        }
    }
}

@Composable
fun Content(isLoggedIn: Boolean, manga: List<UIManga>, loginClicked: (username: String, password: String) -> Unit) {
    if (isLoggedIn) {
        ChaptersList(manga)
    } else {
        LoginScreen(loginClicked)
    }
}

@Composable
fun LoginScreen(loginClicked: (username: String, password: String) -> Unit) {
    Column {
        val username = ""
        val password = ""
        Button(onClick = { loginClicked(username, password) }) {
            Text(text = "Sign In")
        }
    }
}

@Composable
fun ChaptersList(manga: List<UIManga>) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(manga) {
            Column(verticalArrangement = Arrangement.SpaceEvenly) {
                Text(text = it.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                it.chapters.forEach {
                    Row(modifier = Modifier.fillMaxWidth().height(44.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${it.attributes.chapter}", fontWeight = FontWeight.Light, fontSize = 16.sp)
                        // todo: display time instead of date if released today
                        Text(text = "${it.attributes.readableAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date}", fontWeight = FontWeight.Light, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MangadexFollowerTheme {
        Content(isLoggedIn = false, listOf(), loginClicked = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun MangaListPreview() {
    MangadexFollowerTheme {
        Content(isLoggedIn = true, listOf(UIManga("", "Test Manga", mutableListOf(Chapter("", ChapterAttributes(null, "33", readableAt = LocalDateTime(2022, 9, 28, 13, 30, 0, 0).toInstant(
            TimeZone.UTC)), relationships = null)))), loginClicked = { _, _ -> })
    }
}