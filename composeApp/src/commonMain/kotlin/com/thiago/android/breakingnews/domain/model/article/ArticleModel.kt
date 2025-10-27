package com.thiago.android.breakingnews.domain.model.article

import com.thiago.android.breakingnews.domain.model.source.SourceModel

data class ArticleModel(
    val author: String? = null,
    val content: String? = null,
    val description: String? = null,
    val publishedAt: String? = null,
    val source: SourceModel? = null,
    val title: String? = null,
    val url: String? = null,
    val urlToImage: String? = null
)