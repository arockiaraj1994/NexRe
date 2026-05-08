package com.mindshift.nexre.ui.detail

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.ui.components.GradientThumbnail
import com.mindshift.nexre.ui.components.SourceBadge
import com.mindshift.nexre.ui.components.TagChip
import com.mindshift.nexre.ui.components.ZoomableImageViewer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val link by viewModel.link.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageViewer by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(link?.personalNote) {
        noteText = link?.personalNote ?: ""
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavourite(link?.isFavourite ?: false) }) {
                        Icon(
                            if (link?.isFavourite == true) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favourite",
                            tint = if (link?.isFavourite == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = { viewModel.archive() }) {
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        link?.let { l ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (l.sourcePlatform == SourcePlatform.IMAGE && l.thumbnailUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = l.thumbnailUrl,
                        contentDescription = l.title,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showImageViewer = true },
                    )
                } else {
                    GradientThumbnail(link = l, size = 200.dp, cornerRadius = 16.dp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceBadge(source = l.sourcePlatform, size = 32.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(l.sourcePlatform.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(l.title, style = MaterialTheme.typography.headlineSmall)

                // For plain text notes, show the full description as the body
                if (l.sourcePlatform == SourcePlatform.TEXT && l.description.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Note",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(l.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (l.summary.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = when (l.summarySource) {
                                    SummarySource.GEMINI -> "Summary by Gemini"
                                    SummarySource.OG_META -> "Auto-summary"
                                    SummarySource.NONE -> "Description"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(10.dp))
                            val lines = l.summary.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                            if (lines.size > 1) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    lines.forEach { line ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text(
                                                text = if (line.startsWith("•")) line else "• $line",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(l.summary, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                if (l.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        l.tags.forEach { tag -> TagChip(label = tag) }
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        noteText = it
                        viewModel.updateNote(it)
                    },
                    label = { Text("Personal note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                StatsRow(link = l)

                if (l.url.isNotBlank()) {
                    Button(
                        onClick = {
                            viewModel.recordOpen()
                            val tabsIntent = CustomTabsIntent.Builder().build()
                            tabsIntent.launchUrl(context, l.url.toUri())
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Did you finish reading?",
                                    actionLabel = "Yes",
                                    duration = SnackbarDuration.Long,
                                )
                                viewModel.recordReadDuration()
                                if (result == SnackbarResult.ActionPerformed) viewModel.markRead()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open in Browser")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete link?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); showDeleteDialog = false; onDeleted() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    if (showImageViewer) {
        link?.thumbnailUrl?.let { url ->
            ZoomableImageViewer(imageUrl = url, onDismiss = { showImageViewer = false })
        }
    }
}

@Composable
private fun StatsRow(link: Link) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(link.savedAt))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            StatItem("Saved", dateStr)
            StatItem("Opened", "${link.readCount}×")
            StatItem("Read for", if (link.readDurationSec > 0) "${link.readDurationSec}s" else "—")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
