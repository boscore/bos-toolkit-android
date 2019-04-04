package io.bos.accountmanager.data.local.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import io.bos.accountmanager.data.local.db.dao.AccountDao
import io.bos.accountmanager.data.local.db.dao.EstablishAccountDao
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable

@Database(version = 1, entities = [AccountTable::class,EstablishAccountTable::class] )
abstract class DataBase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun establishAccount(): EstablishAccountDao
}