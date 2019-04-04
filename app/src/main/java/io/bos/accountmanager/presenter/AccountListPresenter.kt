package io.bos.accountmanager.presenter

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.view.AccountListView
import io.bos.accountmanager.view.ExportKeytTipsView
import io.bos.accountmanager.view.RedRecordView
import io.bos.accountmanager.view.SecurityView
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.net.StartEOS
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import java.util.*
import javax.inject.Inject

class AccountListPresenter @Inject constructor() : AbstractPresenter<AccountListView>() {

    @Inject
    lateinit var dataManager: DataManager

    private var starteos: StartEOS = StartFactory.build(HttpService(Constants.Const.URL))

    /**
     * 查询是否有该账号
     */
    fun getIsAccount(name: String) {

        addDisposable(
                dataManager.start.accountInfo(name).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            if (it.isError) {
                                getView()?.errAccount(getView()?.context()!!.getString(R.string.accunt_list_not))
                            } else {
                                getView()?.Accountumber()
                            }
                        }, {
                            //可以创建账号
                            getView()?.errAccount(it.message!!)
                        })

        )


    }

    //获取本地信息
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

    //删除本地账号
    fun deleteAccountName(id: Int) {
        Run(object : Callback<Result> {
            override fun call(): Result {
                var dd = dataManager.establishAccountDao.deleteAccount(id)
                return Result(true, "")
            }
        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.deleteSuccess(getView()?.context()!!.getString(R.string.account_list_delete_sessu))
                }, {
                    getView()?.errAccount(getView()?.context()!!.getString(R.string.account_list_delete_txt))
                })


    }


    //    开始导入================================

    //获取账户信息
    fun getAccount(publicKey: String) {
        addDisposable(
                starteos.getKeyAccounts(publicKey)
                        .rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            if (it.isError) {
                                Log.e("HaiChecker", Gson().toJson(it.error))
                                getView()?.errAccount(getView()?.context()?.resources!!.getString(R.string.get_account_error))
                            } else {
                                val list = ArrayList<String>()
                                list.addAll(it.account_names)
                                getView()?.onAccount(list)
                            }
                        }, {
                            Log.e("HaiChecker", it.message)
                            getView()?.errAccount(getView()?.context()?.resources!!.getString(R.string.get_account_error))
                        })
        )
    }


    //通过公钥获取账号名,并且获取所有的公钥已经权限
    fun getLocalAccount(account: ArrayList<String>) {
        addDisposable(Run(object : Callback<NewRedEnvelopesPresenter.LocalAccount> {
            override fun call(): NewRedEnvelopesPresenter.LocalAccount {
                val localAccount = NewRedEnvelopesPresenter.LocalAccount(true, "", ArrayList<Pair<String, AccountTable?>>())
                for (s in account) {
                    localAccount.account.add(Pair(s, dataManager.accountDao.getWithAccountSync(s)))
                }
                return localAccount
            }
        }).rxJava().observeOn(androidSchedulers).subscribeOn(ioSchedulers).subscribe({
            getAuthority(it.account)
//            getView()?.localAccount(it.account)
        }, {
            getView()?.localAccountError(if (it.message == null) "" else it.message!!)

        }))
    }


    //获取所有权限
    fun getAuthority(accountTables: ArrayList<Pair<String, AccountTable?>>) {
        var publicKey: ArrayList<String> = ArrayList<String>()
        addDisposable(
                Run(object : Callback<Rst> {
                    override fun call(): Rst {
                        for (i in 0 until accountTables.size) {

                            val listBaifen: ArrayList<PermissionsBean> = ArrayList<PermissionsBean>()
                            var account = dataManager.start.accountInfo(accountTables[i].first).send()
                            if (account.isError) {
                                return Rst(false, "", publicKey)
                            } else {

                                for (permission in account.permissions!!) {
                                    if (TextUtils.equals(permission.perm_name, "active") || TextUtils.equals(permission.perm_name, "owner")) {
                                        var permissionsBean = PermissionsBean()
                                        for (key in permission.required_auth.keys) {
                                            permissionsBean.perm_name = permission.perm_name
                                            permissionsBean.keys = key.key
                                            listBaifen.add(permissionsBean)
                                        }
                                    }
                                }
                                var json = Gson().toJson(listBaifen)
                                publicKey.add(json)
                            }
                        }
                        return Rst(true, "", publicKey)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.localAccount(accountTables, it.date)
                        }, {

                            getView()?.localAccountError(if (it.message == null) "" else it.message!!)
                        })
        )


    }


    /**
     * 写入数据
     *                                                                                                         已导入 -> 跳过加密，进入下一次
     *                                                                                                       ↗
     *                                                                   存在 -> 解密密文 -> 判断私钥是否导入
     *                                                              ↗                                        ↘
     *                                                            ↗                                            未导入 -> 写入Buffer -> 然后加密
     * 思路-> 拿取公钥下的账号名列表 -> 判断本地是否存在该账号名  ↘
     *                                                              不存在 -> 直接加密写入
     *
     */
    fun writeDB(accounts: ArrayList<Pair<String, AccountTable?>>, privateKey: EosPrivateKey, password: String, pubicKey: ArrayList<String>) {
        addDisposable(
                Run(object : Callback<ImportAccountPresenter.EnDataResult> {
                    override fun call(): ImportAccountPresenter.EnDataResult {
                        val pwd512 = Sha512.from(password.toByteArray())
                        val iv = PreferencesHelper.getIv(pwd512)
                        val privateKeyByte = privateKey.bytes
                        val data = Array<AccountTable>(accounts.size, init = {
                            val accountAndCipherText = accounts[it]
                            if (accountAndCipherText.second == null) {
                                val account = AccountTable()
                                val writer = Writer(255)
                                //Size
                                writer.putUint(1)
                                //PrivateSize
                                writer.putUint(privateKeyByte.size.toLong())
                                //PrivateKey
                                writer.putBytes(privateKeyByte)
                                val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                                account.balance = "0.0000 BOS"
                                account.cipherText = Base58.encode(enData)
                                account.accountName = accountAndCipherText.first
                                account.backup = false
                                account.accountPublic = pubicKey[it]
                                var publicKey: ArrayList<String> = ArrayList<String>()
                                publicKey.add(privateKey.publicKey.toString(Constants.Const.WALLETTYPE))
                                account.publicKey = Gson().toJson(publicKey)


                                return@Array account
                            } else {
                                val privateKeyList = ArrayList<ByteArray>()
                                val plain = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountAndCipherText.second!!.cipherText), iv)
                                        ?: return@Array accountAndCipherText.second!!
                                val reader = Reader(plain)
                                val size = reader.uInt
                                for (i in 0 until size) {
                                    val length = reader.uInt
                                    val privateBytes = reader.getBytes(length.toInt())
                                    privateKeyList.add(privateBytes)
                                }
                                if (privateKeyList.contains(privateKeyByte)) {
                                    return@Array accountAndCipherText.second!!
                                } else {
                                    privateKeyList.add(privateKeyByte)
                                    val writer = Writer(512)
                                    writer.putUint(privateKeyList.size.toLong())
                                    var publicKey: ArrayList<String> = ArrayList<String>()
                                    for (i in 0 until privateKeyList.size) {
                                        val bytes = privateKeyList[i]
                                        writer.putUint(bytes.size.toLong())
                                        writer.putBytes(bytes)
                                        publicKey.add(EosPrivateKey(bytes).publicKey.toString(Constants.Const.WALLETTYPE))
                                    }

                                    val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                                    accountAndCipherText.second!!.cipherText = Base58.encode(enData)
                                    accountAndCipherText.second!!.backup = false
                                    accountAndCipherText.second!!.publicKey = Gson().toJson(publicKey)
                                    return@Array accountAndCipherText.second!!
                                }
                            }
                        })
                        return ImportAccountPresenter.EnDataResult(true, "", data)
                    }
                }).rxJava()
                        .flatMap {
                            Run(object : Callback<Result> {
                                override fun call(): Result {
                                    dataManager.accountDao.insertAll(it.accounts)
                                    return Result(true, "")
                                }
                            }).rxJava()
                        }
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.importSuccess()
                        }, {
                            getView()?.importError()
                        })

        )
    }


    class Rst(success: Boolean, error: String, var date: ArrayList<String>) : Result(success, error) {}

 }