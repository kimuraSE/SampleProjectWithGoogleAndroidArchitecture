package com.sample.demo1.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sample.demo1.ui.counter.CounterScreen
import com.sample.demo1.ui.dogimage.DogImageScreen
import com.sample.demo1.ui.home.HomeScreen
import com.sample.demo1.ui.settings.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home,
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToCounter = { navController.navigate(Screen.Counter) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onNavigateToDogImage = { navController.navigate(Screen.DogImage) },
            )
        }
        composable<Screen.Counter> {
            CounterScreen(onBack = { navController.popBackStack() })
        }
        composable<Screen.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable<Screen.DogImage> {
            DogImageScreen(onBack = { navController.popBackStack() })
        }
    }
}
