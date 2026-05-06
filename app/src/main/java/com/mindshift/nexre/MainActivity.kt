package com.mindshift.nexre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mindshift.nexre.ui.navigation.NexReNavHost
import com.mindshift.nexre.ui.theme.NexReTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexReTheme {
                val showOnboarding by viewModel.showOnboarding.collectAsState()
                NexReNavHost(
                    showOnboarding = showOnboarding,
                    onOnboardingDone = { viewModel.completeOnboarding() },
                )
            }
        }
    }
}
