package io.bos.accountmanager.presenter

import android.text.TextUtils
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.CloudBean
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.view.ExternalImportView
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.raw.Reader
import org.json.JSONArray
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ExternalImportPresenter @Inject constructor() : AbstractPresenter<ExternalImportView>() {
    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper


    fun getAccpountlist() {
        addDisposable(
                Run(object : Callback<RstAccounts> {
                    override fun call(): RstAccounts {
                        var list = ArrayList<CloudBean>()
                        val data = dataManager.accountDao.getAllSync()
                        for (i in 0 until data.size) {
                            var cloudBean = CloudBean()
                            cloudBean.accountName = data[i].accountName
                            cloudBean.cipherText = data[i].cipherText
                            cloudBean.money = data[i].balance
                            cloudBean.publicKey = data[i].publicKey
                            cloudBean.publicName = data[i].accountPublic
                            list.add(cloudBean)

                        }
                        return RstAccounts(true, "", list)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.getAccontList(it.date)
                        }, {

                        })
        )
    }

    class RstAccounts(success: Boolean, error: String, var date: ArrayList<CloudBean>) : Result(success, error) {}

    /**
     *解密导出私钥
     * 要导出的账号名称
     * 密码
     */
    fun exportPrivateKey(cloudBean:  CloudBean , pwd: String) {
        addDisposable(

                Run(object : Callback<RstPrivate> {
                    override fun call(): RstPrivate {
                        var list = ArrayList<String>()
                        var secrelist =ArrayList<SecretKeyBean>()
                        var json=JSONArray()
                        val pwd512 = Sha512.from(pwd.toByteArray())

                            //解密
                            var decrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd512.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(cloudBean.cipherText), PreferencesHelper.getIv(pwd512))
                            val privateKeys = ArrayList<EosPrivateKey>()
                            val reader = Reader(decrypt)
                            val size = reader.uInt
                            for (j in 0 until size) {
                                val length = reader.uInt
                                val privateBytes = reader.getBytes(length.toInt())
                                privateKeys.add(EosPrivateKey(privateBytes))
                            }

                            var keyName = Gson().fromJson<ArrayList<PermissionsBean>>(cloudBean.publicName, object : com.google.common.reflect.TypeToken<ArrayList<PermissionsBean>>() {}.type)

                            for (j in 0 until privateKeys.size) {
                                for (l in 0 until keyName.size) {
                                    if (TextUtils.equals(privateKeys[j].publicKey.toString(Constants.Const.WALLETTYPE), keyName[l].keys)) {
                                        if (TextUtils.equals(keyName[l].perm_name, "active")) {
                                            list.add(privateKeys[j].toString())
//                                            var objs=JSONObject()
//                                            objs.put("name",privateKeys[j].)
                                            var secretKeyBean=SecretKeyBean()
                                            secretKeyBean.publicKey=privateKeys[j].publicKey.toString(Constants.Const.WALLETTYPE)
                                            secretKeyBean.privateKey=privateKeys[j].toString()
                                            secretKeyBean.accountName= keyName[l].perm_name
                                            secrelist.add(secretKeyBean)
//                                            json.put(privateKeys[j].toString())
                                        }

                                    }


                                }
                            }





                        return RstPrivate(true, "",secrelist)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({
                            getView()?.exportPriavteList(it.date)
                        }, {

                        })

        )
    }

    class RstPrivate(success: Boolean, error: String, var date:  ArrayList<SecretKeyBean>) : Result(success, error) {}


}





