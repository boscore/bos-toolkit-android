package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose

data class PermissionX(
        @Expose
        val actor: String,
        @Expose
        val permission: String
) : Parcelable {
        constructor(source: Parcel) : this(
                source.readString(),
                source.readString()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
                writeString(actor)
                writeString(permission)
        }

        companion object {
                @JvmField
                val CREATOR: Parcelable.Creator<PermissionX> = object : Parcelable.Creator<PermissionX> {
                        override fun createFromParcel(source: Parcel): PermissionX = PermissionX(source)
                        override fun newArray(size: Int): Array<PermissionX?> = arrayOfNulls(size)
                }
        }
}