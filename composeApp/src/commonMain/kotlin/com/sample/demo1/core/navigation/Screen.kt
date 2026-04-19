package com.sample.demo1.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Counter : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object DogImage : Screen
}