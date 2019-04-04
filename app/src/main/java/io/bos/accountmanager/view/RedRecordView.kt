package io.bos.accountmanager.view
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.bos.accountmanager.net.bean.RedEnvelopeRecordBean

interface RedRecordView : AbstractView {

    fun  getHistoryList(history:List<EstablishAccountTable>){}

    fun errLose(message:String){}

    /**
     * 红包领取记录列表
     */
    fun getRedEnvelopeListSuccess(list:ArrayList<RedEnvelopeRecordBean>){}
    fun getRedEnvelopeListFail(){}

}