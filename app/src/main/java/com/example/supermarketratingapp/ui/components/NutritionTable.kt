package com.example.supermarketratingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supermarketratingapp.data.local.ProductEntity

@Composable
fun NutritionSection(
    product: ProductEntity,
    modifier: Modifier = Modifier
) {
    val hasNutrition = product.healthStarRating != null ||
            product.energyKj != null ||
            product.proteinG != null ||
            product.fatG != null ||
            product.carbsG != null ||
            product.sodiumMg != null

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nutrition & Health Info",
                    style = MaterialTheme.typography.titleMedium
                )
                if (product.nutriScore != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(getNutriScoreColor(product.nutriScore), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Nutri-Score ${product.nutriScore}",
                            color = Color.White,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (product.healthStarRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Health Star",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Health Star Rating: ${String.format("%.1f", product.healthStarRating)} / 5.0",
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasNutrition) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NutritionRow("Energy", formatNutrient(product.energyKj, "kJ"))
                    NutritionRow("Protein", formatNutrient(product.proteinG, "g"))
                    NutritionRow("Fat (Total)", formatNutrient(product.fatG, "g"))
                    NutritionRow("  - Saturated", formatNutrient(product.saturatedFatG, "g"))
                    NutritionRow("Carbohydrates", formatNutrient(product.carbsG, "g"))
                    NutritionRow("  - Sugars", formatNutrient(product.sugarsG, "g"))
                    NutritionRow("Sodium", formatNutrient(product.sodiumMg, "mg"))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ⓘ Data sourced from Open Food Facts per 100g",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "No detailed nutrition information available for this product.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatNutrient(value: Float?, unit: String): String {
    return if (value != null) "${String.format("%.1f", value)} $unit" else "-"
}

private fun getNutriScoreColor(grade: String): Color {
    return when (grade.uppercase()) {
        "A" -> Color(0xFF008044)
        "B" -> Color(0xFF85BB2F)
        "C" -> Color(0xFFFECB02)
        "D" -> Color(0xFFEE8100)
        "E" -> Color(0xFFE63E11)
        else -> Color(0xFF757575)
    }
}
