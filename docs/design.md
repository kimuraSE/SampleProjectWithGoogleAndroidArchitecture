# demo1 設計書

要件定義書 (`docs/requirements.md`) に基づくアーキテクチャ/実装設計。
アーキテクチャは **Google 推奨の Android App Architecture** (UI Layer / Domain Layer / Data Layer、単方向データフロー) を踏襲する。

## 0. 中核原則（忠実に守る）

Google 公式ガイドの以下の原則を本プロジェクトの実装における憲法として扱う。

### 0.1 UI をデータモデルで操作する（永続モデルを推奨）

- データモデルはアプリのデータを表し、UI やコンポーネントのライフサイクルから **独立** している
- OS がアプリプロセスを破棄しても失われないよう **可能な限り永続化** する
- ネットワーク不安定/不通時でも UI が動作し続けるよう、UI は常に "ローカルで保持しているデータ" を読む

→ 本プロジェクトでは：
- Counter 値は **SQLDelight** に永続化、Settings のテーマは **KVault** に永続化
- DogImage は最後に取得した画像 URL を **KVault に永続キャッシュ** し、オフライン起動時も直近の画像を表示
- 揮発状態（ローディング/エラー）以外の表示データはすべて Data Layer 由来

### 0.2 Single Source of Truth (SSOT)

- アプリ内データは **Repository / DataSource が唯一の所有者**
- ViewModel / UI は Repository の `Flow` を購読するだけで、状態を独自に二重保持しない
- `StateFlow<UiState>` は Repository の Flow を `map`/`combine`/`stateIn` で導出したもの

### 0.3 単方向データフロー (UDF)

状態は **一方向** に流れ、イベントは **逆方向** に流れる：

```
  状態 (State) の流れ               イベントの流れ
  ───────────────▶                 ◀───────────────

  DataSource (DB/KVS/Network)
       │ Flow                              ▲
       ▼                                   │ suspend fun 呼出
  Repository
       │ Flow                              ▲
       ▼                                   │ 関数呼出
  ViewModel (StateFlow<UiState>)
       │ State                             ▲
       ▼                                   │ コールバック
  Screen Composable (Stateful)
       │ props                             ▲
       ▼                                   │ lambda
  Content Composable (Stateless)
       │ 表示                              ▲
       ▼                                   │
  User ────────────── 操作 ─────────────────┘
```

実装ルール：
- `Screen` は State を購読しつつ **ViewModel の関数** をコールバックとして Content に渡す
- `Content` は Stateless（UiState と lambda のみ受ける）
- ViewModel は直接 DataSource に触れず **Repository 経由**
- 状態の所有者を常に 1 箇所に保ち、分岐の起点を明確にする

## 1. 技術スタック（確定）

| 区分 | 採用技術 |
| ---- | -------- |
| プラットフォーム | Kotlin Multiplatform (Android / iOS) |
| UI | Compose Multiplatform 1.10.3 |
| Kotlin | 2.3.20 |
| DI | **Koin** (`io.insert-koin:koin-core` + `koin-compose`) |
| Navigation | **Jetpack Navigation Compose (KMP版)** (`org.jetbrains.androidx.navigation:navigation-compose`) |
| 永続化 (構造化) | **SQLDelight** (`app.cash.sqldelight`) |
| 永続化 (Key-Value) | **KVault** (`com.liftric:kvault`) |
| HTTP通信 | **Ktor Client** + `ContentNegotiation` + `kotlinx.serialization` |
| 非同期 | Kotlin Coroutines + Flow / StateFlow |
| ViewModel | `androidx.lifecycle.ViewModel` (KMP版、既存導入済) |

## 2. レイヤー / パッケージ構成

