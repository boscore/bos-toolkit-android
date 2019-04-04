package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class SelfDelegatedBandwidth(
        @Expose
        var from: String,
        @Expose
        var to: String,
        @Expose
        var net_weight: String,
        @Expose
        var cpu_weight: String
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(from)
        writeString(to)
        writeString(net_weight)
        writeString(cpu_weight)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SelfDelegatedBandwidth> = object : Parcelable.Creator<SelfDelegatedBandwidth> {
            override fun createFromParcel(source: Parcel): SelfDelegatedBandwidth = SelfDelegatedBandwidth(source)
            override fun newArray(size: Int): Array<SelfDelegatedBandwidth?> = arrayOfNulls(size)
        }
    }
}