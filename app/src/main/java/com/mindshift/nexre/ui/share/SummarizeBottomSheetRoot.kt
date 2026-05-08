package com.mindshift.nexre.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.mindshift.nexre.ui.components.TagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummarizeBottomSheetRoot(
    viewModel: ShareViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state) {
        if (state is ShareUiState.Saved) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            when (val s = state) {
                is ShareUiState.Loading -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Fetching summary…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is ShareUiState.Summary -> {
                    Text(s.link.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Spacer(Modifier.height(12.dp))
                    val summaryLines = s.link.summary.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    if (summaryLines.size > 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            summaryLines.forEach { line ->
                                Text(
                                    text = if (line.startsWith("•")) line else "• $line",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    } else {
                        Text(s.link.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (s.link.tags.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            s.link.tags.forEach { tag -> TagChip(label = tag) }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { viewModel.confirmSave() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
                is ShareUiState.ImagePreview -> {
                    SubcomposeAsyncImage(
                        model = s.sourceUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    var titleText by remember(s.title) { mutableStateOf(s.title) }
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it; viewModel.updateImageTitle(it) },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { viewModel.confirmSave() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
                is ShareUiState.NoApiKey -> {
                    Text("No Gemini API key configured", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Open NexRe → Settings to add your Gemini API key.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
                }
                is ShareUiState.NoInternet -> {
                    Text("No internet connection", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Link will not be saved. Please try again when online.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
                }
                is ShareUiState.Error -> {
                    Text("Could not generate summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("The link was saved with an auto-generated summary.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.confirmSave() }, modifier = Modifier.fillMaxWidth()) { Text("OK") }
                }
                is ShareUiState.Saved -> {}
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
