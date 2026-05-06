package com.mindshift.nexre.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(val icon: ImageVector, val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(Icons.Outlined.Share, "Save anything", "Share any link from any app to NexRe. Silent save — no interruptions, just a notification confirming it's saved."),
    OnboardingPage(Icons.Outlined.AutoAwesome, "AI summaries", "Use \"NexRe — Summarize\" in the share sheet to get a Gemini-powered 3–5 sentence summary and smart tags."),
    OnboardingPage(Icons.Outlined.TrendingUp, "Track your reading", "See what you've actually read, how long you spent, and which topics you keep coming back to."),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(96.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(page.icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(page.body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            repeat(pages.size) { i ->
                Surface(
                    color = if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                    modifier = Modifier.padding(4.dp).size(if (i == pagerState.currentPage) 10.dp else 8.dp),
                ) {}
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)) {
            if (pagerState.currentPage < pages.size - 1) {
                Button(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next")
                }
            } else {
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Get Started")
                }
            }
        }
    }
}
