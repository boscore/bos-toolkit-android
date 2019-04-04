package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class CpuLimit(
        @Expose
        val used: Int,
        @Expose
        val available: Long,
        @Expose
        val max: Long
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.readLong(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(used)
        writeLong(available)
        writeLong(max)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CpuLimit> = object : Parcelable.Creator<CpuLimit> {
            override fun createFromParcel(source: Parcel): CpuLimit = CpuLimit(source)
            override fun newArray(size: Int): Array<CpuLimit?> = arrayOfNulls(size)
        }
    }
}