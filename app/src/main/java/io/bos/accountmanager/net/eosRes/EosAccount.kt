package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose

data class EosAccount(
        @Expose
        val permission: PermissionX,
        @Expose
        val weight: Int
) : Parcelable {
        constructor(source: Parcel) : this(
                source.readParcelable<PermissionX>(PermissionX::class.java.classLoader)
                ,source.readInt()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
                writeParcelable(permission, 0)
                writeInt(weight)
        }

        companion object {
                @JvmField
                val CREATOR: Parcelable.Creator<EosAccount> = object : Parcelable.Creator<EosAccount> {
                        override fun createFromParcel(source: Parcel): EosAccount = EosAccount(source)
                        override fun newArray(size: Int): Array<EosAccount?> = arrayOfNulls(size)
                }
        }
}