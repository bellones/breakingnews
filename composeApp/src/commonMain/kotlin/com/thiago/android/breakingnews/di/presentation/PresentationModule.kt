package com.thiago.android.breakingnews.di.presentation

import com.thiago.android.breakingnews.features.about.viewmodel.AboutViewModel
import com.thiago.android.breakingnews.features.details.viewmodel.DetailsViewModel
import com.thiago.android.breakingnews.features.home.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
        viewModelOf(::HomeViewModel)
        viewModelOf(::DetailsViewModel)
        viewModelOf(::AboutViewModel)
    }