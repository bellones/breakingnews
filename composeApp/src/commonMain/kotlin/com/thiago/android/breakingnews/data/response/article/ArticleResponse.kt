package com.thiago.android.breakingnews.data.response.article

import com.thiago.android.breakingnews.data.response.source.SourceResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleResponse(
    @SerialName("author")
    val author: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("publishedAt")
    val publishedAt: String? = null,
    @SerialName("source")
    val source: SourceResponse? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("urlToImage")
    val urlToImage: String? = null
)