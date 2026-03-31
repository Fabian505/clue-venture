package com.clueventure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clueventure.data.model.Adventure
import com.clueventure.data.repository.AdventureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdventureListUiState {
    data object Loading : AdventureListUiState()
    data class Success(val adventures: List<Adventure>) : AdventureListUiState()
    data class Error(val message: String) : AdventureListUiState()
}

class AdventureListViewModel(
    private val repository: AdventureRepository = AdventureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdventureListUiState>(AdventureListUiState.Loading)
    val uiState: StateFlow<AdventureListUiState> = _uiState.asStateFlow()

    init {
        loadAdventures()
    }

    fun loadAdventures() {
        viewModelScope.launch {
            _uiState.value = AdventureListUiState.Loading
            runCatching { repository.getActiveAdventures() }
                .onSuccess { _uiState.value = AdventureListUiState.Success(it) }
                .onFailure { _uiState.value = AdventureListUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
