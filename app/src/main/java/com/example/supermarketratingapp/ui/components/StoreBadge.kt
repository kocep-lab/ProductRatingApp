package com.example.supermarketratingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun getStoreColor(storeName: String): Color {
    return when (storeName.lowercase()) {
        "woolworths" -> Color(0xFF1B5E20)
        "coles" -> Color(0xFFD32F2F)
        "aldi" -> Color(0xFF0D47A1)
        "tong li", "tongli" -> Color(0xFFC2185B)
        "iga" -> Color(0xFFE65100)
        "asian grocer" -> Color(0xFF7B1FA2)
        "harris farm" -> Color(0xFF33691E)
        else -> Color(0xFF455A64)
    }
}

@Composable
fun StoreBadge(
    storeName: String,
    isSelected: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val storeColor = getStoreColor(storeName)
    val bgColor = if (isSelected) storeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val textColor = if (isSelected) storeColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) storeColor.copy(alpha = 0.5f) else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = storeName,
            fontSize = 11.sp,
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
