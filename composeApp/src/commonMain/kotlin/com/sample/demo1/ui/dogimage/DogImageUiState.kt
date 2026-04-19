package com.sample.demo1.ui.dogimage

import com.sample.demo1.data.dogimage.DogImage

/**
 * DogImage 画面の UI State。
 *
 * Counter / Settings と異なり、**データ軸（キャッシュ画像）と状態軸（取得中・失敗）**が
 * 独立して共存し得るため、sealed ではなく data class で表現する。
 * 例：「キャッシュの犬画像を表示しながら、新しい画像を取得中」という状態がある。
 *
 * - [image]       : Repository 由来の SSOT。永続キャッシュされた最新の画像
 * - [isLoading]   : ViewModel の揮発状態。API 取得中かどうか
 * - [errorMessage]: ViewModel の揮発状態。直近の取得失敗メッセージ
 */
data class DogImageUiState(
    val image: DogImage? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