```
com.sample.demo1
├── App.kt                       // ルート Composable (Navigation + Koin 起動)
├── MainViewController.kt        // iOS エントリ (既存)
│
├── core/
│   ├── di/                      // Koin モジュール集約 (appModule)
│   ├── navigation/              // Screen sealed class, NavGraph
│   └── theme/                   // AppTheme, Colors, Typography
│
├── ui/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt       // UiState (+ 必要なら Event)
│   ├── counter/
│   │   ├── CounterScreen.kt
│   │   ├── CounterViewModel.kt
│   │   └── CounterUiState.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsUiState.kt
│   └── dogimage/
│       ├── DogImageScreen.kt
│       ├── DogImageViewModel.kt
│       └── DogImageUiState.kt
│
├── domain/
│   ├── model/                   // Counter, ThemeMode, DogImage
│   ├── repository/              // interface のみ
│   └── usecase/                 // (任意) 1 画面 1 機能なので薄め
│
└── data/
    ├── local/
    │   ├── counter/             // SQLDelight 生成コード利用の Repository impl
    │   └── settings/            // KVault 利用の Repository impl
    ├── remote/
    │   └── dogimage/            // Ktor 利用の Repository impl + DTO
    └── di/                      // data モジュール (repository バインド)
```

- `commonMain` 配下に上記すべてを配置。プラットフォーム固有実装は `expect/actual` のみ。
- ディレクトリは最初から全部作らず、画面実装時に増やす（過剰スキャフォールドを避ける）。

### expect / actual が必要な箇所

| 対象 | 理由 |
| ---- | ---- |
| `SqlDriver` ファクトリ | Android (`AndroidSqliteDriver`) / iOS (`NativeSqliteDriver`) |
| `KVault` の生成 | Android (`Context` 必要) / iOS (`NSUserDefaults` 経由) |

→ `core/platform/` に `expect fun provideSqlDriver(): SqlDriver` 等を置き、`androidMain/iosMain` で `actual` 実装。

## 3. UI Layer ひな形（Google 推奨）

### 3.1 UI State

画面が表示するすべての情報を単一の immutable `data class` にまとめる（Google 公式では "UI state as a data class" として推奨）。

```kotlin
// ui/counter/CounterUiState.kt
data class CounterUiState(
    val value: Int = 0,
    val isLoading: Boolean = true,
)
```

### 3.2 ViewModel

