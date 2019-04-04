package io.bos.accountmanager.presenter

import android.util.Log
import io.bos.accountmanager.Constants
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.view.MainView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService

import javax.inject.Inject

class MainPresenter @Inject constructor() : AbstractPresenter<MainView>() {
    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper

    override fun attachView(view: MainView) {
        super.attachView(view)
        accounts()
    }

    fun balance() {
        addDisposable(
                Run(object : Callback<Result> {
                    override fun call(): Result {
                        val bos = StartFactory.build(HttpService(Constants.Const.URL))
                        val data = dataManager.accountDao.getAllSync()
                        for (datum in data) {
                            try {
                                val balance = bos.balance("eosio.token", datum.accountName, "BOS").send()
                                if (!balance.isError) {
                                    val first = balance.data.first()
                                    datum.balance = first
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        dataManager.accountDao.insertAll(Array(data.size) {
                            data[it]
                        })
                        return Result(true)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .doOnComplete {
                            getView()?.onBalance()
                        }
                        .subscribe({

                        }, {

                        })
        )
    }

    private fun accounts() {
        addDisposable(
                dataManager.accountDao.getAll()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.onAccounts(it)
                        }, {
                            getView()?.onAccounts(emptyList())
                        })
        )
    }


    fun updateState(){
        addDisposable(
                Flowable.create<Long>({
                    it.onNext(dataManager.accountDao.updateBackupsState(true))
                    it.onComplete()
                }, BackpressureStrategy.ERROR)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            Log.i("TAG","=============="+it)

                        }, {

                        })
        )
    }


    fun getIsAccount() {
        addDisposable(
                Flowable.create<List<AccountTable>>({
                    it.onNext(dataManager.accountDao.getAllSync())
                    it.onComplete()
                }, BackpressureStrategy.ERROR)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            val isAccounts = ArrayList<AccountTable>()
                            for (i in 0 until it.size) {
                                if (it[i].create_backup == false) {
                                    isAccounts.add(it[i])
                                }

                            }
                          getView()?.getAccountIs(isAccounts)
                        }, {
                            getView()?.errAccount()
                        })
        )
    }


    class RunResult(val count: Long) : Result(true, "")

}