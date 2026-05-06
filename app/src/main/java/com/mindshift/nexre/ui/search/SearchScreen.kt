package com.mindshift.nexre.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.ui.components.EmptyState
import com.mindshift.nexre.ui.components.LinkCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onLinkClick: (Link) -> Unit,
    onClose: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = {},
            active = true,
            onActiveChange = { if (!it) onClose() },
            placeholder = { Text("Search links, tags, notes…") },
            leadingIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (query.isBlank()) {
                EmptyState(
                    message = "Search your saved links",
                    subtitle = "Search by title, description, tags, or notes",
                    icon = Icons.Outlined.Search,
                )
            } else if (results.isEmpty()) {
                EmptyState(
                    message = "No results for \"$query\"",
                    subtitle = "Try different keywords",
                    icon = Icons.Outlined.Search,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(results, key = { it.id }) { link ->
                        LinkCard(link = link, onClick = { onLinkClick(link) })
                    }
                }
            }
        }
    }
}
