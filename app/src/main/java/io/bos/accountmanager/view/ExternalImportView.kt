package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.net.bean.CloudBean
import io.bos.accountmanager.net.bean.SecretKeyBean

interface ExternalImportView : AbstractView {

    fun getAccontList(cloudBean:ArrayList<CloudBean>){}

    fun exportPriavteList(date:  ArrayList<SecretKeyBean>){}


    fun  getSelect(cloudBean:CloudBean){}

}