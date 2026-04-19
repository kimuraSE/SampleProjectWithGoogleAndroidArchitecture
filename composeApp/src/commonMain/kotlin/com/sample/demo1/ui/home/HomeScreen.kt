package com.sample.demo1.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Home 画面（ハブ画面）。
 *
 * 要件定義書 §3.1 に基づき、状態を持たない純粋な導線画面として実装する。
 * ViewModel は設けず、遷移コールバックのみ受け取る。
 */
@Composable
fun HomeScreen(
    onNavigateToCounter: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDogImage: () -> Unit,
) {
    HomeContent(
        onNavigateToCounter = onNavigateToCounter,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToDogImage = onNavigateToDogImage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    onNavigateToCounter: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDogImage: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("demo1") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MenuCard(
                title = "カウンター",
                description = "SQLDelight で永続化するカウンター",
                onClick = onNavigateToCounter,
            )
            MenuCard(
                title = "設定",
                description = "KVault でテーマ切替を保存",
                onClick = onNavigateToSettings,
            )
            MenuCard(
                title = "犬の画像",
                description = "Ktor で Dog CEO API から取得",
                onClick = onNavigateToDogImage,
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}