package com.mindshift.nexre.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TagChip(
    label: String,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val shape = RoundedCornerShape(999.dp)
    Surface(
        color = if (active) accentColor else MaterialTheme.colorScheme.surface,
        shape = shape,
        modifier = Modifier
            .border(1.dp, if (active) accentColor else MaterialTheme.colorScheme.outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
