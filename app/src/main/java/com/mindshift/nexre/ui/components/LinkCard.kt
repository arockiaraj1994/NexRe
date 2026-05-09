package com.mindshift.nexre.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LinkCard(
    link: Link,
    onClick: () -> Unit,
    onSwipeStartToEnd: (() -> Unit)? = null,
    onSwipeEndToStart: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // rememberUpdatedState ensures the lambda inside confirmValueChange always
    // reads the latest callbacks, even though rememberSwipeToDismissBoxState
    // captures confirmValueChange only on the first composition.
    val currentOnSwipeStartToEnd by rememberUpdatedState(onSwipeStartToEnd)
    val currentOnSwipeEndToStart by rememberUpdatedState(onSwipeEndToStart)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { currentOnSwipeStartToEnd?.invoke(); false }
                SwipeToDismissBoxValue.EndToStart -> { currentOnSwipeEndToStart?.invoke(); false }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Right swipe → Read / Unread toggle
    val readColor = if (link.status == LinkStatus.UNREAD) Color(0xFF30D158) else Color(0xFF007AFF)
    val readIcon: ImageVector = if (link.status == LinkStatus.UNREAD) Icons.Filled.CheckCircle else Icons.Outlined.Replay
    val readLabel = if (link.status == LinkStatus.UNREAD) "Mark read" else "Mark unread"

    // Left swipe → Archive / Unarchive toggle
    val archiveColor = if (link.status == LinkStatus.ARCHIVED) Color(0xFF007AFF) else Color(0xFFFF9500)
    val archiveIcon: ImageVector = if (link.status == LinkStatus.ARCHIVED) Icons.Filled.Unarchive else Icons.Filled.Archive
    val archiveLabel = if (link.status == LinkStatus.ARCHIVED) "Unarchive" else "Archive"

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onSwipeStartToEnd != null,
        enableDismissFromEndToStart = onSwipeEndToStart != null,
        backgroundContent = {
            // requireOffset() throws before the first layout pass; fall back to 0f safely.
            val offset = runCatching { dismissState.requireOffset() }.getOrDefault(0f)
            val dragDirection = when {
                offset > 0f -> SwipeToDismissBoxValue.StartToEnd
                offset < 0f -> SwipeToDismissBoxValue.EndToStart
                else -> SwipeToDismissBoxValue.Settled
            }
            val color by animateColorAsState(
                when (dragDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> readColor
                    SwipeToDismissBoxValue.EndToStart -> archiveColor
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                }
            )
            val isStartToEnd = dragDirection == SwipeToDismissBoxValue.StartToEnd
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = if (isStartToEnd) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isStartToEnd) {
                    Icon(readIcon, contentDescription = readLabel, tint = Color.White)
                } else {
                    Icon(archiveIcon, contentDescription = archiveLabel, tint = Color.White)
                }
            }
        },
        modifier = modifier,
    ) {
        LinkCardContent(link = link, onClick = onClick, onLongClick = onLongClick)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LinkCardContent(
    link: Link,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isRead = link.status == LinkStatus.READ
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isRead) 0.72f else 1f)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            GradientThumbnail(link = link, size = 72.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReadDot(unread = !isRead)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = link.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    link.tags.take(2).forEach { tag -> TagChip(label = tag) }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = timeAgo(link.savedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    if (link.estimatedReadMinutes > 0) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Text(
                            text = "~${link.estimatedReadMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

private fun timeAgo(ms: Long): String {
    val diff = System.currentTimeMillis() - ms
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms))
    }
}
