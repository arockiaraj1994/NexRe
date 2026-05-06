package com.mindshift.nexre.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mindshift.nexre.worker.StoreLinkWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (sharedText != null) {
            val url = extractUrl(sharedText)
            if (url != null) enqueueUrlWork(url) else enqueueTextWork(sharedText)
        }
        finish()
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = Regex("""https?://\S+""")
        return urlRegex.find(text)?.value ?: if (text.startsWith("http")) text else null
    }

    private fun enqueueUrlWork(url: String) {
        val request = OneTimeWorkRequestBuilder<StoreLinkWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setInputData(Data.Builder().putString(StoreLinkWorker.KEY_URL, url).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    private fun enqueueTextWork(text: String) {
        val request = OneTimeWorkRequestBuilder<StoreLinkWorker>()
            .setInputData(Data.Builder().putString(StoreLinkWorker.KEY_TEXT, text).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }
}
