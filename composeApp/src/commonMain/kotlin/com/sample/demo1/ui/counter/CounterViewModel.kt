package com.sample.demo1.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.demo1.data.counter.CounterRepository
import io.ktor.util.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

class CounterViewModel(
    private val repository: CounterRepository,
) : ViewModel() {

    val uiState: StateFlow<CounterUiState> =
        repository.observe()
            .map<_, CounterUiState> { CounterUiState.Loaded(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CounterUiState.Loading,
            )

    fun increment() {
        // コルーチンスコープ：viewModelScope
        // コルーチンビルダー：.launch
        viewModelScope.launch {
            println("now incrementing...")
            delay(2.seconds)
            repository.increment().onFailure {}
        }
        viewModelScope.launch {
            println("Push +")
        }
        println("done")

    }

    fun decrement() {
        viewModelScope.launch {
            repository.decrement().onFailure {
                // value < 0 になる decrement 呼び出しはここで握りつぶす
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.reset().onFailure {
                // reset は 0 なので通常失敗しないが、将来の拡張に備え Result で受ける
            }
        }
    }
}