package com.thiago.android.breakingnews.di.usecase

import com.thiago.android.breakingnews.domain.usecase.GetHomeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val usecaseModule = module {
    factoryOf(::GetHomeUseCase)
}