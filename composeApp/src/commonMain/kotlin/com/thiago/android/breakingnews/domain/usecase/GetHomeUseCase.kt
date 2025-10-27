package com.thiago.android.breakingnews.domain.usecase

import com.thiago.android.breakingnews.data.response.news.NewsResponse
import com.thiago.android.breakingnews.domain.model.news.NewsModel
import com.thiago.android.breakingnews.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetHomeUseCase(private val repository: HomeRepository) {
    fun getBreakingNews() : Flow<NewsModel> = repository.getBreakingNews()
}