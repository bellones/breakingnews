package com.thiago.android.breakingnews.features.details.action

sealed interface DetailsAction  {
    data object Idle: DetailsAction
    data object Loading: DetailsAction
    data object OnBackPressed: DetailsAction
    data object RequestUpdateView: DetailsAction
}