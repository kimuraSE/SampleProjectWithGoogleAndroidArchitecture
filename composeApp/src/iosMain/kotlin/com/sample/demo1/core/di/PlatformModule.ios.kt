package com.sample.demo1.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.liftric.kvault.KVault
import com.sample.demo1.core.platform.DatabaseDriverFactory
import com.sample.demo1.core.platform.configureHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
actual fun rememberPlatformModule(): Module = remember {
    module {
        single { DatabaseDriverFactory() }
        single { KVault() }
        single { HttpClient(Darwin) { configureHttpClient() } }
    }
}
