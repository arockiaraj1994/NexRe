package com.mindshift.nexre.share

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mindshift.nexre.domain.usecase.SaveImageUseCase
import com.mindshift.nexre.worker.StoreLinkWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoreActivity : ComponentActivity() {

    @Inject lateinit var saveImageUseCase: SaveImageUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mimeType = intent.type ?: ""
        when {
            mimeType.startsWith("image/") -> {
                val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }
                if (imageUri == null) { finish(); return }
                // Keep activity alive until copy + save completes so the URI grant stays valid
                lifecycleScope.launch {
                    val result = saveImageUseCase(imageUri)
                    if (result is SaveImageUseCase.Result.Success) {
                        saveImageUseCase.savePendingLink(result.link)
                    }
                    finish()
                }
            }
            else -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
                if (sharedText != null) {
                    val url = extractUrl(sharedText)
                    if (url != null) enqueueUrlWork(url) else enqueueTextWork(sharedText)
                }
                finish()
            }
        }
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
