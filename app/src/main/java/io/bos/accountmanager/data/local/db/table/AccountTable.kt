package io.bos.accountmanager.data.local.db.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "bos_account")
class AccountTable {
    @PrimaryKey
    @ColumnInfo(name = "account_name")
    lateinit var accountName: String

    @ColumnInfo(name = "cipher_text")
    lateinit var cipherText: String

    @ColumnInfo(name = "is_backup")
    var backup: Boolean = false    //是否云端备份

    @ColumnInfo(name = "balance")
    var balance: String = "0.0000 BOS"

    @ColumnInfo(name = "accountPublic")
    var accountPublic: String = ""  //保存所有的公钥名称

    @ColumnInfo(name = "publicKey")
    var publicKey: String = ""   //保存导入的公钥

    @ColumnInfo(name = "create_backup")
    var create_backup:Boolean=true

}