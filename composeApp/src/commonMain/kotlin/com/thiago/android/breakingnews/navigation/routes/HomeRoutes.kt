package com.thiago.android.breakingnews.navigation.routes

import kotlinx.serialization.Serializable

sealed interface HomeRoutes {
    @Serializable
    data object Home: HomeRoutes
    @Serializable
    data class  Details(
        val urlToImage : String,
        val description: String
    ): HomeRoutes

    @Serializable
    data object About: HomeRoutes

}