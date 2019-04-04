package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class TotalResources(
        @Expose
        val owner: String,
        @Expose
        val net_weight: String?,
        @Expose
        val cpu_weight: String,
        @Expose
        val ram_bytes: Int
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(owner)
        writeString(net_weight)
        writeString(cpu_weight)
        writeInt(ram_bytes)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TotalResources> = object : Parcelable.Creator<TotalResources> {
            override fun createFromParcel(source: Parcel): TotalResources = TotalResources(source)
            override fun newArray(size: Int): Array<TotalResources?> = arrayOfNulls(size)
        }
    }
}