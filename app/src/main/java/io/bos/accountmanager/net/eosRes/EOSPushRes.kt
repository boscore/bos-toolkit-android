package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-21.
 */
class EOSPushRes constructor(@Expose val transaction_id: String) : EOSAbstractRes() {

}