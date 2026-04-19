# demo1

**クリーンアーキテクチャ + MVVM + UDF** を Kotlin Multiplatform + Compose Multiplatform で学習するためのサンプルアプリ。

Android / iOS の両プラットフォームで動作する 4 画面構成のアプリを通じて、以下を体系的に扱う：

- **UI Layer**: Composable + ViewModel + UiState
- **Domain Layer**: Entity + Repository interface
- **Data Layer**: Repository impl + DataSource (DB / KVS / Network)
- **単方向データフロー (UDF)** と **Single Source of Truth (SSOT)**
- **永続モデル駆動** の UI（クリーンアーキテクチャ + MVVM + UDF §0.1）

---

## 画面構成

| 画面 | 学習テーマ | 使用ライブラリ |
| --- | --- | --- |
| **Home** | Navigation の基礎 / Stateless な導線画面 | Navigation Compose |
| **Counter** | 構造化データの永続化 / sealed UiState / ドメインルールと Result | **SQLDelight** |
| **Settings** | Key-Value 永続化 / テーマのグローバル反映 | **KVault** |
| **DogImage** | HTTP 通信 / 永続キャッシュ / 画像表示 | **Ktor + Coil 3** |

初期表示は Home。そこから各機能画面へ push 遷移する。

---

## アーキテクチャ

### レイヤー構成

```
com.sample.demo1
├── App.kt                    // ルート (KoinApplication + MaterialTheme + AppNavHost)
│
├── core/
│   ├── di/                   // Koin モジュール (appModule / platformModule)
│   ├── navigation/           // Screen sealed interface, AppNavHost
│   └── platform/             // expect/actual (DatabaseDriverFactory, HttpClientConfig)
│
├── domain/                   // 何にも依存しない純粋層
│   ├── counter/              // Counter, CounterRepository
│   ├── settings/             // ThemeMode, ThemeRepository
│   └── dogimage/             // DogImage, DogImageRepository
│
├── data/                     // Repository 実装 + DataSource 詳細
│   ├── counter/              // SQLDelight
│   ├── settings/             // KVault
│   └── dogimage/             // Ktor + KVault キャッシュ
│
└── ui/                       // Composable + ViewModel + UiState
    ├── home/
    ├── counter/
    ├── settings/
    └── dogimage/
```

### データの流れ (UDF)

```
状態 (State) の流れ              イベントの流れ
───────────────▶                ◀───────────────

DataSource (DB / KVS / Network)
     │ Flow                             ▲
     ▼                                  │ suspend fun
Repository                              │
     │ Flow<Domain>                     ▲
     ▼                                  │ 関数呼出
ViewModel (StateFlow<UiState>)
     │ State                            ▲
     ▼                                  │ コールバック
Composable (Stateful Screen → Stateless Content)
     │                                  ▲
     ▼                                  │
                 User
```

### 中核原則

1. **UI をデータモデルで操作する（永続モデル推奨）**
   - UI が表示するものはすべて Data Layer 由来
   - OS がアプリを破棄しても値を失わないよう DB / KVS に永続化
2. **Single Source of Truth**
   - 値の所有者は Repository / DataSource 一箇所
   - ViewModel は Flow を `stateIn` で導出するだけで独自保持しない
3. **単方向データフロー**
   - 状態は `Repository → ViewModel → Composable` の一方向に流れる
   - イベントは逆方向にコールバックで伝わる

詳しくは [docs/design.md](./docs/design.md) の §0 を参照。

---

## 技術スタック

| 区分 | 採用 | バージョン |
| --- | --- | --- |
| Kotlin | Kotlin Multiplatform | 2.3.20 |
| UI | Compose Multiplatform | 1.10.3 |
| DI | Koin (+ Koin Compose) | 4.1.0 |
| Navigation | Jetpack Navigation Compose (KMP) | 2.9.2 |
| 構造化永続化 | SQLDelight | 2.1.0 |
| KV 永続化 | KVault | 1.12.0 |
| HTTP 通信 | Ktor Client (OkHttp / Darwin) | 3.2.2 |
| 画像表示 | Coil 3 + coil-network-ktor3 | 3.3.0 |
| 非同期 | Kotlin Coroutines + Flow | 1.10.2 |
| シリアライズ | kotlinx.serialization | 1.8.1 |

---

## プロジェクト構成

- [`/composeApp`](./composeApp/src) — 共通 UI コード
  - [`commonMain`](./composeApp/src/commonMain/kotlin) — 全ターゲット共通のロジック / UI
  - [`androidMain`](./composeApp/src/androidMain/kotlin) — Android 固有の `actual` 実装
  - [`iosMain`](./composeApp/src/iosMain/kotlin) — iOS 固有の `actual` 実装
  - [`commonMain/sqldelight`](./composeApp/src/commonMain/sqldelight) — SQLDelight スキーマ (`.sq`)
- [`/iosApp`](./iosApp/iosApp) — iOS アプリのエントリポイント（Xcode プロジェクト）
- [`/docs`](./docs) — 要件定義・設計書
  - [`requirements.md`](./docs/requirements.md) — 要件定義
  - [`design.md`](./docs/design.md) — アーキテクチャ設計

---

## ビルドと実行

### Android

Android Studio で `composeApp` を開いて Run、またはターミナルから：

```shell
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug   # 端末/エミュレータにインストール
```

### iOS

`iosApp/iosApp.xcodeproj` を Xcode で開いて Run。

### 両プラットフォームの型チェック

```shell
./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64
```

---

## 動作確認ポイント

| 画面 | 確認項目 |
| --- | --- |
| **Counter** | +1 / -1 / Reset が動く。**値は 0 未満にならない**（ドメインルール）。アプリを完全終了→再起動しても値が保持される（SQLDelight 永続化） |
| **Settings** | ライト / ダーク / システム追従 を切り替えると**全画面の配色が即座に反映**。再起動後も選択は復元される（KVault 永続化） |
| **DogImage** | 起動時に自動取得。「別の画像にする」で再取得。再起動すると**直近の画像がキャッシュから即表示**される。機内モードでもキャッシュは表示される |

---

## 参考資料

- [Guide to app architecture](https://developer.android.com/topic/architecture) — Google 公式
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
