package com.mindshift.nexre.ui.tags

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MergeType
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindshift.nexre.domain.model.Tag
import com.mindshift.nexre.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagsScreen(
    onTagClick: (String) -> Unit,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val tags by viewModel.tags.collectAsState()
    val renameDialogTag by viewModel.renameDialogTag.collectAsState()
    val mergeSourceTag by viewModel.mergeSourceTag.collectAsState()
    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Topics") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )

        if (tags.isEmpty()) {
            EmptyState(
                message = "No topics yet",
                subtitle = "Tags appear automatically when you save links",
                icon = Icons.Outlined.Sell,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(tags, key = { it.id }) { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onTagClick(tag.name) },
                                onLongClick = { selectedTag = tag },
                            )
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tag.name, style = MaterialTheme.typography.titleSmall)
                            Text("${tag.total} link${if (tag.total != 1) "s" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (tag.unread > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(tag.unread.toString())
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    selectedTag?.let { tag ->
        ModalBottomSheet(onDismissRequest = { selectedTag = null }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                Text(tag.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                TextButton(
                    onClick = { viewModel.openRenameDialog(tag); selectedTag = null },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rename")
                }
                TextButton(
                    onClick = { viewModel.openMergePicker(tag); selectedTag = null },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.MergeType, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Merge into…")
                }
                TextButton(
                    onClick = { showDeleteDialog = true; selectedTag = null },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete tag?") },
            text = { Text("This will remove the tag from all saved links.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTag(selectedTag?.id ?: 0); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    renameDialogTag?.let { tag -> RenameTagDialog(tag, onConfirm = viewModel::confirmRename, onDismiss = viewModel::dismissRenameDialog) }
    mergeSourceTag?.let { tag -> MergeTagPicker(tag, allTags = tags, onConfirm = viewModel::confirmMerge, onDismiss = viewModel::dismissMergePicker) }
}

@Composable
private fun RenameTagDialog(tag: Tag, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember(tag.id) { mutableStateOf(tag.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename tag") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Tag name") },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank() && name != tag.name) {
                Text("Rename")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MergeTagPicker(tag: Tag, allTags: List<Tag>, onConfirm: (Tag) -> Unit, onDismiss: () -> Unit) {
    val targets = allTags.filter { it.id != tag.id }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Merge \"${tag.name}\" into…") },
        text = {
            LazyColumn {
                items(targets, key = { it.id }) { target ->
                    TextButton(onClick = { onConfirm(target) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Label, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(target.name, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
