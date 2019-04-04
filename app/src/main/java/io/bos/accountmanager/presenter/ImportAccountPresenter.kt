package io.bos.accountmanager.presenter

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.bos.accountmanager.Constants
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.view.ImportAccountView
import io.reactivex.disposables.Disposable
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.net.StartEOS
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import org.json.JSONArray
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ImportAccountPresenter @Inject constructor() : AbstractPresenter<ImportAccountView>() {

    @Inject
    lateinit var dataManager: DataManager
    private var starteos: StartEOS = StartFactory.build(HttpService(Constants.Const.URL))
    fun getAccount(publicKey: String) {
        addDisposable(
                starteos.getKeyAccounts(publicKey)
                        .rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            if (it.isError) {
                                Log.e("HaiChecker", Gson().toJson(it.error))
                                getView()?.onAccountError()
                            } else {
                                val list = ArrayList<String>()
                                list.addAll(it.account_names)
                                getView()?.onAccount(list)
                            }
                        }, {
                            Log.e("HaiChecker", it.message)
                            getView()?.onAccountError()
                        })
        )
    }

    fun getLocalAccount(account: ArrayList<String>) {
        addDisposable(Run(object : Callback<LocalAccount> {
            override fun call(): LocalAccount {
                val localAccount = LocalAccount(true, "", ArrayList<Pair<String, AccountTable?>>())
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
//             "添加错误详情"
        }))
    }
    fun addDisposable2(disposable: Disposable) {
        super.addDisposable(disposable)
    }


    //获取所有权限
    fun getAuthority(accountTables: ArrayList<Pair<String, AccountTable?>>) {
        var publicKey:  ArrayList<String> = ArrayList<String>()

        addDisposable(
                Run(object : Callback<Rst> {
                    override fun call(): Rst {
                        for (i in 0 until accountTables.size) {

                            val listBaifen: ArrayList<PermissionsBean> = ArrayList<PermissionsBean>()
                            var account = dataManager.start.accountInfo(accountTables[i].first).send()
                            if (account.isError) {
                                return Rst(false, "",publicKey)
                            } else {

                                for (permission in account.permissions!!) {
//                                    if (TextUtils.equals(permission.perm_name, "active") || TextUtils.equals(permission.perm_name, "owner")) {
                                        var permissionsBean = PermissionsBean()
                                        for (key in permission.required_auth.keys) {
                                            permissionsBean.perm_name = permission.perm_name
                                            permissionsBean.keys = key.key
                                            listBaifen.add(permissionsBean)

                                        }
//                                    }

                                }
                                var json = Gson().toJson(listBaifen)
                                publicKey.add(json)
                            }

                        }
                        return Rst(true, "",publicKey)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.localAccount(accountTables,it.date)
                        }, {

                            getView()?.localAccountError(if (it.message == null) "" else it.message!!)
                        })
        )


    }

    class Rst(success: Boolean, error: String,var date:ArrayList<String>) : Result(success, error) {

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
    fun writeDB(accounts: ArrayList<Pair<String, AccountTable?>>, privateKey: EosPrivateKey, password: String, pubicKey:ArrayList<String>) {
        addDisposable(
                Run(object : Callback<EnDataResult> {
                    override fun call(): EnDataResult {
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
                                account.create_backup=true
                                account.accountPublic=pubicKey[it]
                                var publicKey:  ArrayList<String> = ArrayList<String>()
                                publicKey.add(privateKey.publicKey.toString(Constants.Const.WALLETTYPE))
                                account.publicKey=Gson().toJson(publicKey)


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
                                    if (!privateKeyList.contains(privateBytes)) {
                                        privateKeyList.add(privateBytes)
                                    }
                                }
                                for (bytes in privateKeyList) {
                                    if (Arrays.equals(bytes,privateKeyByte))
                                    {
                                        return@Array accountAndCipherText.second!!
                                    }
                                }

                                    privateKeyList.add(privateKeyByte)
                                    val writer = Writer(512)
                                    writer.putUint(privateKeyList.size.toLong())
                                    var publicKey:  ArrayList<String> = ArrayList<String>()
                                    for (i in 0 until privateKeyList.size)
                                    {
                                        val bytes = privateKeyList[i]
                                        writer.putUint(bytes.size.toLong())
                                        writer.putBytes(bytes)
                                        publicKey.add( EosPrivateKey(bytes).publicKey.toString())
                                    }

                                    val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                                    accountAndCipherText.second!!.cipherText = Base58.encode(enData)
                                    accountAndCipherText.second!!.backup = false
                                    accountAndCipherText.second!!.create_backup=true
                                    accountAndCipherText.second!!.publicKey=Gson().toJson(publicKey)
                                    return@Array accountAndCipherText.second!!

                            }
                        })

                        if(accounts.isEmpty()||accounts.size<=0){
                            return EnDataResult(false, "", data)
                        }else{
                            return EnDataResult(true, "", data)
                        }

                    }
                }).rxJava()
                        .flatMap {
                            Run(object : Callback<Result> {
                                override fun call(): Result {
                                    if(it.success){
                                        dataManager.accountDao.insertAll(it.accounts)
                                        return Result(true, "")
                                    }else{
                                        return Result(false, "")
                                    }


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

    class LocalAccount(success: Boolean, error: String, val account: ArrayList<Pair<String, AccountTable?>>) : Result(success, error)
    class EnDataResult(success: Boolean, error: String, var accounts: Array<AccountTable> = emptyArray()) : Result(success, error)

}