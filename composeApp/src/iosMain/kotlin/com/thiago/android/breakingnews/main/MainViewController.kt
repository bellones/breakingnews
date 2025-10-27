package com.thiago.android.breakingnews.main

import androidx.compose.ui.window.ComposeUIViewController
import com.thiago.android.breakingnews.di.main.initializeKoin

fun MainViewController() = ComposeUIViewController (
    configure = { initializeKoin()}
) { App() }