package com.thiago.android.breakingnews.data.mapping

import com.thiago.android.breakingnews.data.response.article.ArticleResponse
import com.thiago.android.breakingnews.data.response.news.NewsResponse
import com.thiago.android.breakingnews.data.response.source.SourceResponse
import com.thiago.android.breakingnews.domain.model.article.ArticleModel
import com.thiago.android.breakingnews.domain.model.news.NewsModel
import com.thiago.android.breakingnews.domain.model.source.SourceModel

fun NewsResponse.toModel() = NewsModel (
    status = this.status,
    articles = this.articles?.map { it.toModel() },
    totalResults = this.totalResults,

)
fun ArticleResponse.toModel() = ArticleModel (
    author = this.author,
    title= this.title,
    content = this.content,
    url = this.url,
    source = this.source?.toModel(),
    urlToImage = this.urlToImage,
    description = this.description,
    publishedAt = this.publishedAt
)

fun SourceResponse.toModel() = SourceModel (
    id = this.id,
    name = this.name

)