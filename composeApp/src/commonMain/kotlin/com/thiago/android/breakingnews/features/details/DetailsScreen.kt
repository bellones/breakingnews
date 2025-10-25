package com.thiago.android.breakingnews.features.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.thiago.android.breakingnews.ui.content.DetailsContentSection
import com.thiago.android.breakingnews.ui.header.DetailsHeaderSection

@Composable
fun DetailsScreen(
    onBackPress: () -> Unit
) {
    DetailsScreenContent(onBackPress = onBackPress)
}

@Composable
fun DetailsScreenContent (
    onBackPress: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentColor = Color.White,
        topBar = {},
        content = { paddingValues ->
            Column (
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailsHeaderSection( onClick = { onBackPress.invoke()})
                DetailsContentSection( content = "Teste")
            }

        }
    )
}
