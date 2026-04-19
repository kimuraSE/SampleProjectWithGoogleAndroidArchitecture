package com.sample.demo1.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.demo1.data.settings.ThemeMode
import com.sample.demo1.data.settings.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        repository.observe()
            .map<_, SettingsUiState> { SettingsUiState.Loaded(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState.Loading,
            )

    fun selectTheme(mode: ThemeMode) {
        viewModelScope.launch { repository.setMode(mode) }
    }
}