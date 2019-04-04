package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


class RammarKet(
        @Expose
        val supply: String,
        @Expose
        val base: Base,
        @Expose
        val quote: Quote
) : EOSAbstractRes()