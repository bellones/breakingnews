package com.thiago.android.breakingnews.features.home.action

sealed interface HomeAction {
    data object Idle: HomeAction
    data class RequestNavigationToDetails (
       val  urlToImage : String, val description: String
    ): HomeAction
    data object RequestNavigationToAbout: HomeAction
    data object RequestBreakingNews: HomeAction
}