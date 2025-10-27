package com.thiago.android.breakingnews.di.data

import com.thiago.android.breakingnews.data.repository.HomeRepositoryImpl
import com.thiago.android.breakingnews.domain.repository.HomeRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    //Home
    factoryOf(::HomeRepositoryImpl).bind(HomeRepository::class)
}