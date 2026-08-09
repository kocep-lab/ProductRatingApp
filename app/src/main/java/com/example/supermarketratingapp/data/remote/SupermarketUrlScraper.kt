package com.example.supermarketratingapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URI

data class ScrapedUrlResult(
    val title: String,
    val storeName: String,
    val imageUrl: String? = null,
    val brand: String? = null,
    val sourceUrl: String
)

class SupermarketUrlScraper {

    suspend fun scrapeUrl(urlInput: String): ScrapedUrlResult = withContext(Dispatchers.IO) {
        val cleanUrl = if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) {
            "https://$urlInput"
        } else urlInput

        val detectedStore = detectStoreFromUrl(cleanUrl)

        // Try Jsoup HTTP fetch
        try {
            val doc = Jsoup.connect(cleanUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(6000)
                .get()

            var title = doc.select("meta[property=og:title]").attr("content")
            if (title.isBlank()) {
                title = doc.select("meta[name=title]").attr("content")
            }
            if (title.isBlank()) {
                title = doc.title()
            }

            var imageUrl = doc.select("meta[property=og:image]").attr("content")
            if (imageUrl.isBlank()) {
                imageUrl = doc.select("meta[name=image]").attr("content")
            }

            var brand = doc.select("meta[property=og:brand]").attr("content")
            if (brand.isBlank()) {
                brand = doc.select("meta[property=product:brand]").attr("content")
            }

            // Clean title strings (e.g. "Smith's Chips | Woolworths" -> "Smith's Chips")
            title = cleanTitleString(title, detectedStore)

            if (title.isNotBlank()) {
                return@withContext ScrapedUrlResult(
                    title = title,
                    storeName = detectedStore,
                    imageUrl = imageUrl.ifBlank { null },
                    brand = brand.ifBlank { null },
                    sourceUrl = cleanUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: Parse URL slug
        val slugTitle = parseTitleFromSlug(cleanUrl)
        return@withContext ScrapedUrlResult(
            title = slugTitle.ifBlank { "Supermarket Product" },
            storeName = detectedStore,
            imageUrl = null,
            brand = null,
            sourceUrl = cleanUrl
        )
    }

    private fun detectStoreFromUrl(url: String): String {
        return try {
            val host = URI(url).host?.lowercase() ?: ""
            when {
                host.contains("woolworths") -> "Woolworths"
                host.contains("coles") -> "Coles"
                host.contains("aldi") -> "Aldi"
                host.contains("tongli") -> "Tong Li"
                host.contains("iga") -> "IGA"
                host.contains("harrisfarm") -> "Harris Farm"
                else -> "Supermarket"
            }
        } catch (e: Exception) {
            "Supermarket"
        }
    }

    private fun cleanTitleString(rawTitle: String, storeName: String): String {
        var clean = rawTitle
        val separators = listOf("|", "-", "—", "•")
        for (sep in separators) {
            if (clean.contains(sep)) {
                val parts = clean.split(sep)
                // Filter out store names from title
                val filtered = parts.filter { !it.trim().equals(storeName, ignoreCase = true) && !it.lowercase().contains("online") && !it.lowercase().contains("buy") }
                if (filtered.isNotEmpty()) {
                    clean = filtered.first().trim()
                    break
                }
            }
        }
        return clean.trim()
    }

    private fun parseTitleFromSlug(url: String): String {
        return try {
            val uri = URI(url)
            val path = uri.path ?: ""
            val segments = path.split("/").filter { it.isNotBlank() }
            if (segments.isNotEmpty()) {
                // Find segment with hyphens
                val slug = segments.lastOrNull { it.contains("-") } ?: segments.last()
                slug.replace("-", " ")
                    .replace(Regex("\\d{5,}"), "") // remove numeric product IDs
                    .split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
                    .trim()
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

}
