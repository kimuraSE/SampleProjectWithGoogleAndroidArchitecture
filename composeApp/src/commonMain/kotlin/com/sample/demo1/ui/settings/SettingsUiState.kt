package com.sample.demo1.ui.settings

import com.sample.demo1.data.settings.ThemeMode

/**
 * Settings 画面の UI State。
 *
 * Counter 画面と同様に sealed interface で表現し、
 * 初回ロード中とロード済みを型で排他化する。
 */
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Loaded(val themeMode: ThemeMode) : SettingsUiState
}