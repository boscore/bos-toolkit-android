package io.bos.accountmanager.presenter

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.net.NetManager
import io.bos.accountmanager.net.bean.RedEnvelopeRecordBean
import io.bos.accountmanager.net.eosReq.BosTableRedEnvelopeRowReq
import io.bos.accountmanager.net.eosReq.EOSGetAccountReq
import io.bos.accountmanager.view.ExportKeytTipsView
import io.bos.accountmanager.view.RedRecordView
import io.bos.accountmanager.view.SecurityView
import retrofit2.http.Body
import javax.inject.Inject

class RedRecordPresenter @Inject constructor() : AbstractPresenter<RedRecordView>() {

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var netManager: NetManager


    fun getEstablishAccount() {
        addDisposable(
                dataManager.establishAccountDao.getAllEstablishAccount()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.getHistoryList(it)

                        }, {
                            getView()?.errLose(getView()?.context()!!.getString(R.string.red_record_err_lose))
                        })
        )
    }
    /**
     * 红包领取记录
     */
    fun getRedEnvelopeRecord(content:BosTableRedEnvelopeRowReq){
        addDisposable(
                netManager.eOSApiService.getRedEnvelopeRecoed(content)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            val list = ArrayList<RedEnvelopeRecordBean>()
                            val arrays = it.get("rows").asJsonArray
                            (0 until arrays.size())
                                    .map { Gson().fromJson(arrays[it].toString(),RedEnvelopeRecordBean::class.java) }
                                    .filterTo(list) { TextUtils.equals(it.sender,content.lower_bound) }
                            getView()?.getRedEnvelopeListSuccess(list)

                        }, {
                            getView()?.errLose(getView()?.context()!!.getString(R.string.get_red_envelope_fail))
                        })
        )
    }

}