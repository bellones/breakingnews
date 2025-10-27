package com.thiago.android.breakingnews.di.main

import com.thiago.android.breakingnews.di.data.dataModule
import com.thiago.android.breakingnews.di.network.networkModule
import com.thiago.android.breakingnews.di.presentation.presentationModule
import com.thiago.android.breakingnews.di.usecase.usecaseModule
import org.koin.core.context.startKoin
import org.koin.core.KoinApplication

fun initializeKoin (
    config: (KoinApplication.() -> Unit) ? = null
) {
    startKoin {
        config?.invoke(this)
        modules(
            dataModule, networkModule, presentationModule, usecaseModule
        )
    }
}