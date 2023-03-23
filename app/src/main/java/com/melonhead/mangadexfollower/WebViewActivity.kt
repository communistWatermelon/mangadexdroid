package com.melonhead.mangadexfollower

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.WebViewViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WebViewActivity : ComponentActivity() {
    private val viewModel by viewModel<WebViewViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MangadexFollowerTheme {
                val url by viewModel.url.observeAsState()

                WebView(url = url, callClose = {
                    finish()
                })
            }
        }

        viewModel.parseIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            viewModel.parseIntent(intent)
        }
    }

    companion object {
        const val EXTRA_URL = "EXTRA_URL"

        fun newIntent(context: Context, url: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            return intent
        }
    }
}


@Composable
private fun WebView(url: String?, callClose: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner(callClose)

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    if (url != null) {
                        loadUrl(url)
                    }
                }
            }, update = {
                if (url != null) {
                    it.loadUrl(url)
                }
            })
        }
    }
}

@Composable
private fun CloseBanner(callClose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceTint)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = { callClose() }) {
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
        CloseBanner { }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    Surface {
        WebView("") { }
    }
}

