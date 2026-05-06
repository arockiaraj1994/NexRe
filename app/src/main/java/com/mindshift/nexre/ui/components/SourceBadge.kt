package com.mindshift.nexre.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindshift.nexre.domain.model.SourcePlatform

private data class BadgeStyle(val bg: Color, val fg: Color, val text: String)

private val styles = mapOf(
    SourcePlatform.GITHUB to BadgeStyle(Color(0xFF1A1A1A), Color.White, "GH"),
    SourcePlatform.LINKEDIN to BadgeStyle(Color(0xFF0A66C2), Color.White, "in"),
    SourcePlatform.TWITTER to BadgeStyle(Color(0xFF000000), Color.White, "𝕏"),
    SourcePlatform.MEDIUM to BadgeStyle(Color(0xFF000000), Color.White, "M"),
    SourcePlatform.DEV to BadgeStyle(Color(0xFF0A0A0A), Color.White, "DEV"),
    SourcePlatform.STACKOVERFLOW to BadgeStyle(Color(0xFFF48024), Color.White, "SO"),
    SourcePlatform.RESEARCH to BadgeStyle(Color(0xFFB31B1B), Color.White, "arX"),
    SourcePlatform.WEB to BadgeStyle(Color(0xFF374151), Color.White, "Web"),
    SourcePlatform.TEXT to BadgeStyle(Color(0xFF5C6BC0), Color.White, "Txt"),
)

@Composable
fun SourceBadge(source: SourcePlatform, size: Dp = 28.dp) {
    val style = styles[source] ?: styles[SourcePlatform.WEB]!!
    val fontSize = when {
        style.text.length <= 2 -> (size.value * 0.42f).sp
        style.text.length == 3 -> (size.value * 0.32f).sp
        else -> (size.value * 0.26f).sp
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(style.bg),
    ) {
        Text(
            text = style.text,
            color = style.fg,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = if (source == SourcePlatform.DEV) FontFamily.Monospace else FontFamily.Default,
            fontStyle = if (source == SourcePlatform.RESEARCH) FontStyle.Italic else FontStyle.Normal,
        )
    }
}
