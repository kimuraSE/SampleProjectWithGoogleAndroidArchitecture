package com.sample.demo1.core.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * プラットフォーム依存の Koin モジュールを提供する。
 * Android では Context が必要なため、Compose のスコープで解決する。
 */
@Composable
expect fun rememberPlatformModule(): Module