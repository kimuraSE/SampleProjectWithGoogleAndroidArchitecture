package com.sample.demo1.data.dogimage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Dog CEO API の薄いラッパー。
 *
 * Repository がネットワーク層の詳細（Ktor, エンドポイント URL）を知らなくて済むよう
 * このクラスで HTTP の事情を閉じ込める。
 */
internal class DogImageApi(private val httpClient: HttpClient) {

    suspend fun fetchRandom(): RandomDogDto =
        httpClient.get(URL_RANDOM).body()

    private companion object {
        const val URL_RANDOM = "https://dog.ceo/api/breeds/image/random"
    }
}
