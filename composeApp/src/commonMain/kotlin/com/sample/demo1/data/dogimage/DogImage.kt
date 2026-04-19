package com.sample.demo1.data.dogimage

/**
 * ランダム画像画面で扱う犬の画像を表すデータモデル。
 *
 * ドメインルール：url は空文字であってはならない。
 */
data class DogImage(val url: String) {
    init {
        if (url.isBlank()) throw InvalidDogImageUrlException(url)
    }
}

class InvalidDogImageUrlException(val attemptedUrl: String) : IllegalStateException(
    "DogImage url must not be blank, but was \"$attemptedUrl\""
)
