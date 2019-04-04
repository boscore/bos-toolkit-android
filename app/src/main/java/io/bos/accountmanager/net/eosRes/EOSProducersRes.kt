package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-22.
 */

data class EOSProducersRes(
        @Expose
        var rows: List<Row>,
        @Expose
        val total_producer_vote_weight: String,
        @Expose
        val more: String
) : EOSAbstractRes()

data class Row(
        @Expose
        val owner: String,
        @Expose
        val total_votes: String,
        @Expose
        val producer_key: String,
        @Expose
        val is_active: Int,
        @Expose
        val url: String,
        @Expose
        val unpaid_blocks: Int,
        @Expose
        val last_claim_time: Long,
        @Expose
        val location: Int,

        var rec: Boolean = false
) : Parcelable, Comparator<Row> {
    override fun equals(other: Any?): Boolean {
        if (other !is Row) {
            return false
        }
        return TextUtils.equals(owner, other.owner)
    }

          override fun compare(o1: Row?, o2: Row?): Int {
            if (o1 == null)
                return -1
            if (o1 == o2) {
                return 0
            }

            for (i in 0..11) {
                val o1Chat = o1.owner[i]
                val o2Chat = o2!!.owner[i]
                val d = o1Chat.compareTo(o2Chat)
                if (d == 0) {
                    continue
                }
            }

        return 0

    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readLong(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(owner)
        writeString(total_votes)
        writeString(producer_key)
        writeInt(is_active)
        writeString(url)
        writeInt(unpaid_blocks)
        writeLong(last_claim_time)
        writeInt(location)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Row> = object : Parcelable.Creator<Row> {
            override fun createFromParcel(source: Parcel): Row = Row(source)
            override fun newArray(size: Int): Array<Row?> = arrayOfNulls(size)
        }
    }
}