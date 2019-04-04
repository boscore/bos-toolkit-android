package io.bos.accountmanager.presenter

import android.app.Activity
import android.widget.Toast
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.storage.OneDrive
import io.bos.accountmanager.core.storage.core.response.LogoutResponse
import io.bos.accountmanager.core.storage.core.response.UploadResponse
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.ui.dialog.PwdDialogCloud
import io.bos.accountmanager.view.ICloudStorageView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.net.response.AccountResponse
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class CloudStoragePresenter @Inject constructor() : AbstractPresenter<ICloudStorageView>() {

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper

    fun logout(onNext: Consumer<LogoutResponse>, onError: Consumer<Throwable>) {
        addDisposable(OneDrive.get(getView() as Activity)
                .logout().rxJava()
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe(onNext, onError))
    }

    fun getLocalAccounts() {
        addDisposable(
                Flowable.create<List<AccountTable>>({
                    it.onNext(dataManager.accountDao.getAllSync())
                    it.onComplete()
                }, BackpressureStrategy.ERROR)
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.onLocalAccounts(it)
                        }, {
                            getView()?.onLocalAccounts(emptyList())
                        })
        )
    }

    fun uploadBackupStatus(data: List<AccountTable>) {
        addDisposable(
                Flowable.create<Array<Long>>({
                    it.onNext(dataManager.accountDao.insertAll(data.toTypedArray()))
                    it.onComplete()
                }, BackpressureStrategy.ERROR)
                        .subscribeOn(ioSchedulers)
                        .observeOn(androidSchedulers)
                        .subscribe({
                        }, {
                        }))
    }

    //获取列表
    fun getCloudAccounts() {
        addDisposable(OneDrive.get(getView() as Activity)
                .lastOne().rxJava().flatMap {
                    return@flatMap OneDrive.get(getView() as Activity).download(it.name).rxJava()
                }.subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({ it ->
                    decodeCloudData(it.string())
                }, {
                    getView()?.onCloudAccounts(emptyList())
                }))
    }

    fun encodeCloudData(data: String, pwd: String): String {
        val pwd512 = Sha512.from(pwd.toByteArray())
        val iv = PreferencesHelper.getIv(pwd512)
        val encodeData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN),
                Base58.encode(data.toByteArray(Charset.forName("UTF-8"))).toByteArray(Charset.forName("UTF-8")),
                PreferencesHelper.getIv(pwd512))
        return Base58.encode(encodeData)
    }

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
                val cloudAccounts = ArrayList<EnDataResult>()
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
                    val item = EnDataResult(true, "", obj.optString("accountName"), keyBeans)
                    cloudAccounts.add(item)
                }
                getView()?.onCloudAccounts(cloudAccounts)
            }

        })
    }

    /**
     *解出私钥和公钥
     */
    fun exportPrivateKey(pwd: String, accounts: List<AccountTable>) {
        addDisposable(Flowable.create<ArrayList<EnDataResult>>({
            val list = ArrayList<EnDataResult>()
            for (i in 0 until accounts.size) {
                list.add(decodeTable(pwd, accounts[i]))
            }
            it.onNext(list)
            it.onComplete()
        }, BackpressureStrategy.ERROR).observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.onExportPrivateKey(it)
                }, {
                    getView()?.onExportPrivateKey(emptyList())
                }))
    }

    private fun decodeTable(pwd: String, accountTable: AccountTable): EnDataResult {
        val pwd512 = Sha512.from(pwd.toByteArray())
        //解密
        val decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(accountTable.cipherText), PreferencesHelper.getIv(pwd512))
        val listKey = ArrayList<SecretKeyBean>()
        val reader = Reader(decrypt)
        val size = reader.uInt
        for (i in 0 until size) {
            val length = reader.uInt
            val privateBytes = reader.getBytes(length.toInt())
            val privateKey = EosPrivateKey(privateBytes)
            val secrekey = SecretKeyBean()
            secrekey.publicKey = privateKey.publicKey.toString(Constants.Const.WALLETTYPE)
            val s = EosPrivateKey(privateKey.bytes).toWif()
            secrekey.privateKey = s
            listKey.add(secrekey)
        }
        return EnDataResult(true, "", accountTable.accountName, listKey)
    }

    class EnDataResult(success: Boolean, error: String, val accountName: String, var enData: List<SecretKeyBean>) : Result(success, error) {
        var isLocal = false
    }

    fun importFromCloud(localKeys: List<EnDataResult>, cloudKeys: List<EnDataResult>, pwd: String, onNext: Consumer<Boolean>, onError: Consumer<Throwable>) {
        addDisposable(Flowable.create<List<AccountTable>>({
            val merged = merge(localKeys, cloudKeys)
            val pwd512 = Sha512.from(pwd.toByteArray())
            val iv = PreferencesHelper.getIv(pwd512)
            val result = ArrayList<AccountTable>()
            merged.forEach { enDataResult ->
                val table = AccountTable()
                table.accountName = enDataResult.accountName
                table.backup = true
                table.balance = "0.0000 BOS"
                val publicKeys = JSONArray()
                val accountPublic = JSONArray()
                val privateKeyList = ArrayList<ByteArray>()
                val accountInfo = dataManager.start.accountInfo(enDataResult.accountName).send()
                if (accountInfo.isError) {
                    //网络错误，如果是500错误，则未没有权限
                    if (accountInfo.code == 500) {
                        accountInfo.permissions = arrayListOf()
                    } else {
                        it.onError(Throwable(getView()?.context()!!.getString(R.string.err_txt_network)))
                        return@create
                    }
                }
                accountInfo.permissions.forEach { permission ->
                    if (permission.perm_name.equals("active") || permission.perm_name.equals("owner")) {
                        permission.required_auth.keys.forEach { keys ->
                            val accountJson = JSONObject()
                            accountJson.put("keys", keys.key)
                            accountJson.put("perm_name", permission.perm_name)
                            accountPublic.put(accountJson)
                        }
                    }
                }
                enDataResult.enData.forEach { secretKeyBean ->
                    try {
                        val eosPrivateKey = EosPrivateKey(secretKeyBean.privateKey)
                        accountInfo.permissions.forEach { permission ->
                            if (permission.required_auth.keys.find<AccountResponse.PermissionsBean.RequiredAuthBean.KeysBean> { keysBean ->
                                        keysBean.key.equals(secretKeyBean.publicKey)
                                    } != null) {
                                publicKeys.put(eosPrivateKey.publicKey.toString(Constants.Const.WALLETTYPE))
                                privateKeyList.add(eosPrivateKey.bytes)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                table.accountPublic = accountPublic.toString()
                table.publicKey = publicKeys.toString()
                val writer = Writer(512)
                writer.putUint(privateKeyList.size.toLong())
                privateKeyList.forEach { bytes ->
                    writer.putUint(bytes.size.toLong())
                    writer.putBytes(bytes)
                }
                val enData = CryptUtil.aesEncrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), writer.toBytes(), iv)
                table.cipherText = Base58.encode(enData)
                result.add(table)
            }
            it.onNext(result)
        }, BackpressureStrategy.ERROR)
                .flatMap { tabls ->
                    return@flatMap Flowable.create<Boolean>({
                        dataManager.accountDao.insertAll(tabls.toTypedArray())
                        it.onNext(true)
                        it.onComplete()
                    }, BackpressureStrategy.ERROR)
                }
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe(onNext, onError))
    }

    fun merge(localKeys: List<EnDataResult>, cloudKeys: List<EnDataResult>): List<EnDataResult> {
        val keys = LinkedList<EnDataResult>()
        keys.addAll(localKeys)
        keys.addAll(cloudKeys)
        val empty = LinkedList<EnDataResult>()
        var i = 0
        while (i < keys.size) {
            val key1 = keys[i]
            for (j in keys.size - 1 downTo 0) {
                if (key1.accountName.equals(keys[j].accountName) && i != j) {
                    //是同一个账号，然后合并他们的公钥
                    val key2 = keys[j]
                    val set1 = key1.enData.toHashSet()
                    val set2 = key2.enData.toHashSet()
                    set1.addAll(set2)
                    key1.enData = set1.toList()
                    keys.removeAt(j)
                }
            }
            if (key1.enData.isNotEmpty()) {
                empty.add(key1)
            }
            i++
        }
        return empty
    }

    /**
     * 同步账号，合并，并且导入本地，且备份到云端（先传云端，再导入本地，方便写入状态）
     *
     */
    fun synchronization(localKeys: List<EnDataResult>, cloudKeys: List<EnDataResult>, pwd: String, onNext: Consumer<UploadResponse>, onError: Consumer<Throwable>) {
        addDisposable(Flowable.create<JSONObject>({
            val keys = LinkedList<EnDataResult>()
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

}