package com.thiago.android.breakingnews.features.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen (
    navigateToAbout: () -> Unit
) {
    Column (modifier = Modifier.fillMaxSize().safeDrawingPadding().safeContentPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            colors = ButtonDefaults.buttonColors(
                Color.LightGray,
                contentColor = Color.Black
            ),
            onClick = {
                navigateToAbout.invoke()
            },

        ) {
            Text(
                "Navegar"
            )
        }
    }
}
