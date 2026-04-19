package com.sample.demo1.core.di

import com.sample.demo1.core.platform.DatabaseDriverFactory
import com.sample.demo1.data.counter.CounterRepositoryImpl
import com.sample.demo1.data.dogimage.DogImageApi
import com.sample.demo1.data.dogimage.DogImageRepositoryImpl
import com.sample.demo1.data.settings.ThemeRepositoryImpl
import com.sample.demo1.db.Database
import com.sample.demo1.domain.counter.CounterRepository
import com.sample.demo1.domain.dogimage.DogImageRepository
import com.sample.demo1.domain.settings.ThemeRepository
import com.sample.demo1.ui.counter.CounterViewModel
import com.sample.demo1.ui.dogimage.DogImageViewModel
import com.sample.demo1.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { Database(get<DatabaseDriverFactory>().create()) }

    // Counter
    single<CounterRepository> { CounterRepositoryImpl(get()) }
    viewModel { CounterViewModel(get()) }

    // Settings (Theme)
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    viewModel { SettingsViewModel(get()) }

    // DogImage
    single { DogImageApi(get()) }
    single<DogImageRepository> { DogImageRepositoryImpl(get(), get()) }
    viewModel { DogImageViewModel(get()) }
}
