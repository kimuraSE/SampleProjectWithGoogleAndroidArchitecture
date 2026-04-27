package com.sample.demo1.data.counter

import kotlinx.coroutines.flow.Flow

/**
 * カウンターにかかわるDB操作を宣言している。約束事
 * observe(): カウンターの値の最新を常に取得するためのメソッド
 * increment(): カウンターの値に＋１した値をDBに保存するメソッド
 * decrement(): 逆
 * reset(): カウンターの値を０にして保存するメソッド。
 */
interface CounterRepository {
    fun observe(): Flow<Counter>
    suspend fun increment(): Result<Unit>
    suspend fun decrement(): Result<Unit>
    suspend fun reset(): Result<Unit>
}
