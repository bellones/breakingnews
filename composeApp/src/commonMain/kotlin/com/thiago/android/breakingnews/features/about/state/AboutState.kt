package com.thiago.android.breakingnews.features.about.state

sealed interface AboutState {
    data object Idle: AboutState
    data object Loading: AboutState
    data object OnBackPressed: AboutState
}