- `StateFlow<XxxUiState>` で State を公開
- UI からのユーザー操作は ViewModel の **public 関数** として受ける（`increment()`, `selectTheme(mode)` 等）
- One-shot event (Snackbar 表示、ナビゲーション発火など) は `Channel` → `Flow` で公開（UI State には入れない）

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : ViewModel() {

    val uiState: StateFlow<CounterUiState> =
        repository.observe()
            .map { CounterUiState(value = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CounterUiState(isLoading = true),
            )

    fun increment() = viewModelScope.launch { repository.increment() }
    fun decrement() = viewModelScope.launch { repository.decrement() }
    fun reset()     = viewModelScope.launch { repository.reset() }
}
```

### 3.3 Screen / Content テンプレート

```kotlin
// Stateful (VM 所有)
@Composable
fun CounterScreen(viewModel: CounterViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CounterContent(
        uiState = uiState,
        onIncrement = viewModel::increment,
        onDecrement = viewModel::decrement,
        onReset = viewModel::reset,
    )
}

// Stateless (Preview/Test 可)
@Composable
fun CounterContent(
    uiState: CounterUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
) { /* ... */ }
```

- `Screen` = State 購読 + コールバック配線のみ
- `Content` = Stateless Composable（Preview / Test 容易）
- One-shot event がある画面では `LaunchedEffect(Unit) { viewModel.events.collect { ... } }` で受ける

## 4. 各画面の UiState / ViewModel 契約

### 4.1 Home

```kotlin
// Home は State を持たない単純なハブ画面。UiState 定義不要。
class HomeViewModel : ViewModel()
```

遷移処理は Screen の外側（`AppNavHost`）でコールバックを渡して実行する（§7 参照）。
Home に ViewModel が不要な場合は省略しても良い。

### 4.2 Counter

```kotlin
data class CounterUiState(
    val value: Int = 0,
    val isLoading: Boolean = true,
)

class CounterViewModel(
    private val repository: CounterRepository,
) : ViewModel() {
    val uiState: StateFlow<CounterUiState>  // §3.2 参照
    fun increment()
    fun decrement()
    fun reset()
}
```

- `CounterRepository.observe(): Flow<Int>` を `stateIn` で `StateFlow` 化（Single Source of Truth）
- 操作はすべて Repository 経由で永続化

### 4.3 Settings

```kotlin
enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class SettingsViewModel(
    private val repository: ThemeRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState>
    fun selectTheme(mode: ThemeMode)
}
```

- `ThemeRepository` は `Flow<ThemeMode>` を公開
- `App.kt` では別途 `ThemeRepository.observe()` を購読して `MaterialTheme` の `colorScheme` を切り替える（グローバル反映）

### 4.4 DogImage

```kotlin
data class DogImageUiState(
    val imageUrl: String? = null,     // Repository (永続キャッシュ) 由来
    val isLoading: Boolean = false,   // 揮発：取得中フラグのみ ViewModel 所有
    val errorMessage: String? = null, // 揮発：直近の取得エラー
)

class DogImageViewModel(
    private val repository: DogImageRepository,
) : ViewModel() {
    val uiState: StateFlow<DogImageUiState>
    fun reload()
}
```

- §0.1「永続モデル推奨」に従い、**Repository は最後に取得した画像 URL を KVault に永続キャッシュ**する
- 画面の `imageUrl` は常に `repository.observeCachedUrl(): Flow<String?>` から導出（SSOT）
- `reload()` は：
  1. `isLoading = true` に切替
  2. `repository.fetchRandom()` 成功 → 内部で KVault に保存 → Flow 経由で UI に反映
  3. 失敗 → `errorMessage` をセット、既存の `imageUrl` は保持（ネットワーク不通時もキャッシュを表示）
- 初回起動時にキャッシュが空で、ネットワークも失敗した場合のみ "画像なし" UI を出す

## 5. データ層

### 5.1 Counter (SQLDelight)

```sql
-- composeApp/src/commonMain/sqldelight/com/sample/demo1/db/Counter.sq
CREATE TABLE CounterEntity (
    id INTEGER NOT NULL PRIMARY KEY,
    value INTEGER NOT NULL
);

selectValue:
SELECT value FROM CounterEntity WHERE id = 1;

upsertValue:
INSERT OR REPLACE INTO CounterEntity(id, value) VALUES (1, ?);
```

```kotlin
interface CounterRepository {
    fun observe(): Flow<Int>
    suspend fun increment()
    suspend fun decrement()
    suspend fun reset()
}
```

### 5.2 Settings (KVault)

```kotlin
interface ThemeRepository {
    fun observe(): Flow<ThemeMode>
    suspend fun setMode(mode: ThemeMode)
}
```

- KVault は Flow を持たないので、`MutableStateFlow` を Repository 内で保持し、書込時に `emit` する。

### 5.3 DogImage (Ktor + KVault キャッシュ)

```kotlin
@Serializable
data class RandomDogDto(val message: String, val status: String)

interface DogImageRepository {
    /** 永続キャッシュされた最新の画像URLを観測する（SSOT） */
    fun observeCachedUrl(): Flow<String?>

    /** APIから新しい画像を取得し、成功時はキャッシュに保存する */
    suspend fun fetchRandom(): Result<Unit>
}
```

- 実装: `DogImageRepositoryImpl(private val api: DogApi, private val cache: KVault)`
  - 内部で `MutableStateFlow<String?>` を保持し、起動時に `KVault.string("last_dog_image_url")` で復元
  - `fetchRandom()` 成功時に KVault へ保存 + Flow を更新
- Ktor `HttpClient` は Android (`OkHttp`) / iOS (`Darwin`) を expect/actual で分岐
- `ContentNegotiation(Json)` で DTO デシリアライズ

**データモデルの観点**: ViewModel は `observeCachedUrl()` の結果を UiState に流すだけ。ネットワーク成功/失敗は `fetchRandom()` の `Result` を観察して `isLoading` / `errorMessage` を更新するのみで、`imageUrl` 自体は必ず Data Layer から来る。

## 6. DI 構成 (Koin)

```kotlin
// core/di/AppModule.kt
val appModule = module {
    // data sources
    single<SqlDriver> { provideSqlDriver() }
    single { Database(get()) }
    single<KVault> { provideKVault() }
    single { provideHttpClient() }

    // repositories
    single<CounterRepository> { CounterRepositoryImpl(get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<DogImageRepository> { DogImageRepositoryImpl(get()) }

    // viewmodels
    viewModel { HomeViewModel() }
    viewModel { CounterViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { DogImageViewModel(get()) }
}
```

- Android: `Application.onCreate()` で `startKoin { modules(appModule) }`
- iOS: `MainViewController` 初期化時に同様。
- Composable 内で `koinViewModel<T>()` で取得。

## 7. Navigation

```kotlin
sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Counter : Screen
    @Serializable data object Settings : Screen
    @Serializable data object DogImage : Screen
}
```

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToCounter = { navController.navigate(Screen.Counter) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onNavigateToDogImage = { navController.navigate(Screen.DogImage) },
            )
        }
        composable<Screen.Counter> { CounterScreen() }
        composable<Screen.Settings> { SettingsScreen() }
        composable<Screen.DogImage> { DogImageScreen() }
    }
}
```

- Home からの遷移は Screen に渡したコールバックで `navController.navigate(...)` を呼ぶ直接方式。ViewModel に Event チャネルは設けない（UDF の最小構成）。

## 8. Gradle 依存追加方針

`gradle/libs.versions.toml` に以下を追記予定（バージョンは導入時点で確定）。

```toml
[versions]
koin = "4.x.x"
koinComposeMultiplatform = "4.x.x"
navigation = "2.9.0-alpha18"        # KMP 対応版（確定時に更新）
sqldelight = "2.x.x"
kvault = "1.12.0"
ktor = "3.x.x"
kotlinxSerialization = "1.x.x"
kotlinxCoroutines = "1.x.x"

[libraries]
# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koinComposeMultiplatform" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koinComposeMultiplatform" }

# Navigation
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }

# SQLDelight
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

# KVault
kvault = { module = "com.liftric:kvault", version.ref = "kvault" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# Serialization / Coroutines
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

`composeApp/build.gradle.kts` の `sourceSets`:
- **commonMain**: koin-core, koin-compose, koin-compose-viewmodel, navigation-compose, sqldelight-runtime + coroutines-extensions, kvault, ktor-client-core + content-negotiation + serialization-json, kotlinx-serialization-json, kotlinx-coroutines-core
- **androidMain**: sqldelight-android, ktor-client-okhttp
- **iosMain**: sqldelight-native, ktor-client-darwin

SQLDelight 用プラグイン設定（commonMain に `.sq` を置く場合）:
```kotlin
sqldelight {
    databases {
        create("Database") {
            packageName.set("com.sample.demo1.db")
        }
    }
}
```

## 9. 画像表示ライブラリ

Ktor で取得した URL を Compose で表示するため **Coil 3** (KMP対応) を採用:
```
coil3-compose, coil3-network-ktor
```

## 10. テスト方針（最小）

- **ViewModel の単体テスト** を `commonTest` に記述。public 関数呼び出し → `StateFlow<UiState>` の遷移を検証（`kotlinx-coroutines-test` の `runTest` + fake Repository）。
- Repository は interface 化済なのでテスト時は fake 実装を注入。
- 画面テストは最小限（Screen の最上位の表示分岐確認のみ、必要に応じて）。

## 11. 実装順序（提案）

1. **Gradle 依存追加** と SQLDelight プラグイン設定
2. **core/di** Koin 起動 + **core/navigation** ルート定義
3. **Home 画面**（Navigation の動作確認まで）
4. **Counter 画面**（SQLDelight 統合）
5. **Settings 画面**（KVault 統合 + Theme 切替）
6. **Dog Image 画面**（Ktor + Coil 統合）
7. 各画面 ViewModel テスト追加

## 12. 未決事項（実装時に確定）

- 各ライブラリの具体的バージョン（導入時に最新安定版を libs.versions.toml に記録）
- 画像表示の Coil3 のサイズ指定/プレースホルダ方針
- Navigation 時の Top App Bar 戻るボタン共通化の有無