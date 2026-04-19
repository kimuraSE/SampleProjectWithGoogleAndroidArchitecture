package com.sample.demo1.data.dogimage

import kotlinx.coroutines.flow.Flow

/**
 * 犬の画像の Single Source of Truth。
 *
 * 最後に取得した画像を KVault に永続キャッシュする。
 * [observe] が返す Flow が UI に流れる真の所有者。
 *
 * ネットワーク取得は [fetchRandom] で行い、成功時は内部でキャッシュを更新するため
 * [observe] の Flow に自動的に反映される。失敗はアプリを落とさないよう [Result] で包む。
 */
interface DogImageRepository {
    /** 永続キャッシュされた最新の画像を観測する。キャッシュが無ければ null。 */
    fun observe(): Flow<DogImage?>

    /** API から新しいランダム画像を取得し、成功時はキャッシュに保存する。 */
    suspend fun fetchRandom(): Result<Unit>
}
