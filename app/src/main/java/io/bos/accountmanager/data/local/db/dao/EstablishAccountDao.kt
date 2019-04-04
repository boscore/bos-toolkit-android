package io.bos.accountmanager.data.local.db.dao
import android.arch.persistence.room.*
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.reactivex.Flowable


@Dao
interface EstablishAccountDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEstablishAccount(establishAccountTable: EstablishAccountTable): Long

    @Query("SELECT * FROM establish_account order by id desc")
    fun getAllEstablishAccount(): Flowable<List<EstablishAccountTable>>

    @Query(" DELETE FROM establish_account where id = :id")
    fun deleteAccount( id:Int): Int

    @Query(" SELECT COUNT(*) FROM establish_account where account_name = :name")
    fun getNameNumber( name:String): Int

}
