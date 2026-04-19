# demo1 要件定義書

## 1. プロジェクト目的

本プロジェクトは **Google 推奨の Android アーキテクチャ**（[Guide to app architecture](https://developer.android.com/topic/architecture)）を、Kotlin Multiplatform (Compose Multiplatform) の文脈で学習するためのサンプルアプリである。

- UI Layer (Composable + ViewModel + UI State)
- Domain Layer (UseCase、必要に応じて)
- Data Layer (Repository + DataSource)
- 単方向データフロー (UDF)

実用性よりも「主要ライブラリを典型的なユースケースで一通り触る」ことを優先する。

## 2. プラットフォーム / 技術スタック

| 区分 | 採用技術 |
| ---- | -------- |
| プラットフォーム | Android + iOS (Kotlin Multiplatform) |
| UI | Compose Multiplatform (Jetpack Compose) |
| アーキテクチャ | Google 推奨アーキテクチャ (UI State + ViewModel + Repository / UDF) |
| ローカル永続化 (構造化データ) | SQLDelight |
| ローカル永続化 (Key-Value) | KVault |
| ネットワーク通信 | Ktor Client |
| DI | (暫定) 手動 DI or Koin ※後続で決定 |
| Navigation | Compose Multiplatform Navigation ※後続で決定 |
| 非同期 | Kotlin Coroutines + Flow |

## 3. 画面構成

アプリは 4 画面で構成される。全画面 Compose + ViewModel + UI State で実装する。
初期表示は **Home 画面** で、そこから各機能画面へ遷移する。

### 3.1 ホーム画面 (Home)

- **学習テーマ**: Navigation による画面遷移、画面遷移イベントのコールバック設計
- **機能**:
  - アプリのエントリポイント（起動時の初期画面）
  - 3 つの機能画面 (Counter / Settings / Dog Image) への導線（カード or ボタン一覧）
  - 各項目タップで該当画面へ遷移
- **永続化**: なし

### 3.2 カウンター画面 (Counter)

- **学習テーマ**: SQLDelight によるローカル永続化、Repository 経由での UI State 更新
- **機能**:
  - 現在のカウント値を表示
  - `+1` / `-1` ボタンでカウント値を増減
  - `Reset` ボタンで 0 にリセット
  - アプリを再起動してもカウント値が保持される
- **永続化**: SQLDelight のテーブル `counter_value` に単一レコードで保持

### 3.3 設定画面 (Settings)

- **学習テーマ**: KVault による Key-Value 永続化、テーマ切り替えによる Compose の再構成
- **機能**:
  - ダークモード / ライトモード / システム追従 を切り替えるトグル
  - 選択値は KVault に永続化し、アプリ全体のテーマへ即時反映
- **永続化**: KVault のキー `theme_mode` に保持

### 3.4 ランダム画像画面 (Dog Image)

- **学習テーマ**: Ktor による HTTP 通信、非同期処理、ローディング/エラー状態の扱い
- **機能**:
  - 画面表示時に Dog CEO API からランダムな犬画像を 1 枚取得し表示
  - `Reload` ボタンで再取得
  - ローディング中はインジケータ、失敗時はエラーメッセージと再試行ボタン
- **API**: `https://dog.ceo/api/breeds/image/random`
- **レスポンス例**: `{"message": "https://images.dog.ceo/breeds/xxx/xxx.jpg", "status": "success"}`
- **認証**: 不要（API キー登録も不要）

## 4. ナビゲーション

- 起動時の初期画面は **Home**
- Home → Counter / Settings / Dog Image へ push 遷移
- 各機能画面にはシステム戻る or 画面内の戻るボタンで Home に戻る
- スタック構造: `Home` をルートとし、機能画面はその上に積まれる
- ※ 実装は Compose Multiplatform Navigation を採用予定（§10 未決事項参照）

## 5. アーキテクチャ統一ルール（Google 推奨）

Google 公式ガイドに沿った **単方向データフロー (UDF)** を全画面で踏襲する。

```
[ User event ]
     │
     ▼
Composable ─── 関数呼び出し ──▶ ViewModel
     ▲                             │
     │                             ▼
StateFlow<UiState>         Repository / UseCase
     ▲                             │
     └──────── 更新 ◀─────── DataSource (DB / KVS / Network)
```

- **UI State**: 画面が表示するすべての情報を持つ単一の immutable `data class` (`XxxUiState`)
- **ViewModel**: `StateFlow<XxxUiState>` を公開し、UI からのイベントを public 関数で受ける（例: `increment()`, `selectTheme(mode)`, `reload()`）。ビジネスロジックは Repository / UseCase に委譲
- **One-shot events** (Snackbar / Navigation 等): `Channel<Event>().receiveAsFlow()` で公開。State には保持しない
- **Composable**: `collectAsStateWithLifecycle()` で State を購読。最上位の Screen コンポーザブルから、下位の Stateless Content コンポーザブルへ State とコールバックを Hoisting で流す

## 6. レイヤー構成 (Google 推奨ガイド準拠)

```
ui/         (UI Layer)     Composable (Screen/Content), ViewModel, UiState, Event
domain/     (Domain Layer) UseCase, Model, Repository interface  ※今回は薄め
data/       (Data Layer)   Repository impl, DataSource (SQLDelight / KVault / Ktor), DTO
core/                       共通ユーティリティ, DI, テーマ, Navigation 共通
```

## 7. 非機能要件

- Android: minSdk は composeApp の既定値に準拠（後で確認）
- iOS: Compose Multiplatform の対応バージョンに準拠
- 文字列は英日どちらかで統一（初期版は日本語 UI）
- 鍵・トークンは `local.properties` 管理、.gitignore 済みであることを確認

## 8. 学習スコープ外 (今回はやらない)

- 認証 / ユーザー管理
- プッシュ通知
- CI / CD
- 複数端末同期・クラウド永続化
- 複雑なデザインシステム

## 9. 成果物

- 動作する Android アプリ
- 動作する iOS アプリ
- 各画面のアーキテクチャ構成図 / 説明コメント
- 本要件定義書および以降の設計メモ（`docs/` 配下）

## 10. 未決事項（次フェーズで詰める）

- DI ライブラリ: 手動 DI / Koin / Kodein いずれにするか
- Navigation: `compose-navigation` (Jetbrains) 採用可否
- Ktor の JSON シリアライザ (kotlinx.serialization) 構成の確定
- SQLDelight / KVault / Ktor のバージョン確定
- テスト方針（ViewModel / Repository の単体テストをどこまで書くか）
