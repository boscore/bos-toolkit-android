package io.bos.accountmanager.data.local.db.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "establish_account")
class EstablishAccountTable{
    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name = "id") // 定义对应的数据库的字段名成
    var id: Int=0;
    @ColumnInfo(name = "public_key")
    lateinit var publicKey:String//公钥
    @ColumnInfo(name = "private_key")
    lateinit var privateKey:String  //私钥
    @ColumnInfo(name = "account_name")
    lateinit var accountName:String ;//账户名称
    @ColumnInfo(name = "time")
    lateinit var time:String ;//保存时间
}