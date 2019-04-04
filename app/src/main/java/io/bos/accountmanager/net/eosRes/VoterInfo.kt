package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class VoterInfo(
        @Expose
        val owner: String,
        @Expose
        val proxy: String,
        @Expose
        val producers: List<String>,
        @Expose
        val staked: Int,
        @Expose
        val last_vote_weight: String,
        @Expose
        val proxied_vote_weight: String,
        @Expose
        val is_proxy: Int,
        @Expose
        val deferred_trx_id: Int,
        @Expose
        val last_unstake_time: String,
        @Expose
        val unstaking: String
) : Parcelable {
        constructor(source: Parcel) : this(
                source.readString(),
                source.readString(),
                source.createStringArrayList(),
                source.readInt(),
                source.readString(),
                source.readString(),
                source.readInt(),
                source.readInt(),
                source.readString(),
                source.readString()
        )

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
                writeString(owner)
                writeString(proxy)
                writeStringList(producers)
                writeInt(staked)
                writeString(last_vote_weight)
                writeString(proxied_vote_weight)
                writeInt(is_proxy)
                writeInt(deferred_trx_id)
                writeString(last_unstake_time)
                writeString(unstaking)
        }

        companion object {
                @JvmField
                val CREATOR: Parcelable.Creator<VoterInfo> = object : Parcelable.Creator<VoterInfo> {
                        override fun createFromParcel(source: Parcel): VoterInfo = VoterInfo(source)
                        override fun newArray(size: Int): Array<VoterInfo?> = arrayOfNulls(size)
                }
        }
}