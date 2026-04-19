package com.sample.demo1.domain.counter

/**
 * カウンターのドメインモデル。
 *
 * ドメインルール：**value は 0 以上でなければならない**。
 * この不変条件を `init` ブロックで強制し、違反時は
 * [CounterValueOutOfRangeException] をスローする。
 *
 * ドメイン層は例外をそのまま投げ、呼び出し側（Repository / UseCase 層）で
 * `runCatching` 等により [Result] に変換する責務分担とする。
 */
data class Counter(val value: Int) {

    init {
        if (value < 0) throw CounterValueOutOfRangeException(value)
    }

    fun incremented(): Counter = Counter(value + 1)

    fun decremented(): Counter = Counter(value - 1)

    companion object {
        val ZERO = Counter(0)
    }
}

/**
 * カウンター値がドメインルールに反した (< 0) 場合にスローされる例外。
 */
class CounterValueOutOfRangeException(val attemptedValue: Int) : IllegalStateException(
    "Counter value must be >= 0, but was $attemptedValue"
)