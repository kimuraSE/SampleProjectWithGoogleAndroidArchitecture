package com.sample.demo1.data.dogimage

import kotlinx.serialization.Serializable

/**
 * Dog CEO API のランダム画像エンドポイントのレスポンス DTO。
 *
 * 例:
 * ```json
 * { "message": "https://images.dog.ceo/breeds/xxx/xxx.jpg", "status": "success" }
 * ```
 */
@Serializable
internal data class RandomDogDto(
    val message: String,
    val status: String,
)
