package com.sample.demo1.data.settings

/**
 * アプリ全体のテーマ指定を表すデータモデル。
 *
 * - [LIGHT]  : 常にライトテーマ
 * - [DARK]   : 常にダークテーマ
 * - [SYSTEM] : OS のダークモード設定に追従（既定値）
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        val DEFAULT: ThemeMode = SYSTEM

        /** 永続化された文字列から [ThemeMode] を復元。未知値は [DEFAULT] にフォールバック。 */
        fun fromStorage(raw: String?): ThemeMode =
            entries.firstOrNull { it.name == raw } ?: DEFAULT
    }
}
