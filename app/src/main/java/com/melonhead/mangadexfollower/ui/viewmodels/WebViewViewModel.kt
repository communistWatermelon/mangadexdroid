package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.melonhead.mangadexfollower.WebViewActivity
import com.melonhead.mangadexfollower.extensions.asLiveData

class WebViewViewModel(
): ViewModel() {
    private val mutableUrl: MutableLiveData<String?> = MutableLiveData(null)
    val url = mutableUrl.asLiveData()

    fun parseIntent(intent: Intent) {
        mutableUrl.value = intent.getStringExtra(WebViewActivity.EXTRA_URL)
    }
}