package io.bos.accountmanager.presenter

import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.view.ExportKeytTipsView
import io.bos.accountmanager.view.SecurityView
import javax.inject.Inject

class ExportKeytTipsPresenter @Inject constructor() : AbstractPresenter<ExportKeytTipsView>() {

    @Inject
    lateinit var dataManager: DataManager


}