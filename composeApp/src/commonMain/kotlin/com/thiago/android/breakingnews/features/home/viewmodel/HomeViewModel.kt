package com.thiago.android.breakingnews.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiago.android.breakingnews.domain.usecase.GetHomeUseCase
import com.thiago.android.breakingnews.features.home.action.HomeAction
import com.thiago.android.breakingnews.features.home.state.HomeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel ( private val getHomeUseCase : GetHomeUseCase): ViewModel() {

    private val pendingAction = MutableSharedFlow<HomeAction>()
    private val _state : MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Loading)
    val state : StateFlow<HomeState> = _state



    init {
        handleActions()
    }

    private  fun getBreakingNews() {
        viewModelScope.launch {
                getHomeUseCase.getBreakingNews().collect {
                HomeState.ShowData(it.articles ?: emptyList()).update()
            }
        }
    }

    fun handleActions() = viewModelScope.launch {
        pendingAction.collect { action ->
            when (action) {
                is HomeAction.Idle -> requestIdleState()
                is HomeAction.RequestNavigationToAbout -> navigateToAbout()
                is HomeAction.RequestNavigationToDetails -> navigateToDetails(
                    urlToImage = action.urlToImage,
                    descrition = action.description
                )
                is HomeAction.RequestBreakingNews -> getBreakingNews()
            }

        }
    }
    fun submitActions (action: HomeAction) = viewModelScope.launch {
        pendingAction.emit(action)
    }

    fun requestIdleState () {
        viewModelScope.launch {
            HomeState.Idle.update()
        }
    }

    fun navigateToDetails ( urlToImage : String, descrition: String) {
        viewModelScope.launch {
            HomeState.NavigateToDetails(urlToImage, descrition).update()
        }
    }

    fun navigateToAbout() {
        viewModelScope.launch {
            HomeState.NavigateToAbout.update()
        }
    }

    private fun HomeState.update () = _state.update { this }
}