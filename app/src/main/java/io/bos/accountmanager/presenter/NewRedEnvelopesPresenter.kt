package io.bos.accountmanager.presenter


import android.annotation.SuppressLint
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
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.NetManager
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.view.NewRedEnvelopesView
import io.starteos.jeos.Name
import io.starteos.jeos.Symbol
import io.starteos.jeos.crypto.digest.Sha256
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.net.StartEOS
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Reader
import io.starteos.jeos.raw.Writer
import io.starteos.jeos.raw.core.Action
import io.starteos.jeos.raw.core.Signature
import io.starteos.jeos.raw.core.TypePublicKey
import io.starteos.jeos.raw.core.TypeSignature
import io.starteos.jeos.transaction.PackedTransaction
import io.starteos.jeos.transaction.SignedTransaction
import io.starteos.jeos.transaction.type.ChainTypeId
import java.nio.charset.Charset

import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class NewRedEnvelopesPresenter @Inject constructor() : AbstractPresenter<NewRedEnvelopesView>() {
    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var netManager: NetManager
    @Inject
    lateinit var shared: PreferencesHelper

    private var starteos: StartEOS = StartFactory.build(HttpService(Constants.Const.URL))


    @SuppressLint("CheckResult")
    //获取所有的本地账户名
    fun getEstablishAccount(name: String) {
        Run(object : Callback<RstIsAccount> {
            override fun call(): RstIsAccount {
                var count = dataManager.establishAccountDao.getNameNumber(name)
                if (count > 0) {
                    return RstIsAccount(true, "", true)
                } else {
                    return RstIsAccount(true, "", false)
                }

                return RstIsAccount(false, "", false)
            }
        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({

                    getView()?.AddTransferAccounts(it.date)
                }, {
                    getView()?.errAccount(getView()?.context()!!.getString(R.string.nre_red_enve_txt_inspect))
                })

    }


    class RstIsAccount(success: Boolean, error: String, var date: Boolean) : Result(success, error) {}


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

                                getEstablishAccount(name)

                            } else {
                                getView()?.errAccount(getView()?.context()!!.getString(R.string.new_red_envelopes_err_presence))
                            }
                        }, {
                            //可以创建账号
                            getView()?.errAccount(it.message!!)
                        })

        )


    }


    //没使用红包串的时候检查可以创建保存本地
    @SuppressLint("CheckResult")
    fun AddAccount(establishAccountTable: EstablishAccountTable) {
        Run(object : Callback<Result> {
            override fun call(): Result {
                var dd = dataManager.establishAccountDao.insertEstablishAccount(establishAccountTable)
                return Result(true, "")
            }
        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.getlocalSuccess(establishAccountTable)
                }, {
                    getView()?.localAccountError(getView()?.context()?.resources!!.getString(R.string.new_red_envelopes_err_txt_de))
                })

    }


    @SuppressLint("CheckResult")
            /**
             * 红包串创建账号
             * ======
             * 红包的私钥
             * 红包id
             *创建账号的名称
             *
             */
    fun redEstablishAccount(privateKeyActive: String, id: String, accountName: String) {
        Run<RedResult>(object : Callback<RedResult> {
            override fun call(): RedResult {
                val newOwner = EosPrivateKey()
                //地址
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                //判断账号是否被注册
                var getInfo = start.accountInfo(accountName).send()
                if (getInfo.isError == false) {
                    return RedResult(false, getView()?.context()?.resources!!.getString(R.string.new_red_envelopes_err_presence), newOwner, id.toLong(), accountName)
                }

                val privateKey = EosPrivateKey(privateKeyActive)
                //根据函数方法开始封装   要创建的账号
                val account = Name(accountName)
                val owner_key = TypePublicKey(newOwner.publicKey.toString(Constants.Const.WALLETTYPE))
                val active_key = TypePublicKey(newOwner.publicKey.toString(Constants.Const.WALLETTYPE))
                //签名的sig  客户端使用私钥对红包id进行ecdsa签名生成，每个签名只能使用一次
                val s = Sha256.from(id.toString().toByteArray(Charset.forName("UTF-8")))
                val signId = privateKey.sign(s).toString()
                val sig = TypeSignature(signId)


                //首先获取info
                val info = start.info().send()
                //开始put
                val writer = Writer(255)
                account.pack(writer)
                owner_key.pack(writer)
                active_key.pack(writer)
                writer.putLong(id.toLong())
                sig.pack(writer)


                val txn = SignedTransaction()
                txn.expiration = info.getTimeAfterHeadBlockTime(30000)
                txn.setReferenceBlock(info.head_block_id)

                //必须用有redpacket权限的账号
                val action = Action.toAction(Constants.Const.BOS_CONTRACT, "create", "bosfreetouse@redpacket", writer.toBytes())
                //添加action
                txn.addAction(action)

                //必须用有redpacket权限
                txn.sign(EosPrivateKey("5Jg3KtArcxdsk2opXpyBNqKeZ7ah9SFLPg2Xx8vHFGCnfRGffkD"), ChainTypeId(info.chain_id))

                val result = start.pushTransaction(PackedTransaction(txn)).send()

                //判断是否成功  ==flase 创建成功
                if (result.isError) {
                    return RedResult(false, result.error.what, newOwner, id.toLong(), accountName)
                } else {
                    return RedResult(true, "", newOwner, id.toLong(), accountName)
                }


                return RedResult(false, getView()?.context()!!.getString(R.string.err_txt_network), newOwner, id.toLong(), accountName)
            }

        }).rxJava().flatMap {

            Run(object : Callback<RedResult> {
                override fun call(): RedResult {
                    var establishAccountTable = EstablishAccountTable()
                    establishAccountTable.publicKey = it.newPriavateKey.publicKey.toString(Constants.Const.WALLETTYPE)
                    establishAccountTable.privateKey = it.newPriavateKey.toString()
                    establishAccountTable.accountName = it.accountName
                    establishAccountTable.time = System.currentTimeMillis().toString()
                    var dd = dataManager.establishAccountDao.insertEstablishAccount(establishAccountTable)
                    return RedResult(true, "", it.newPriavateKey, it.id, it.accountName)
                }
            }).rxJava()


        }.observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    if (it.success) {
                        getView()?.CreateSuccess(it.newPriavateKey, it.id, it.accountName)
                    } else {
                        getView()?.errAccount(it.error)
                    }
                }, {

                    getView()?.errAccount(it.message!!)
                })


    }

    class RedResult(success: Boolean, error: String, val newPriavateKey: EosPrivateKey, val id: Long, val accountName: String) : Result(success, error)


    /**
     * 创建红包
     *
     * 红包类型  1：普通红包  2：随机红包  3：仅供创建账号的红包
     * 红包个数
     * 红包祝福语
     * 转账的金额
     * 发送红包的账号
     * 发送红包的私钥
     * 权限名称
     *
     */
    @SuppressLint("CheckResult")
    fun addRedEnvelopes(hb_type: Int, count: Int, greetings: String, money: String, account: String, privateKey: String, authority: String) {

        Run<Result>(object : Callback<Result> {
            override fun call(): Result {
                //地址
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                val getinfo = start.info().send()

                //准备开始签名
                val txn = SignedTransaction()
                //客户端生成的红包id，需要为不会重复的数字，推荐使用当前毫秒数*10000+10000以内随机数
                val id = System.currentTimeMillis() + Random().nextInt(10000)
                //需要签名的公私钥
                val sign = EosPrivateKey(privateKey)
                //通过eosio.token的transfer接口进行 拼接memo   hb开头   类型+id+红包公钥+创建的账号+祝福语
                val memo = "hb^$hb_type^$id^$count^${sign.publicKey.toString(Constants.Const.WALLETTYPE)}^$account^$greetings"
                //转账的账号
                val from = Name("$account")

                //转给哪个合约  测试
                val to = Name(Constants.Const.BOS_CONTRACT)

                //转账的金额
                val quantity = Symbol("$money BOS")
                //根据函数顺序进行put
                val wdata = Writer(255)
                from.pack(wdata)
                to.pack(wdata)
                quantity.pack(wdata)
                wdata.putString(memo)
                //放入id
                txn.setReferenceBlock(getinfo.head_block_id)
                //设置超时时间
                txn.expiration = getinfo.getTimeAfterHeadBlockTime(30000)
                //封装action      合约名字， 函数名，账号@权限名字       wdata put的数据bytese格式
                val action = Action.toAction("eosio.token", "transfer", "$account@active", wdata.toBytes())
                //添加action  可以多个添加
                txn.addAction(action)
                //整个的加密
                txn.sign(sign, ChainTypeId(getinfo.chain_id))
                //发送请求
                val result = start.pushTransaction(PackedTransaction(txn)).send()
                if (result.isError) {
                    return Result(false, result.error.what)
                } else {
                    return Result(true, "")
                }

            }

        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({

                    if (it.success) {//
                        //创建红包成功
                        getView()?.errAccount(it.error)

                    } else {

                        getView()?.errAccount(it.error)

                    }
                }, {

                    getView()?.errAccount(getView()?.context()!!.getString(R.string.err_txt_network))
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
//                                    if (TextUtils.equals(permission.perm_name, "active") || TextUtils.equals(permission.perm_name, "owner")) {
                                        var permissionsBean = PermissionsBean()
                                        for (key in permission.required_auth.keys) {
                                            permissionsBean.perm_name = permission.perm_name
                                            permissionsBean.keys = key.key
                                            listBaifen.add(permissionsBean)

//                                        }
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
                                account.create_backup=false
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
                                    accountAndCipherText.second!!.create_backup=false
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
    class EnDataResult(success: Boolean, error: String, var accounts: Array<AccountTable> = emptyArray()) : Result(success, error)
    class LocalAccount(success: Boolean, error: String, val account: ArrayList<Pair<String, AccountTable?>>) : Result(success, error)
}