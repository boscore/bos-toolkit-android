package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class RequiredAuth(
        @Expose
        val threshold: Int,
        @Expose
        val keys: List<Key>,
        @Expose
        val accounts: List<EosAccount>,
        @Expose
        val waits: List<Int>
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.createTypedArrayList(Key.CREATOR),
            source.createTypedArrayList(EosAccount.CREATOR),
            ArrayList<Int>().apply { source.readList(this, Int::class.java.classLoader) }
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(threshold)
        writeTypedList(keys)
        writeTypedList(accounts)
        writeList(waits)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RequiredAuth> = object : Parcelable.Creator<RequiredAuth> {
            override fun createFromParcel(source: Parcel): RequiredAuth = RequiredAuth(source)
            override fun newArray(size: Int): Array<RequiredAuth?> = arrayOfNulls(size)
        }
    }
}