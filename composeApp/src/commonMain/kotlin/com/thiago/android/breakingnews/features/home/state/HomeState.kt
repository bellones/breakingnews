package com.thiago.android.breakingnews.features.home.state

import com.thiago.android.breakingnews.domain.model.article.ArticleModel

sealed interface HomeState {
    data object Loading: HomeState
    data object Idle: HomeState
    data class NavigateToDetails(
        val urlToImage : String, val description : String
    ): HomeState
    data object NavigateToAbout: HomeState
    data class ShowData(val data: List<ArticleModel>): HomeState
}