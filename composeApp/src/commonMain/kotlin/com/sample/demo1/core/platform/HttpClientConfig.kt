package com.sample.demo1.core.platform

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Android / iOS で共通の HttpClient 設定。
 * エンジン（OkHttp / Darwin）選択は platformModule で行い、本関数では
 * プラットフォーム非依存の設定（JSON シリアライザ等）のみを集約する。
 */
internal fun HttpClientConfig<*>.configureHttpClient() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
}
