package com.thiago.android.breakingnews.domain.model.news

import com.thiago.android.breakingnews.domain.model.article.ArticleModel

data class NewsModel(
    val articles: List<ArticleModel>? = null,
    val status: String? = null,
    val totalResults: Int? = null
)