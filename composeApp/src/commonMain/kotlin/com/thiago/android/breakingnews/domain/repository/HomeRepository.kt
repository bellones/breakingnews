package com.thiago.android.breakingnews.domain.repository

import com.thiago.android.breakingnews.data.response.news.NewsResponse
import com.thiago.android.breakingnews.domain.model.news.NewsModel
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    fun getBreakingNews() : Flow<NewsModel>
}