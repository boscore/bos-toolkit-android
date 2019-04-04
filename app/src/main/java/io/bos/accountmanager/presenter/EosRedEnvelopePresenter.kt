package io.bos.accountmanager.presenter

import android.annotation.SuppressLint
import android.util.Log
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.view.EosRedEnvelopeView
import io.starteos.jeos.Name
import io.starteos.jeos.crypto.digest.Sha256
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.crypto.util.HexUtils
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import io.starteos.jeos.raw.core.Action
import io.starteos.jeos.raw.core.Asset
import io.starteos.jeos.raw.core.TypeSignature
import io.starteos.jeos.transaction.PackedTransaction
import io.starteos.jeos.transaction.SignedTransaction
import io.starteos.jeos.transaction.type.ChainTypeId
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

/**
 *
 *
 * Created by Administrator on 2018/12/26/026.
 */
class EosRedEnvelopePresenter @Inject constructor() : AbstractPresenter<EosRedEnvelopeView>() {
    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper

    /**
     * 获取全部账号信息
     */
    fun getAllAccount() {
        addDisposable(dataManager.accountDao.getAll()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getAllAccountSuccess(it as ArrayList<AccountTable>)
                }, {
                    getView()?.getAllAccountFail()
                }))
    }

    /**
     *导出私钥
     */
    fun exportPrivateKey(pwd: String, accountTable: AccountTable) {
        addDisposable(Run(object : Callback<EnDataResult> {
            override fun call(): EnDataResult {
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
                return EnDataResult(true, "", listKey)
            }
        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getPrivateKey(it.enData)
                }, {
                    getView()?.getPrivateKeyFail(getView()?.context()?.resources!!.getString(R.string.dialog_key_ktore_err_txt))
                }))

    }

    class EnDataResult(success: Boolean, error: String, val enData: ArrayList<SecretKeyBean>) : Result(success, error)
    class RedResult(success: Boolean, error: String, val privateKey: String, val id: Long, val congratulations: String) : Result(success, error)
    class GetRedResult(success: Boolean, error: String, val quantity: String, val memo: String, val time: String) : Result(success, error)

    @SuppressLint("CheckResult")
            /**
     * accountName:红包创建账户
     * privateKey：红包创建账户私钥
     * permission：红包创建账户私钥权限
     * amount：红包金额
     * redEnvelopeType：红包类型
     * count：红包个数
     * congratulations:红包祝福语
     */
    fun createRedEnvelope(accountName: String, privateKeyActive: String, amount: String, redEnvelopeType: Int, count: Int, congratulations: String) {


//        System.out.println(Gson().toJson(result))


        Run<RedResult>(object : Callback<RedResult> {
            override fun call(): RedResult {
                val key = EosPrivateKey()
                var privateKey = key.toString()
                var publicKey = key.publicKey.toString(Constants.Const.WALLETTYPE)
                System.out.println("公钥===" + publicKey)
                System.out.println("私钥===" + privateKey)


                //提交者的的当前账号公私钥
//        val privateKeyOwner = "5K2F8hErqaJPh8aBRQPrQRTzZgrgVnvzBZJrBSBNxjZgbxFn5Hn"
//        val privateKeyActive = "5KNr8svTKXuzBf8qg1pWs7uDHzsGXf9R6fXY31EB5Mt1RofZmMh"
                //地址
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                //首先获取info
                val info = start.info().send()
                //准备开始签名
                val txn = SignedTransaction()
                val id = System.currentTimeMillis() + Random().nextInt(10000)
                System.out.println("id===" + id)
                //需要签名的公私钥
                val sign = EosPrivateKey(privateKeyActive)
                //通过eosio.token的transfer接口进行 拼接memo

                val memo = "hb^$redEnvelopeType^$id^$count^$publicKey^$accountName^$congratulations"

                //转账的账号
                val from = Name(accountName)
                //转给哪个合约 现在是测试合约
                val to = Name(Constants.Const.BOS_CONTRACT)
                //金额
                val quantity = Asset(amount)


                //根据函数顺序进行put
                val wdata = Writer(255)
                from.pack(wdata)
                to.pack(wdata)
                quantity.pack(wdata)
                wdata.putString(memo)
                //放入id
                txn.setReferenceBlock(info.head_block_id)
                //设置超时时间
                txn.expiration = info.getTimeAfterHeadBlockTime(30000)
                //封装action      合约名字， 函数名，账号@权限名字       wdata put的数据bytese格式
                val action = Action.toAction("eosio.token", "transfer", accountName + "@active", wdata.toBytes())

                //添加action  可以多个添加
                txn.addAction(action)
                //整个的加密
                txn.sign(sign, ChainTypeId(info.chain_id))
                //发送请求
                val result = start.pushTransaction(PackedTransaction(txn)).send()
                //判断是否成功  ==flase 创建成功
                if (result.isError) {
                    return RedResult(false, result.error.what, privateKey, id.toLong(), congratulations)
                } else {
                    return RedResult(true, "", privateKey, id.toLong(), congratulations)
                }


//                return RedResult(false, getView()?.context()!!.getString(R.string.err_txt_network), publicKey, id.toLong(), accountName)
            }

        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    if (it.success) {
                        var redEnvelopeText = "" + redEnvelopeType + "^" + it.id + "^" + it.privateKey
                        getView()?.createRedEnvelopeSuccess(redEnvelopeText)
                    } else {
                        getView()?.createRedEnvelopeFail(it.error)
                    }
                }, {

                    getView()?.createRedEnvelopeFail(it.message!!)
                })

    }

    fun get(receiver: String, redPrivateKey: String, id: Long) {

        addDisposable(Run(object : Callback<GetRedResult> {
            override fun call(): GetRedResult {


                val signPrivate = redPrivateKey
                val privateKey = EosPrivateKey(signPrivate)
//领取的账号
                val account = Name(receiver)
//签名的sig  客户端使用私钥对红包id进行ecdsa签名生成，每个签名只能使用一次
                val s = Sha256.from(id.toString().toByteArray(Charset.forName("UTF-8")))
                val signId = privateKey.sign(s).eosEncodingHex(false)
//                val signId = privateKey.sign(s).toString()
                val sig = TypeSignature(signId)
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                val writer = Writer(255)
                account.pack(writer)
                writer.putLong(id)
                sig.pack(writer)
                val action = Action.toAction(Constants.Const.BOS_CONTRACT, "get", "bosfreetouse@redpacket", HexUtils.toHex(writer.toBytes()))
                val result = start.pushTransaction(SignedTransaction.createTransactionSync(start, arrayListOf(action), EosPrivateKey("5Jg3KtArcxdsk2opXpyBNqKeZ7ah9SFLPg2Xx8vHFGCnfRGffkD"))).send()
                Log.e("result", result.toString())
                return if (result.isError) {
                    GetRedResult(false, result.error.what, "", "", "")
                } else {
                    GetRedResult(true, "", result.processed.action_traces[0].inline_traces[0].act.data.asJsonObject.get("quantity").asString,
                            result.processed.action_traces[0].inline_traces[0].act.data.asJsonObject.get("memo").asString,
                            result.processed.action_traces[0].inline_traces[0].block_time)
                }
            }

        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({

                    getView()?.getRedEnvelopeSuccess(it.quantity, it.memo, it.time)
                }, {
                    getView()?.getRedEnvelopeFail()
                }))


    }


}