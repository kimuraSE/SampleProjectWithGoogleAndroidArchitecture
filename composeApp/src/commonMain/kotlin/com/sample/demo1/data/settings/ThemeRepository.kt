package com.sample.demo1.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * テーマ設定の Single Source of Truth。
 *
 * UI / ViewModel は [observe] から [ThemeMode] を購読するのみ。
 * 永続化の詳細（KVault）は Repository 実装内に隠蔽される。
 */
interface ThemeRepository {
    fun observe(): Flow<ThemeMode>
    suspend fun setMode(mode: ThemeMode)
}
