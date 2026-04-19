package com.sample.demo1.data.dogimage

import com.liftric.kvault.KVault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ktor で取得した画像 URL を KVault に永続キャッシュする Repository 実装。
 *
 * SSOT：内部で保持する [MutableStateFlow] が真の所有者。
 * ネットワークの成否に関わらず、UI へ流れる値は必ずこの Flow を通る。
 */
internal class DogImageRepositoryImpl(
    private val api: DogImageApi,
    private val kvault: KVault,
) : DogImageRepository {

    private val state = MutableStateFlow(readFromStorage())

    override fun observe(): Flow<DogImage?> = state.asStateFlow()

    override suspend fun fetchRandom(): Result<Unit> = runCatching {
        val dto = api.fetchRandom()
        val image = DogImage(dto.message)
        kvault.set(KEY_CACHED_URL, image.url)
        state.value = image
    }

    private fun readFromStorage(): DogImage? =
        kvault.string(KEY_CACHED_URL)?.takeIf { it.isNotBlank() }?.let(::DogImage)

    private companion object {
        const val KEY_CACHED_URL = "dog_image_cached_url"
    }
}
