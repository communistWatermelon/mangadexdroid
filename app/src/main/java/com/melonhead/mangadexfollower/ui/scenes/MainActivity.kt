package com.melonhead.mangadexfollower.ui.scenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
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
                    val chapters by viewModel.chapters.observeAsState(listOf())

                    Content(isLoggedIn = isLoggedIn,
                        chapters = chapters,
                        loginClicked = { username, password -> viewModel.authenticate(username, password) })
                }
            }
        }
    }
}

@Composable
fun Content(isLoggedIn: Boolean, chapters: List<String>, loginClicked: (username: String, password: String) -> Unit) {
    if (isLoggedIn) {
        ChaptersList(chapters)
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
fun ChaptersList(chapters: List<String>) {
    Column {
        Text(text = chapters.joinToString("\n") { it })
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MangadexFollowerTheme {
        Content(isLoggedIn = false, listOf("1", "2", "2"), loginClicked = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun MangaListPreview() {
    MangadexFollowerTheme {
        Content(isLoggedIn = true, listOf("1", "2", "2"), loginClicked = { _, _ -> })
    }
}