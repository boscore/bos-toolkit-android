package io.bos.accountmanager.net.bean

import android.text.TextUtils

class SecretKeyBean() {
    var publicKey: String = ""//公钥
    var privateKey: String = "" //私钥
    var accountName: String = "";//账户名称
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecretKeyBean

        if (!TextUtils.equals(publicKey, other.publicKey)) return false
        if (!TextUtils.equals(privateKey, other.privateKey)) return false
        if (!TextUtils.equals(accountName, other.accountName)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + privateKey.hashCode()
        result = 31 * result + accountName.hashCode()
        return result
    }

}