package com.thiago.android.breakingnews.features.about.action

sealed interface AboutAction {
    data object Idle: AboutAction
    data object Loading: AboutAction
    data object OnBackPressed: AboutAction
}