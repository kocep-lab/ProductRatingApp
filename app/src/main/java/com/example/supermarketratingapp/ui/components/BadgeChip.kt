package com.example.supermarketratingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ProductBadge(val key: String, val label: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MUST_BUY("MUST_BUY", "Must Buy", Color(0xFF2E7D32), Icons.Filled.ThumbUp),
    ON_SALE_ONLY("ON_SALE_ONLY", "On Sale Only", Color(0xFFEF6C00), Icons.Filled.LocalOffer),
    AVOID("AVOID", "Avoid", Color(0xFFC62828), Icons.Filled.Block);

    companion object {
        fun fromKey(key: String?): ProductBadge? = values().firstOrNull { it.key == key }
    }
}

@Composable
fun BadgeChip(
    badge: ProductBadge,
    isSelected: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) badge.color.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) badge.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val contentColor = if (isSelected) badge.color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = badge.icon,
            contentDescription = badge.label,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = badge.label,
            fontSize = 12.sp,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
