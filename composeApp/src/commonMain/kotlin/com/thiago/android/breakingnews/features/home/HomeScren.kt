package com.thiago.android.breakingnews.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thiago.android.breakingnews.ui.card.BreakingNewsCard
import com.thiago.android.breakingnews.ui.topbar.BreakingNewsTopBar

@Composable
fun HomeScreen (
    navigateToAbout: () -> Unit,
    navigateToDetails: () -> Unit,
) {
    HomeScreenContent( navigateToAbout = { navigateToAbout.invoke()}, navigateToDetails = { navigateToDetails.invoke()})
}

@Composable
fun HomeScreenContent (
    navigateToDetails: () -> Unit,
    navigateToAbout: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentColor = Color.White,
        topBar = {
            BreakingNewsTopBar(onClick = {navigateToAbout.invoke()})
        },
        content = { paddingValues ->
            Column (modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.Start) {

                Text(
                    "Breaking News",
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    color =  Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(16.dp)
                )
                BreakingNewsCard(
                    title = "Lorem Ipsum dot Amet",
                    author = "Thiago Belao",
                    data = "17/08/1985",
                    onClick = {
                        navigateToDetails.invoke()
                    }

                )
                BreakingNewsCard(
                    title = "Lorem Ipsum dot Amet",
                    author = "Thiago Belao",
                    data = "17/08/1985",
                    onClick = {
                        navigateToDetails.invoke()
                    }

                )
                BreakingNewsCard(
                    title = "Lorem Ipsum dot Amet",
                    author = "Thiago Belao",
                    data = "17/08/1985",
                    onClick = {
                        navigateToDetails.invoke()
                    }

                )
                BreakingNewsCard(
                    title = "Lorem Ipsum dot Amet",
                    author = "Thiago Belao",
                    data = "17/08/1985",
                    onClick = {
                        navigateToDetails.invoke()
                    }

                )

            }
        }

    )
}