package io.bos.accountmanager.data.local.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.reactivex.Flowable

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(accountTable: AccountTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(accounts: Array<AccountTable>): Array<Long>

    @Query("SELECT * FROM bos_account")
    fun getAll(): Flowable<List<AccountTable>>

    @Query("SELECT * FROM bos_account")
    fun getAllSync(): List<AccountTable>

    @Query("SELECT * FROM bos_account WHERE account_name = :account limit 1")
    fun getWithAccount(account: String): Flowable<AccountTable>

    @Query("SELECT * FROM bos_account WHERE account_name = :account limit 1")
    fun getWithAccountSync(account: String): AccountTable?

    @Query("SELECT COUNT(*) FROM bos_account")
    fun getAccountCount(): Long

    @Query("DELETE FROM bos_account WHERE account_name = :account")
    fun deleteAccount(account: String): Int
    @Query("UPDATE   bos_account  SET create_backup= :state  ")
    fun updateBackupsState(state:Boolean):Long
}