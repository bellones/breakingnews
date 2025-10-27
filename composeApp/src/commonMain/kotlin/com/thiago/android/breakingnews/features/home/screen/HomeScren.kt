package com.thiago.android.breakingnews.features.home.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thiago.android.breakingnews.features.home.action.HomeAction
import com.thiago.android.breakingnews.features.home.state.HomeState
import com.thiago.android.breakingnews.features.home.viewmodel.HomeViewModel
import com.thiago.android.breakingnews.ui.card.BreakingNewsCard
import com.thiago.android.breakingnews.ui.loading.Loading
import com.thiago.android.breakingnews.ui.topbar.BreakingNewsTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen (
    navigateToAbout: () -> Unit,
    navigateToDetails: (urlToImage : String, description: String) -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.submitActions(HomeAction.RequestBreakingNews)
    }
    HomeScreenContent( navigateToAbout = { navigateToAbout.invoke()}, navigateToDetails = navigateToDetails, state = state, action = viewModel::submitActions)
}

@Composable
fun HomeScreenContent (
    state : HomeState,
    navigateToDetails: (urlToImage : String, description: String) -> Unit,
    navigateToAbout: () -> Unit,
    action : (HomeAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentColor = Color.White,
        topBar = {
            BreakingNewsTopBar(onClick = {
                action(HomeAction.RequestNavigationToAbout)
            })
        },
        content = { paddingValues ->
            Column (modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.Start) {


                when(state) {
                    is HomeState.Idle -> {}
                    is HomeState.Loading -> Loading()
                    is HomeState.NavigateToDetails -> {
                        action(HomeAction.Idle)
                        navigateToDetails.invoke(
                            state.urlToImage, state.description
                        )
                    }
                    is HomeState.NavigateToAbout ->  {
                        action(HomeAction.Idle)
                        navigateToAbout.invoke()
                    }
                    is HomeState.ShowData -> {
                        Text(
                            "Breaking News",
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            color =  Color.Black,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(16.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
//                                .scrollable(orientation = Orientation.Vertical )
                        ) {

                            items(state.data) {
                                item ->
                                BreakingNewsCard(
                                    title = item.title.orEmpty(),
                                    author = item.author.orEmpty(),
                                    data = item.publishedAt.orEmpty(),
                                    imageUrl = item.urlToImage.orEmpty(),
                                    onClick = {
                                        action(HomeAction.RequestNavigationToDetails (
                                            urlToImage = item.urlToImage.orEmpty(), description = item.description.orEmpty()
                                        ))
                                    }
                                )
                            }
                        }
                    }

                }




            }
        }

    )
}