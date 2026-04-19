package com.sample.demo1

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.sample.demo1.core.di.appModule
import com.sample.demo1.core.di.rememberPlatformModule
import com.sample.demo1.core.navigation.AppNavHost
import com.sample.demo1.domain.settings.ThemeMode
import com.sample.demo1.domain.settings.ThemeRepository
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val platformModule = rememberPlatformModule()
    KoinApplication(application = { modules(appModule, platformModule) }) {
        ConfigureCoil()
        AppTheme {
            AppNavHost()
        }
    }
}

/**
 * Coil 3 の画像ローダーを Ktor バックエンドで初期化する。
 * DI に登録済みの HttpClient を Coil に渡すことで、HTTP 設定を一元化する。
 */
@Composable
private fun ConfigureCoil() {
    val httpClient = koinInject<io.ktor.client.HttpClient>()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = httpClient))
            }
            .build()
    }
}

/**
 * ThemeRepository の値を購読し MaterialTheme の配色を切り替える。
 * GAA 原則に従い、Composable は単に Repository の Flow を読むだけで、
 * 色の決定ロジックは下位で完結する。
 */
@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val repository: ThemeRepository = koinInject()
    val mode by repository.observe().collectAsStateWithLifecycle(ThemeMode.DEFAULT)
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
    MaterialTheme(
        colorScheme = if (useDark) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
