package io.bos.accountmanager.presenter

import android.annotation.SuppressLint
import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import io.bos.accountmanager.Constants


import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.core.storage.OneDrive
import io.bos.accountmanager.core.storage.StorageFactory
import io.bos.accountmanager.core.storage.core.response.LoginResponse
import io.bos.accountmanager.core.storage.core.response.UploadResponse
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.NetManager
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.ui.dialog.PwdDialogCloud


import io.bos.accountmanager.view.WalletManageView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.starteos.jeos.Name
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import io.starteos.jeos.raw.core.Action
import io.starteos.jeos.raw.core.TypeAuthority
import io.starteos.jeos.raw.core.TypeKeyWeight
import io.starteos.jeos.raw.core.TypePublicKey
import io.starteos.jeos.transaction.SignedTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class WalletManagePresenter @Inject constructor() : AbstractPresenter<WalletManageView>() {
    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var netManager: NetManager
    @Inject
    lateinit var shared: PreferencesHelper

    //获取所有权限
    fun getAuthority(name: String) {
        addDisposable(

                dataManager.start.accountInfo(name).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            val listBaifen: ArrayList<PermissionsBean> = ArrayList<PermissionsBean>()
                            if (it.isError) {
                                getView()?.getErrAuthority(it.error!!.what)
                            } else {
                                for (permission in it.permissions!!) {

                                    if (TextUtils.equals(permission.perm_name, "active") || TextUtils.equals(permission.perm_name, "owner")) {
                                        var permissionsBean = PermissionsBean()
                                        for (key in permission.required_auth.keys) {
                                            permissionsBean.perm_name = permission.perm_name
                                            permissionsBean.keys = key.key
                                            listBaifen.add(permissionsBean)
                                        }

                                    }
                                }
                                getView()?.getAuthorityList(listBaifen)
                            }
                        }, {

                            getView()?.getErrAuthority(getView()?.context()?.resources!!.getString(R.string.err_txt_network))
                        })

        )


    }

    //获取账号信息
    fun accounts(name: String) {
        addDisposable(
                dataManager.accountDao.getWithAccount(name)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.onAccounts(it)
                        }, {
                            getView()?.errMessage(getView()?.context()?.getString(R.string.wallet_txt_err_name)!!)
                        })
        )
    }

    //导出keyStore列表
    fun exportKeyStoreDate(pwd: String, accountTable: AccountTable) {

        addDisposable(Run(object : Callback<WalletManagePresenter.EnDataResult> {
            override fun call(): WalletManagePresenter.EnDataResult {
                val pwd512 = Sha512.from(pwd.toByteArray())
                //解密
                var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
                val privateKeys = ArrayList<EosPrivateKey>()
                var listKey = ArrayList<SecretKeyBean>()
                val reader = Reader(decrypt)
                val size = reader.uInt
                for (i in 0 until size) {
                    val length = reader.uInt
                    val privateBytes = reader.getBytes(length.toInt())
                    privateKeys.add(EosPrivateKey(privateBytes))
                }
                //获取所有公钥名称
                var list = Gson().fromJson<java.util.ArrayList<PermissionsBean>>(accountTable?.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)
                for (i in 0 until privateKeys.size) {

                    for (j in 0 until list.size) {
                        if (TextUtils.equals(privateKeys[i].publicKey.toString(Constants.Const.WALLETTYPE), list[j].keys)) {
                            var secrekey = SecretKeyBean()
                            secrekey.publicKey = privateKeys[i].publicKey.toString(Constants.Const.WALLETTYPE)
                            val s = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), privateKeys[i].toWif().toByteArray(), PreferencesHelper.getIv(pwd512))
                            secrekey.privateKey = Base58.encode(s)

                            secrekey.accountName = list[j].perm_name
                            listKey.add(secrekey)
                        }
                    }

                }

                //公钥
//                  privateKeys[0].publicKey.toString()

//                // 把私钥加密成keyStore
//                val s =  CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes,PreferencesHelper.ENCRYPT_KEY_LEN),privateKeys[0].bytes,PreferencesHelper.getIv(pwd512))
//                Base58.encode(s)

                return WalletManagePresenter.EnDataResult(true, "", listKey)
            }
        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getDecrypt(it.enData)
                }, {
                    getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.dialog_key_ktore_err_txt))
                }))

    }

    /**
     *导出私钥
     */
    fun exportPrivateKey(pwd: String, accountTable: AccountTable) {

        addDisposable(Run(object : Callback<WalletManagePresenter.EnDataResult> {
            override fun call(): WalletManagePresenter.EnDataResult {
                val pwd512 = Sha512.from(pwd.toByteArray())
                //解密
                var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
                val privateKeys = ArrayList<EosPrivateKey>()
                var listKey = ArrayList<SecretKeyBean>()
                val reader = Reader(decrypt)
                val size = reader.uInt
                for (i in 0 until size) {
                    val length = reader.uInt
                    val privateBytes = reader.getBytes(length.toInt())
                    privateKeys.add(EosPrivateKey(privateBytes))
                }
                //获取所有公钥名称
                var list = Gson().fromJson<java.util.ArrayList<PermissionsBean>>(accountTable?.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)

                for (i in 0 until privateKeys.size) {
                    for (j in 0 until list.size) {

                        if (TextUtils.equals(privateKeys[i].publicKey.toString(Constants.Const.WALLETTYPE), list[j].keys)) {

                            var secrekey = SecretKeyBean()
                            secrekey.publicKey = privateKeys[i].publicKey.toString(Constants.Const.WALLETTYPE)
                            val s = EosPrivateKey(privateKeys[i].bytes).toWif()
                            secrekey.privateKey = s
                            secrekey.accountName = list[j].perm_name
                            listKey.add(secrekey)

                        }


                    }


                }

                //公钥
//                  privateKeys[0].publicKey.toString()

//                // 把私钥加密成keyStore
//                val s =  CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes,PreferencesHelper.ENCRYPT_KEY_LEN),privateKeys[0].bytes,PreferencesHelper.getIv(pwd512))
//                Base58.encode(s)

                return WalletManagePresenter.EnDataResult(true, "", listKey)
            }
        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getPrivateKey(it.enData)
                }, {
                    getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.dialog_key_ktore_err_txt_priavte))
                }))

    }


    class EnDataResult(success: Boolean, error: String, val enData: ArrayList<SecretKeyBean>) : Result(success, error)

    /**
     * 删除钱包
     */
    fun deleteWallet(account: String) {
        Flowable.just(account)
                .flatMap {
                    dataManager.accountDao.deleteAccount(it)
                    return@flatMap Flowable.just(it)
                }
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.onDeleteWalletSuccess()
                }, {
                    getView()?.onDeleteWalletFail()
                })


    }
    /**
     *云端 删除钱包
     */
    fun deleteWalletCloud(account: String) {
        Flowable.just(account)
                .flatMap {
                    dataManager.accountDao.deleteAccount(it)
                    return@flatMap Flowable.just(it)
                }
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.onDeleteWalletSuccess()
                }, {
                    getView()?.onDeleteWalletFail()
                })


    }

    //获取当前钱包的公钥和私钥对
    fun currentAccountKeys(pwd: String, accountTable: AccountTable) {
        addDisposable(Run(object : Callback<WalletManagePresenter.EnDataResult> {
            override fun call(): WalletManagePresenter.EnDataResult {
                val pwd512 = Sha512.from(pwd.toByteArray())
                //解密
                var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
                val privateKeys = ArrayList<EosPrivateKey>()
                var listKey = ArrayList<SecretKeyBean>()
                val reader = Reader(decrypt)
                val size = reader.uInt
                for (i in 0 until size) {
                    val length = reader.uInt
                    val privateBytes = reader.getBytes(length.toInt())
                    privateKeys.add(EosPrivateKey(privateBytes))
                }

                for (i in 0 until privateKeys.size) {
                    var secrekey = SecretKeyBean()
                    secrekey.publicKey = privateKeys[i].publicKey.toString(Constants.Const.WALLETTYPE)
                    val s = EosPrivateKey(privateKeys[i].bytes).toWif()
                    secrekey.privateKey = s
                    listKey.add(secrekey)
                }



                return WalletManagePresenter.EnDataResult(true, "", listKey)
            }
        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getCurrentAccountKey(it.enData, pwd)
                }, {
                    getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.dialog_key_ktore_err_txt))
                }))
    }


    /** //修改权限0
     * 当前账号
     * 修改的权限名字
     * 父级名称
     * 修改的新的公钥
     * 修改新的私钥
     * 当前账号私钥
     *保存的对象
     * 密码
     * 修改的公钥
     * 修改的什么权限
     */
    @SuppressLint("CheckResult")
    fun updateAuthority(account: String, authority: String, parentLevel: String, keyPublic: String, keyPriavate: String, currentPriavteKey: String, accountTable: AccountTable, pwd: String, updatPublic: String, updateName: String) {
        val pwd512 = Sha512.from(pwd.toByteArray())
        val iv = PreferencesHelper.getIv(pwd512)
//        d.dispose()


        val d = Run<Result>(object : Callback<Result> {

            override fun call(): Result {
                //地址HttpService("http://47.254.82.241:80")
                val start = StartFactory.build(HttpService(Constants.Const.URL))

                val accountName = Name(account)
                //删除当前权限名称
                val active = Name(authority)
                //父级权限的名称
                val parent = Name(parentLevel)
                var typeAuthority = TypeAuthority.create(TypeKeyWeight(TypePublicKey(keyPublic), 1))
                var writer = Writer(255);
                accountName.pack(writer);
                active.pack(writer);
                parent.pack(writer);
                typeAuthority.pack(writer);
                val a = Action.toAction("eosio", "updateauth", "$account@owner", writer.toBytes())
                var actions = ArrayList<Action>();
                actions.add(a);
                var packedTransaction = SignedTransaction.createTransactionSync(start, actions, EosPrivateKey(currentPriavteKey));
                var transactionResponse = start.pushTransaction(packedTransaction).send();
                if (transactionResponse.isError) {
                    return Result(false, transactionResponse.error.what)
                } else {
                    //成功//

                    //获取保存的公钥
                    var keeplist = Gson().fromJson<ArrayList<String>>(accountTable.publicKey, object : com.google.common.reflect.TypeToken<ArrayList<String>>() {}.type)
                    var isUpdate: Boolean = false //表示没有修改当前保存的公钥
                    for (i in 0 until keeplist.size) {
                        //如果修改了当前保存的任意公钥
                        if (TextUtils.equals(updatPublic, keeplist[i])) {
                            isUpdate = true
                            break
                        }
                    }
                    //等于true就是修改当前的公钥要进行数据库覆盖
                    if (isUpdate) {
                        var list = Gson().fromJson<java.util.ArrayList<PermissionsBean>>(accountTable?.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)
                        var num = 0
                        var newListPublic = PermissionsBean()
                        for (i in 0 until list.size) {
                            if (TextUtils.equals(updatPublic, list[i].keys) && TextUtils.equals(updateName, list[i].perm_name)) {
                                newListPublic = list[i]
                                num = i
                                break
                            }
                        }
                        list[num].keys = keyPublic

                        var listname = Gson().fromJson<java.util.ArrayList<String>>(accountTable?.publicKey, object : com.google.common.reflect.TypeToken<java.util.ArrayList<String>>() {}.type)
                        num = 0
                        for (i in 0 until listname.size) {
                            if (TextUtils.equals(updatPublic, listname[i])) {
                                num = i
                                break
                            }
                        }
                        listname[num] = keyPublic
                        accountTable.backup = false
                        accountTable.accountPublic = Gson().toJson(list)
                        accountTable.publicKey = Gson().toJson(listname)
                        //获取新的私钥
                        var newEosPrivateKey = EosPrivateKey(keyPriavate)
                        //解密
                        var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
                        val privateKeys = ArrayList<EosPrivateKey>()
                        val reader = Reader(decrypt)
                        val size = reader.uInt
                        //获取解密后的所有key
                        for (i in 0 until size) {
                            val length = reader.uInt
                            val privateBytes = reader.getBytes(length.toInt())
                            privateKeys.add(EosPrivateKey(privateBytes))
                        }

                        val privateKeyListByte = ArrayList<ByteArray>()
                        for (i in 0 until privateKeys.size) {
                            if (TextUtils.equals(updatPublic, privateKeys[i].publicKey.toString())) {
                                privateKeys[i] = newEosPrivateKey
                            }
                            privateKeyListByte.add(privateKeys[i].bytes)
                        }

                        val writer = Writer(512)
                        writer.putUint(privateKeyListByte.size.toLong())

                        for (i in 0 until privateKeyListByte.size) {
                            val bytes = privateKeyListByte[i]
                            writer.putUint(bytes.size.toLong())
                            writer.putBytes(bytes)
                        }
                        val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                        accountTable.cipherText = Base58.encode(enData)
                        dataManager.accountDao.insert(accountTable)

                        return Result(true, "")
                    } else {
                        //======================
                        var list = Gson().fromJson<java.util.ArrayList<PermissionsBean>>(accountTable?.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)
                        var num = 0
                        for (i in 0 until list.size) {
                            if (TextUtils.equals(updatPublic, list[i].keys)) {
                                num = i
                                break
                            }
                        }
                        list[num].keys = keyPublic
                        list[num].perm_name = updateName
                        var listname = Gson().fromJson<java.util.ArrayList<String>>(accountTable?.publicKey, object : com.google.common.reflect.TypeToken<java.util.ArrayList<String>>() {}.type)
                        num = 0
                        //查看导入的公钥是否有
                        var used: Boolean = false
                        for (i in 0 until listname.size) {
                            if (TextUtils.equals(updatPublic, listname[i])) {
                                num = i
                                used = true
                                break
                            }
                        }
                        if (used) {
                            listname[num] = keyPublic
                        } else {
                            listname.add(keyPublic)
                        }
                        accountTable.backup = false
                        accountTable.accountPublic = Gson().toJson(list)
                        accountTable.publicKey = Gson().toJson(listname)
                        //获取新的私钥
                        var newEosPrivateKey = EosPrivateKey(keyPriavate)
                        //解密
                        var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
                        val privateKeys = ArrayList<EosPrivateKey>()
                        val reader = Reader(decrypt)
                        val size = reader.uInt
                        //获取解密后的所有key
                        for (i in 0 until size) {
                            val length = reader.uInt
                            val privateBytes = reader.getBytes(length.toInt())
                            privateKeys.add(EosPrivateKey(privateBytes))
                        }

                        val privateKeyListByte = ArrayList<ByteArray>()

                        var not: Boolean = false
                        for (i in 0 until privateKeys.size) {
                            if (TextUtils.equals(updatPublic, privateKeys[i].publicKey.toString())) {
                                privateKeys[i] = newEosPrivateKey
                                not = true
                                privateKeyListByte.add(privateKeys[i].bytes)
                                continue
                            } else {
                                privateKeyListByte.add(privateKeys[i].bytes)
                            }

                        }
                        if (not == false) {
                            privateKeyListByte.add(newEosPrivateKey.bytes)
                        } else {

                        }


                        val writer = Writer(512)
                        writer.putUint(privateKeyListByte.size.toLong())

                        for (i in 0 until privateKeyListByte.size) {
                            val bytes = privateKeyListByte[i]
                            writer.putUint(bytes.size.toLong())
                            writer.putBytes(bytes)
                        }
                        val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                        accountTable.cipherText = Base58.encode(enData)
                        dataManager.accountDao.insert(accountTable)

                        return Result(true, "")

                    }


                    return Result(true, "")
                }


                return Result(false, "")

            }


        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    if (it.success) {
                        getView()?.updateSuccess()
                    } else {
                        getView()?.errAccount(it.error)
                    }
                }, {

                    getView()?.errAccount(getView()?.context()!!.getString(R.string.err_txt_network))
                })


    }


    //=============
    //登录
    fun loginOneDrive(onNext: Consumer<LoginResponse>, onError: Consumer<Throwable>) {
        addDisposable(StorageFactory.createOneDrive(getView()?.context()!! as Activity).login(getView()?.context()!! as Activity).rxJava()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext,onError))
    }

    //获取列表
    fun getCloudAccounts() {
        addDisposable(OneDrive.get(getView() as Activity)
                .lastOne().rxJava().flatMap {
                    return@flatMap OneDrive.get(getView() as Activity).download(it.name).rxJava()
                }.subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({ it ->
                    getView()?.cloceDialog()
                    decodeCloudData(it.string())
                }, {
                    getView()?.getErrAuthority(getView()?.context()!!.getString(R.string.cloud_delte_err_txt))
                }))
    }

    //加密
    fun encodeCloudData(data: String, pwd: String): String {
        val pwd512 = Sha512.from(pwd.toByteArray())
        val iv = PreferencesHelper.getIv(pwd512)
        val encodeData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN),
                Base58.encode(data.toByteArray(Charset.forName("UTF-8"))).toByteArray(Charset.forName("UTF-8")),
                PreferencesHelper.getIv(pwd512))
        return Base58.encode(encodeData)
    }

    //解密
    fun decodeCloudData(data: String) {
        getView()?.showPwd(object : PwdDialogCloud.PwdCallback {
            override fun onPwd(pwd: String) {
                var decode: String? = null
                val pwd512 = Sha512.from(pwd.toByteArray())
                val iv = PreferencesHelper.getIv(pwd512)
                val decodeData = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN),
                        Base58.decode(data),
                        PreferencesHelper.getIv(pwd512))
                val base58Data =
                        if (decodeData == null) {
                            //密码错误
                            Toast.makeText(getView()?.context(), getView()?.context()!!.resources.getString(R.string.dialog_pwd_err_txt), Toast.LENGTH_SHORT).show()
                            decodeCloudData(data)
                            return
                        } else {
                            Base58.decode(String(decodeData))
                        }
                decode = String(base58Data)
                val json = JSONObject(decode)
                val accountList = json.optJSONArray("accountList")
                val cloudAccounts = ArrayList<EnDataResults>()
                for (i in 0 until accountList.length()) {
                    val obj = accountList.optJSONObject(i)
                    val keys = obj.getJSONObject("keys")
                    val keyMap = keys.keys()
                    val keyBeans = ArrayList<SecretKeyBean>()
                    keyMap.forEach { jsonKey ->
                        val bean = SecretKeyBean()
                        bean.publicKey = jsonKey.split("_")[0]
                        bean.privateKey = keys.optString(jsonKey)
                        keyBeans.add(bean)
                    }
                    val item =EnDataResults(true, "", obj.optString("accountName"), keyBeans)
                    cloudAccounts.add(item)
                }
                getView()?.onCloudAccounts(cloudAccounts)
            }

        })
    }


    /**
     * 同步账号，合并，并且导入本地，且备份到云端（先传云端，再导入本地，方便写入状态）
     *
     */
    fun synchronization(localKeys: List<EnDataResults>, cloudKeys: List<EnDataResults>, pwd: String, onNext: Consumer<UploadResponse>, onError: Consumer<Throwable>) {
        addDisposable(Flowable.create<JSONObject>({
            val keys = LinkedList<EnDataResults>()
            keys.addAll(localKeys)
            keys.addAll(cloudKeys)
            val json = JSONObject()
            json.put("timestamp", System.currentTimeMillis())
            json.put("device", android.os.Build.DEVICE)
            val accountList = JSONArray()
            var i = 0
            while (i < keys.size) {
                val key1 = keys[i]
                for (j in keys.size - 1 downTo 0) {
                    if (key1.accountName.equals(keys[j].accountName) && i != j) {
                        //是同一个账号，然后合并他们的公钥
                        val key2 = keys[j]
                        val set1 = HashSet<SecretKeyBean>()
                        set1.addAll(key1.enData)
                        set1.addAll(key2.enData)
                        key1.enData = set1.toList()
                        keys.removeAt(j)
                    }
                }
                val item = JSONObject()
                item.put("accountName", key1.accountName)
                val itemKeys = JSONObject()
                //获取公钥权限名称
                val accountResponse = dataManager.start.accountInfo(key1.accountName).send()
                val permissions = if (accountResponse.isError) {
                    //网络错误，如果是500错误，则未没有权限
                    if (accountResponse.code == 500) {
                        arrayListOf()
                    } else {
                        it.onError(Throwable(getView()?.context()?.getString(R.string.err_txt_network)))
                        return@create
                    }
                } else {
                    accountResponse.permissions
                }
                for (j in 0 until key1.enData.size) {
                    for (k in 0 until permissions.size) {
                        for (l in 0 until permissions[k].required_auth.keys.size) {
                            if (key1.enData[j].publicKey.equals(permissions[k].required_auth.keys[l].key)) {
                                itemKeys.put(key1.enData[j].publicKey + "_" + permissions[k].perm_name, key1.enData[j].privateKey)
                            }
                        }
                    }
                }
                if (itemKeys.keys().hasNext()) {
                    item.put("keys", itemKeys)
                    accountList.put(item)
                } else {
                    //key失效了，保留原本的
                    for (j in 0 until key1.enData.size) {
                        itemKeys.put(key1.enData[j].publicKey + "_invalid", key1.enData[j].privateKey)
                    }
                    item.put("keys", itemKeys)
                    accountList.put(item)
                }
                i++
            }
            json.put("accountList", accountList)
            it.onNext(json)
        }, BackpressureStrategy.ERROR)
                .flatMap {
                    return@flatMap OneDrive.get(getView() as Activity)
                            .upload(encodeCloudData(it.toString(), pwd), "backup" + SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date()) + ".zip")
                            .rxJava()
                }
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe(onNext, onError))
    }

    class EnDataResults(success: Boolean, error: String, val accountName: String, var enData: List<SecretKeyBean>) : Result(success, error) {
        var isLocal = false
    }

    fun getLocalAccounts(name:String) {
        addDisposable(
                Flowable.create<AccountTable>({
                    it.onNext(dataManager.accountDao.getWithAccountSync(name)!!)
                    it.onComplete()
                }, BackpressureStrategy.ERROR)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
//                            getView()?.onLocalAccounts(it)
                        }, {
//                            getView()?.onLocalAccounts(emptyList())
                        })
        )
    }

}