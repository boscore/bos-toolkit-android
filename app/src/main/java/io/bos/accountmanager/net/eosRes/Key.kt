package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.Expose


data class Key(
        @Expose
        val key: String,
        @Expose
        val weight: Int
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(key)
        writeInt(weight)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Key) {
            return TextUtils.equals(other.key, key)
        }
        return super.equals(other)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Key> = object : Parcelable.Creator<Key> {
            override fun createFromParcel(source: Parcel): Key = Key(source)
            override fun newArray(size: Int): Array<Key?> = arrayOfNulls(size)
        }
    }
}