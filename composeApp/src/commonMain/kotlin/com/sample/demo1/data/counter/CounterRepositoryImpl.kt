package com.sample.demo1.data.counter

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.sample.demo1.db.Database
import com.sample.demo1.domain.counter.Counter
import com.sample.demo1.domain.counter.CounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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