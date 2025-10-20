package com.thiago.android.breakingnews

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview

import com.thiago.android.breakingnews.navigation.main.HomeNavHost

@Composable
@Preview
fun App() {
    MaterialTheme {
        HomeNavHost(navHostController = rememberNavController())
    }
}