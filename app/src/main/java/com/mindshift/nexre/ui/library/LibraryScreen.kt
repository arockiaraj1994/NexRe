package com.mindshift.nexre.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.ui.components.EmptyState
import com.mindshift.nexre.ui.components.LinkCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onLinkClick: (Link) -> Unit,
    tagFilter: String? = null,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val links by viewModel.links.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val currentSort by viewModel.sort.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val displayed = if (tagFilter != null) links.filter { tagFilter in it.tags } else links

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (tagFilter != null) "#$tagFilter" else "Library") },
            actions = {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        LibrarySort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label) },
                                onClick = { viewModel.setSort(sort); showSortMenu = false },
                                leadingIcon = {
                                    if (currentSort == sort) Icon(Icons.Outlined.Check, contentDescription = null)
                                },
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )

        if (tagFilter == null) {
            val filters = LibraryFilter.entries
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(currentFilter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                divider = {},
            ) {
                filters.forEach { filter ->
                    Tab(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = {
                            Text(
                                text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
        }

        if (displayed.isEmpty()) {
            EmptyState(
                message = when (currentFilter) {
                    LibraryFilter.UNREAD -> "All caught up!"
                    LibraryFilter.ARCHIVED -> "Nothing archived"
                    LibraryFilter.FAVOURITES -> "No favourites yet"
                    else -> "No links here"
                },
                subtitle = if (currentFilter == LibraryFilter.ALL) "Save links via the share sheet" else "",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(displayed, key = { it.id }) { link ->
                    LinkCard(
                        link = link,
                        onClick = { onLinkClick(link) },
                        onSwipeStartToEnd = if (link.status == LinkStatus.UNREAD) {
                            { viewModel.markRead(link) }
                        } else {
                            { viewModel.markUnread(link) }
                        },
                        onSwipeEndToStart = if (link.status == LinkStatus.ARCHIVED) {
                            { viewModel.unarchive(link) }
                        } else {
                            { viewModel.archive(link) }
                        },
                    )
                }
            }
        }
    }
}
