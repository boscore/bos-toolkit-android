package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class NetLimit(
        @Expose
        val used: Long,

        @Expose
        val available: Long,
        @Expose
        val max: Long
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readLong(),
            source.readLong(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(used)
        writeLong(available)
        writeLong(max)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<NetLimit> = object : Parcelable.Creator<NetLimit> {
            override fun createFromParcel(source: Parcel): NetLimit = NetLimit(source)
            override fun newArray(size: Int): Array<NetLimit?> = arrayOfNulls(size)
        }
    }
}