package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-20.
 */

data class GetRequiredKeysRes(@Expose val required_keys: List<String>) : EOSAbstractRes()