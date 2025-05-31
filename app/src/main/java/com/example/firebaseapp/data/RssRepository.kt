package com.example.firebaseapp.data

import com.prof18.rssparser.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RssRepository {

    private val rssParser = RssParser()

    val rssFeeds = mapOf(
        "РИА Новости" to "https://ria.ru/export/rss2/archive/index.xml",
        "ТАСС" to "https://tass.ru/rss/v2.xml",
        "Интерфакс" to "https://www.interfax.ru/rss.asp",
        "Ведомости" to "https://www.vedomosti.ru/rss/news",
        "Коммерсантъ" to "https://www.kommersant.ru/RSS/news.xml",
        "Lenta.ru" to "https://lenta.ru/rss/news",
        "RT (Russia Today)" to "https://russian.rt.com/rss"
    )

    suspend fun fetchAllFeeds(): List<NewsItem> = withContext(Dispatchers.IO) {
        val allItems = mutableListOf<NewsItem>()
        for ((sourceName, url) in rssFeeds) {
            try {
                val channel = rssParser.getRssChannel(url)
                for (item in channel.items) {
                    allItems.add(
                        NewsItem(
                            title = item.title ?: "Без заголовка",
                            link = item.link ?: "",
                            pubDate = item.pubDate ?: "",
                            description = item.description ?: "",
                            sourceName = sourceName
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withContext allItems.sortedByDescending { it.pubDate }
    }

    fun getAvailableSources(): List<String> {
        return rssFeeds.keys.toList()
    }

    fun filterFeeds(
        allNews: List<NewsItem>,
        selectedSource: String?,
        selectedTopic: String?,
        keyword: String
    ): List<NewsItem> {
        return allNews.filter { item ->
            val sourceMatches = selectedSource == null || item.sourceName == selectedSource
            val topicMatches = selectedTopic == null || item.title.contains(selectedTopic, ignoreCase = true)
            val keywordMatches = keyword.isBlank() || item.title.contains(keyword, ignoreCase = true) || item.description.contains(keyword, ignoreCase = true)
            sourceMatches && topicMatches && keywordMatches
        }
    }
}
