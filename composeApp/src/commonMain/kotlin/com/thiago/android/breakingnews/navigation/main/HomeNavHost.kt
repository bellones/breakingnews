package com.thiago.android.breakingnews.navigation.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.thiago.android.breakingnews.features.about.AboutScreen
import com.thiago.android.breakingnews.features.home.HomeScreen
import com.thiago.android.breakingnews.navigation.routes.HomeRoutes

@Composable
fun HomeNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
){
    NavHost(
        navController = navHostController,
        startDestination = HomeRoutes.Home,
        modifier = modifier

    ) {
        composable<HomeRoutes.About> {
            AboutScreen(
                onBackPress = {
                    navHostController.popBackStack()
                }
            )
        }
        composable<HomeRoutes.Home> {
            HomeScreen(
                navigateToAbout =  {
                    navHostController.navigate(HomeRoutes.About)
                }
            )
        }
    }
}