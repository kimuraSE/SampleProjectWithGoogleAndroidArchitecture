package com.sample.demo1.data.settings

import com.liftric.kvault.KVault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepositoryImpl(private val kvault: KVault) : ThemeRepository {

    private val state = MutableStateFlow(readFromStorage())

    override fun observe(): Flow<ThemeMode> = state.asStateFlow()

    override suspend fun setMode(mode: ThemeMode) {
        kvault.set(KEY_THEME_MODE, mode.name)
        state.value = mode
    }

    private fun readFromStorage(): ThemeMode =
        ThemeMode.fromStorage(kvault.string(KEY_THEME_MODE))

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
