package com.sample.demo1.data.counter

/**
 * データモデル：アプリ内で重要な役割を持つデータを抽象化したもの。設計図とかしたもの
 * ポイントは3つ。
 *   ▎ 1つ目、data class + val で不変。 一度作ったら書き換えられない。
 *   ▎ 2つ目、init で『値は 0 以上』というドメインルールを守っている。 ViewModel じゃなくモデル自身が守る。だからどこから作られても安全。
 *   ▎ 3つ目、incremented のように過去分詞で命名し、新しいインスタンスを返す。
 *   ▎ ZERO はリセット用の定数。
 *
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
