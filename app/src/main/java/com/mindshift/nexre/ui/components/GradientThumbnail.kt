package com.mindshift.nexre.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.SourcePlatform

private val palettes = listOf(
    Pair(Color(0xFFFFE4D5), Color(0xFFFFB088)),
    Pair(Color(0xFFDDE7FF), Color(0xFFA6BFFF)),
    Pair(Color(0xFFD9F5E6), Color(0xFF86E2B5)),
    Pair(Color(0xFFFFD9E8), Color(0xFFF590B8)),
    Pair(Color(0xFFE8DBFF), Color(0xFFB398FF)),
    Pair(Color(0xFFFFF1B8), Color(0xFFF5C75F)),
    Pair(Color(0xFFD5F0F5), Color(0xFF7AC9D8)),
    Pair(Color(0xFFE8E8E8), Color(0xFFA8A8A8)),
)

@Composable
fun GradientThumbnail(link: Link, size: Dp = 80.dp, cornerRadius: Dp = 12.dp) {
    if (link.thumbnailUrl.isNotBlank()) {
        SubcomposeAsyncImage(
            model = link.thumbnailUrl,
            contentDescription = link.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
            loading = { GradientThumbnailPlaceholder(link, size, cornerRadius) },
            error = { GradientThumbnailPlaceholder(link, size, cornerRadius) },
        )
    } else {
        GradientThumbnailPlaceholder(link, size, cornerRadius)
    }
}

@Composable
private fun GradientThumbnailPlaceholder(link: Link, size: Dp, cornerRadius: Dp) {
    val hash = (link.url + link.title).fold(0) { acc, c -> acc + c.code }
    val (start, end) = palettes[hash % palettes.size]

    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(start, end))),
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.6f)
                .offset(x = size * 0.35f, y = -(size * 0.35f))
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
        Box(modifier = Modifier.padding(6.dp)) {
            SourceBadge(source = link.sourcePlatform, size = (size.value * 0.3f).coerceAtLeast(20f).dp)
        }
    }
}
