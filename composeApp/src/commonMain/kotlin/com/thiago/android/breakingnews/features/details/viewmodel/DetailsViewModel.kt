package com.thiago.android.breakingnews.features.details.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.thiago.android.breakingnews.features.details.action.DetailsAction
import com.thiago.android.breakingnews.features.details.state.DetailsState
import com.thiago.android.breakingnews.navigation.routes.HomeRoutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private  val savedStateHandle: SavedStateHandle
): ViewModel() {
    private  val pendingActions = MutableSharedFlow<DetailsAction>()
    private  var _state : MutableStateFlow<DetailsState> = MutableStateFlow(DetailsState.Loading)
    val state : StateFlow<DetailsState> = _state
    private val urlToImage: String = savedStateHandle.toRoute<HomeRoutes.Details>().urlToImage
    private val description: String = savedStateHandle.toRoute<HomeRoutes.Details>().description

    init{
        handleActions()
    }

    private fun handleActions () = viewModelScope.launch {
        pendingActions.collect {
            action ->
            when(action) {
                is DetailsAction.Idle -> {}
                is DetailsAction.Loading -> {}
                is DetailsAction.OnBackPressed -> onbackPressed()
                is DetailsAction.RequestUpdateView -> requestUpdateView(
                    urlToImage, description
                )
            }
        }
    }

    private fun requestUpdateView (
        urlToImage : String, description: String
    ) {
        viewModelScope.launch {
            DetailsState.RequestUpdateView(
                urlToImage, description
            ).updateState()
        }
    }

    private  fun onbackPressed () {
        viewModelScope.launch {
            DetailsState.OnBackPressed.updateState()
        }
    }

    fun submitAction(action: DetailsAction) {
        viewModelScope.launch {
            pendingActions.emit(
                action
            )
        }
    }

    private  fun DetailsState.updateState() = _state.update { this }
}