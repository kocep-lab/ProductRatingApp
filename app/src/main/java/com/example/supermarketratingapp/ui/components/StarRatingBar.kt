package com.example.supermarketratingapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChanged: ((Float) -> Unit)? = null,
    starSize: Dp = 24.dp,
    starColor: Color = Color(0xFFFFB300),
    showValueText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        for (i in 1..5) {
            val isFull = rating >= i
            val isHalf = rating >= i - 0.5f && rating < i

            val icon = when {
                isFull -> Icons.Filled.Star
                isHalf -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarBorder
            }

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                tint = if (isFull || isHalf) starColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i.toFloat()) }
                        } else Modifier
                    )
            )
        }

        if (showValueText && rating > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.labelLarge,
                color = starColor
            )
        }
    }
}
