package com.sample.demo1.ui.dogimage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.demo1.domain.dogimage.DogImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DogImageViewModel(
    private val repository: DogImageRepository,
) : ViewModel() {

    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DogImageUiState> =
        combine(
            repository.observe(),
            isLoading,
            errorMessage,
        ) { image, loading, error ->
            DogImageUiState(
                image = image,
                isLoading = loading,
                errorMessage = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DogImageUiState(),
        )

    init {
        // 初回起動でキャッシュが空なら自動取得を試みる
        viewModelScope.launch {
            if (repository.observe().first() == null) reload()
        }
    }

    fun reload() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            repository.fetchRandom().onFailure {
                errorMessage.value = it.message ?: "画像の取得に失敗しました"
            }
            isLoading.value = false
        }
    }
}
