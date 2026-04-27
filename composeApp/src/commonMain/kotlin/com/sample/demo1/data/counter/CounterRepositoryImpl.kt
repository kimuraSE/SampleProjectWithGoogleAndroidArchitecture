package com.sample.demo1.data.counter

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.sample.demo1.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repositoryの具象クラス
 * インターフェイスに記載のあった詳細な処理を実装する。
 * DB操作を実際に行うデータソース層への橋渡し。
 *
 *
 */


class CounterRepositoryImpl(database: Database) : CounterRepository {

    private val queries = database.counterQueries

    override fun observe(): Flow<Counter> =
        queries.selectValue()
            .asFlow()
            .mapToOneOrDefault(defaultValue = 0L, context = Dispatchers.Default)
            .map { Counter(it.toInt()) }

    override suspend fun increment(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val next = currentCounter().incremented()
            queries.upsertValue(next.value.toLong())
            Unit
        }
    }

    override suspend fun decrement(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val next = currentCounter().decremented()
            queries.upsertValue(next.value.toLong())
            Unit
        }
    }

    override suspend fun reset(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            queries.upsertValue(Counter.ZERO.value.toLong())
            Unit
        }
    }

    private fun currentCounter(): Counter =
        Counter((queries.selectValue().executeAsOneOrNull() ?: 0L).toInt())
}
