package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class RefundRequest(
        @Expose
        val owner: String,
        @Expose
        val request_time: String,
        @Expose
        val net_amount: String,
        @Expose
        val cpu_amount: String
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(owner)
        writeString(request_time)
        writeString(net_amount)
        writeString(cpu_amount)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RefundRequest> = object : Parcelable.Creator<RefundRequest> {
            override fun createFromParcel(source: Parcel): RefundRequest = RefundRequest(source)
            override fun newArray(size: Int): Array<RefundRequest?> = arrayOfNulls(size)
        }
    }
}