package com.mindshift.nexre.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReadDot(unread: Boolean, accentColor: Color = MaterialTheme.colorScheme.primary) {
    if (unread) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor),
        )
    } else {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
    }
}
