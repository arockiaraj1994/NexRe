package com.mindshift.nexre.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mindshift.nexre.ui.share.SummarizeBottomSheetRoot
import com.mindshift.nexre.ui.share.ShareViewModel
import com.mindshift.nexre.ui.theme.NexReTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummarizeActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (sharedText == null) { finish(); return }

        val url = extractUrl(sharedText)
        if (url != null) {
            viewModel.startSummarize(url)
        } else {
            viewModel.startSummarizeText(sharedText)
        }

        setContent {
            NexReTheme {
                SummarizeBottomSheetRoot(
                    viewModel = viewModel,
                    onDismiss = { finish() },
                )
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = Regex("""https?://\S+""")
        return urlRegex.find(text)?.value ?: if (text.startsWith("http")) text else null
    }
}
