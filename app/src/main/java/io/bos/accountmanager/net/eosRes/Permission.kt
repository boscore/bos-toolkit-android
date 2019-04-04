package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class Permission(
        @Expose
        val perm_name: String,
        @Expose
        val parent: String,
        @Expose
        val required_auth: RequiredAuth
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readParcelable<RequiredAuth>(RequiredAuth::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(perm_name)
        writeString(parent)
        writeParcelable(required_auth, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Permission> = object : Parcelable.Creator<Permission> {
            override fun createFromParcel(source: Parcel): Permission = Permission(source)
            override fun newArray(size: Int): Array<Permission?> = arrayOfNulls(size)
        }
    }
}