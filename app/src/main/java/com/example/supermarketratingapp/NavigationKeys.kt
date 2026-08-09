package com.example.supermarketratingapp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeKey : NavKey
@Serializable data class ProductDetailKey(val productId: Long) : NavKey
@Serializable data class SearchResultsKey(val query: String) : NavKey
@Serializable data object SettingsKey : NavKey
