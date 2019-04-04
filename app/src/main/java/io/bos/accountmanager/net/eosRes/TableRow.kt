package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-7-1.
 */
class TableRow<T> constructor(
        @Expose val more: Boolean,
        @Expose val rows: List<T>) : EOSAbstractRes()