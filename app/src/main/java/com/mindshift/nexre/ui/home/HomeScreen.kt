package com.mindshift.nexre.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.model.SummarySource
import com.mindshift.nexre.ui.components.EmptyState
import com.mindshift.nexre.ui.components.LinkCard
import com.mindshift.nexre.ui.components.SourceBadge
import com.mindshift.nexre.ui.components.TagChip
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLinkClick: (Link) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val links by viewModel.links.collectAsState()

    val unreadLinks = remember(links) { links.filter { it.status == LinkStatus.UNREAD } }
    val readLinks = remember(links) { links.filter { it.status == LinkStatus.READ } }

    val unreadCount = unreadLinks.size
    val weeklyReadCount = remember(readLinks) {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        readLinks.count { it.openedAt >= weekAgo }
    }
    val streak = remember(readLinks) { calculateStreak(readLinks) }
    val featuredLink = remember(unreadLinks) {
        unreadLinks.firstOrNull { it.summarySource == SummarySource.GEMINI }
            ?: unreadLinks.firstOrNull()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NexRe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                            Text(
                                text = unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )

        if (links.isEmpty()) {
            EmptyState(
                message = "Share a link to get started",
                subtitle = "Use the Android share sheet and pick \"NexRe — Save\"",
                icon = Icons.Outlined.Bookmarks,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item {
                    StatsRow(
                        unreadCount = unreadCount,
                        streak = streak,
                        weeklyReadCount = weeklyReadCount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                if (featuredLink != null) {
                    item {
                        Text(
                            text = "Next read",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        FeaturedCard(
                            link = featuredLink,
                            onClick = { onLinkClick(featuredLink) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item {
                    Text(
                        text = "All saves",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                items(links, key = { it.id }) { link ->
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    unreadCount: Int,
    streak: Int,
    weeklyReadCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            value = unreadCount.toString(),
            label = "to read",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            valueColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = if (streak > 0) "${streak}d" else "—",
            label = "streak",
            containerColor = Color(0xFFFFF3E0),
            valueColor = Color(0xFFE65100),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = weeklyReadCount.toString(),
            label = "this week",
            containerColor = Color(0xFFE8F5E9),
            valueColor = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = valueColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun FeaturedCard(
    link: Link,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hash = (link.url + link.title).fold(0) { acc, c -> acc + c.code }
    val palettes = listOf(
        Pair(Color(0xFFFFE4D5), Color(0xFFFFB088)),
        Pair(Color(0xFFDDE7FF), Color(0xFFA6BFFF)),
        Pair(Color(0xFFD9F5E6), Color(0xFF86E2B5)),
        Pair(Color(0xFFFFD9E8), Color(0xFFF590B8)),
        Pair(Color(0xFFE8DBFF), Color(0xFFB398FF)),
        Pair(Color(0xFFFFF1B8), Color(0xFFF5C75F)),
        Pair(Color(0xFFD5F0F5), Color(0xFF7AC9D8)),
        Pair(Color(0xFFE8E8E8), Color(0xFFA8A8A8)),
    )
    val (gradStart, gradEnd) = palettes[hash % palettes.size]

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            // Gradient hero band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.linearGradient(listOf(gradStart, gradEnd))),
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 220.dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 260.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Box(modifier = Modifier.padding(14.dp).align(Alignment.BottomStart)) {
                    SourceBadge(source = link.sourcePlatform, size = 32.dp)
                }
            }

            // Content
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (link.summary.isNotBlank()) {
                    val firstLine = link.summary.lines().firstOrNull { it.isNotBlank() } ?: link.summary
                    Text(
                        text = firstLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (link.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        link.tags.take(3).forEach { TagChip(label = it) }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Outlined.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Read now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun calculateStreak(readLinks: List<Link>): Int {
    if (readLinks.isEmpty()) return 0
    val zone = ZoneId.systemDefault()
    val readDays = readLinks
        .filter { it.openedAt > 0 }
        .map { LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.openedAt), zone) }
        .toSet()

    var streak = 0
    var day = LocalDate.now(zone)
    // Allow today with no reads yet — start from yesterday
    if (!readDays.contains(day)) day = day.minusDays(1)
    while (readDays.contains(day)) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}
