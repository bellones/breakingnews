package com.thiago.android.breakingnews.features.details.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thiago.android.breakingnews.features.details.action.DetailsAction
import com.thiago.android.breakingnews.features.details.state.DetailsState
import com.thiago.android.breakingnews.features.details.viewmodel.DetailsViewModel
import com.thiago.android.breakingnews.ui.content.DetailsContentSection
import com.thiago.android.breakingnews.ui.header.DetailsHeaderSection
import com.thiago.android.breakingnews.ui.loading.Loading
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailsScreen(
    onBackPress: () -> Unit
) {
    val viewModel = koinViewModel<DetailsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.submitAction(DetailsAction.RequestUpdateView)
    }
    DetailsScreenContent(onBackPress = onBackPress, state = state, action = viewModel::submitAction)
}

@Composable
fun DetailsScreenContent (
    state: DetailsState,
    onBackPress: () -> Unit,
    action: (DetailsAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentColor = Color.White,
        topBar = {},
        content = { paddingValues ->
            when(state) {
                is DetailsState.Idle -> {}
                is DetailsState.Loading -> Loading()
                is DetailsState.OnBackPressed -> {
                    action(DetailsAction.Idle)
                    onBackPress()
                }
                is DetailsState.RequestUpdateView -> {
                    Column (
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DetailsHeaderSection( onClick = { action(DetailsAction.OnBackPressed)}, imageUrl = state.urlToImage)
                        DetailsContentSection( content = state.description)
                    }
                }
            }


        }
    )
}
