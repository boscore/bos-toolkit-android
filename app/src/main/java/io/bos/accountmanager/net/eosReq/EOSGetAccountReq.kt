package io.bos.accountmanager.net.eosReq

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-19.
 */
class EOSGetAccountReq constructor(
        @Expose val account_name: String
) : EOSAbstractRequest() {
    override fun getData(): Any {
        return this
    }
}