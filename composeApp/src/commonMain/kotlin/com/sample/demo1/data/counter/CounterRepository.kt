package com.sample.demo1.data.counter

import kotlinx.coroutines.flow.Flow

/**
 * カウンターの Single Source of Truth。
 *
 * UI / ViewModel はこの Repository の Flow から [Counter] を読み取るのみ。
 * 値の所有はこの Repository が担う。
 *
 * 更新系メソッドは [Result] を返し、ドメインルール違反 ([CounterValueOutOfRangeException]) が
 * 起きてもアプリが落ちないようにしている。
 */
interface CounterRepository {
    fun observe(): Flow<Counter>
    suspend fun increment(): Result<Unit>
    suspend fun decrement(): Result<Unit>
    suspend fun reset(): Result<Unit>
}
