package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-19.
 */
class EOSGetKeyAccountRes(@Expose val account_names: List<String>) : EOSAbstractRes()