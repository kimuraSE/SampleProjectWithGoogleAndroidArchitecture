package com.sample.demo1.core.platform

import app.cash.sqldelight.db.SqlDriver

/**
 * SQLDelight の SqlDriver をプラットフォームごとに生成するファクトリ。
 * androidMain / iosMain でそれぞれ actual 実装を提供する。
 */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}