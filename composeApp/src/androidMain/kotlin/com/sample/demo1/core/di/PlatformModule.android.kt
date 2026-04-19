package com.sample.demo1.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.liftric.kvault.KVault
import com.sample.demo1.core.platform.DatabaseDriverFactory
import com.sample.demo1.core.platform.configureHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
actual fun rememberPlatformModule(): Module {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        module {
            single { DatabaseDriverFactory(context) }
            single { KVault(context) }
            single { HttpClient(OkHttp) { configureHttpClient() } }
        }
    }
}
