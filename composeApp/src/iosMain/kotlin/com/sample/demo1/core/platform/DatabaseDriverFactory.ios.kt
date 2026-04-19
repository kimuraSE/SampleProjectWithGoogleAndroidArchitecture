package com.sample.demo1.core.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sample.demo1.db.Database

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(
            schema = Database.Schema,
            name = "demo1.db",
        )
}