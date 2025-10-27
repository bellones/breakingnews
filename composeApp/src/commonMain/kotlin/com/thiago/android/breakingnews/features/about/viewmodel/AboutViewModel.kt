package com.thiago.android.breakingnews.features.about.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thiago.android.breakingnews.features.about.action.AboutAction
import com.thiago.android.breakingnews.features.about.state.AboutState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AboutViewModel: ViewModel() {
    private val pendingActions = MutableSharedFlow<AboutAction>()
    private var aboutState: MutableStateFlow<AboutState> = MutableStateFlow(AboutState.Loading)
    val state : StateFlow<AboutState> = aboutState

    init {
        handleActions()
    }

    private fun handleActions () = viewModelScope.launch {
            pendingActions.collect { action ->
                when(action) {
                is AboutAction.Idle ->  {}
                is AboutAction.Loading -> {}
                is AboutAction.OnBackPressed -> onBackPressed()
            }
        }
    }

    private fun onBackPressed () {
        viewModelScope.launch {
            AboutState.OnBackPressed.updateSate()
        }
    }

    fun submitActions (action: AboutAction) = viewModelScope.launch {
        pendingActions.emit(action)
    }

    private fun AboutState.updateSate() = aboutState.update { this }

}