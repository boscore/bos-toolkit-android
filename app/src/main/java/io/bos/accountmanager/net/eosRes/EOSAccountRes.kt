package com.hconline.base.eos.eosRes

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose


data class EOSAccountRes(
        @Expose
        val account_name: String,
        @Expose
        val head_block_num: Int,
        @Expose
        val head_block_time: String,
        @Expose
        val privileged: Boolean,
        @Expose
        val last_code_update: String,
        @Expose
        val created: String,
        @Expose
        val core_liquid_balance: String,
        @Expose
        val ram_quota: Int,
        @Expose
        val net_weight: String,
        @Expose
        val cpu_weight: String,
        @Expose
        val net_limit: NetLimit,
        @Expose
        val cpu_limit: CpuLimit,
        @Expose
        val ram_usage: Int,
        @Expose
        val permissions: List<Permission>?,
        @Expose
        val total_resources: TotalResources?
        ,
        @Expose
        val self_delegated_bandwidth: SelfDelegatedBandwidth?,
        @Expose
        val refund_request: RefundRequest?,
        @Expose
        val voter_info: VoterInfo?
) : EOSAbstractRes(), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            1 == source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readParcelable<NetLimit>(NetLimit::class.java.classLoader),
            source.readParcelable<CpuLimit>(CpuLimit::class.java.classLoader),
            source.readInt(),
            source.createTypedArrayList(Permission.CREATOR),
            source.readParcelable<TotalResources>(TotalResources::class.java.classLoader),
            source.readParcelable<SelfDelegatedBandwidth>(SelfDelegatedBandwidth::class.java.classLoader),
            source.readParcelable<RefundRequest>(RefundRequest::class.java.classLoader),
            source.readParcelable<VoterInfo>(VoterInfo::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(account_name)
        writeInt(head_block_num)
        writeString(head_block_time)
        writeInt((if (privileged) 1 else 0))
        writeString(last_code_update)
        writeString(created)
        writeInt(ram_quota)
        writeString(net_weight)
        writeString(cpu_weight)
        writeParcelable(net_limit, 0)
        writeParcelable(cpu_limit, 0)
        writeInt(ram_usage)
        writeTypedList(permissions)
        writeParcelable(total_resources, 0)
        writeParcelable(self_delegated_bandwidth, 0)
        writeParcelable(refund_request, 0)
        writeParcelable(voter_info, 0)
    }
    fun getTotalResources():TotalResources{
        return if (total_resources == null)
        {
            return TotalResources("","0.0000 EOS","0.0000 EOS",0)
        }else{
            total_resources
        }
    }
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EOSAccountRes> = object : Parcelable.Creator<EOSAccountRes> {
            override fun createFromParcel(source: Parcel): EOSAccountRes = EOSAccountRes(source)
            override fun newArray(size: Int): Array<EOSAccountRes?> = arrayOfNulls(size)
        }
    }
}