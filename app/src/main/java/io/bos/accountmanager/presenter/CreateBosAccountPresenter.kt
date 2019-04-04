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
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.view.CreateBosAccountView
import io.starteos.jeos.Name
import io.starteos.jeos.Symbol

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
import io.starteos.jeos.raw.core.Asset

import io.starteos.jeos.transaction.PackedTransaction
import io.starteos.jeos.transaction.SignedTransaction
import io.starteos.jeos.transaction.type.ChainTypeId
import java.util.*
import javax.inject.Inject

class CreateBosAccountPresenter @Inject constructor() : AbstractPresenter<CreateBosAccountView>() {

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper
    private var starteos: StartEOS = StartFactory.build(HttpService(Constants.Const.URL))

    fun accountsCha(isBoo: Boolean) {
        var isflage: Boolean = false
        isflage = isBoo
        addDisposable(
                dataManager.accountDao.getAll()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .subscribe({

                            if (isflage) {
                                getView()?.onAccountsName(it)
                            }
                            isflage = false
                        }, {
                            if (isflage) {
                                getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.create_bos_pay_qdrzh))
                            }
                            isflage = false
                        })
        )
    }


    fun accountsName() {
        addDisposable(
                Run(object : Callback<RstAccounts> {
                    override fun call(): RstAccounts {
                        val data = dataManager.accountDao.getAllSync()
                        return RstAccounts(true, "", data)
                    }
                }).rxJava()
                        .observeOn(androidSchedulers)
                        .subscribeOn(ioSchedulers)
                        .doOnComplete {

                        }
                        .subscribe({
                            getView()?.onAccountsName(it.date)
                        }, {
                            getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.create_bos_pay_qdrzh))
                        })


        )


    }

    class RstAccounts(success: Boolean, error: String, var date: List<AccountTable>) : Result(success, error) {}


    //获取当前钱包的公钥和私钥对
    fun currentAccountKeys(pwd: String, accountTable: AccountTable) {
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
                    getView()?.getCurrentAccountKey(it.enData, pwd)
                }, {
                    getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.create_bos_account_err_txt_active))
                }))
    }


    class EnDataResult(success: Boolean, error: String, val enData: ArrayList<SecretKeyBean>) : Result(success, error)


    /**
     * 开始交易
     * 当前账号的
     * 转账memo
     * 当前账号的名称
     * 转账金额
     *
     */
    fun transferTransaction(privateKey: String, memo: String, accountName: String, money: String) {

        val d = Run<Result>(object : Callback<Result> {

            override fun call(): Result {
                //地址
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                //首先获取info
                val info = start.info().send()
                //准备开始签名
                val txn = SignedTransaction()
                val id = System.currentTimeMillis() + Random().nextInt(10000)
                val sign = EosPrivateKey(privateKey)
                //转账的账号
                val from = Name(accountName)
                //转给哪个合约 现在是测试合约
                val to = Name(Constants.Const.BOS_CONTRACT)
                //金额
                val quantity = Asset("$money BOS")
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
                val action = Action.toAction("eosio.token", "transfer", "$accountName@active", wdata.toBytes())
                //添加action  可以多个添加
                txn.addAction(action)

                //整个的加密
                txn.sign(sign, ChainTypeId(info.chain_id))
                //发送请求
                val result = start.pushTransaction(PackedTransaction(txn)).send()

                //判断是否成功
                if (result.isError) {
                    return Result(false, result.error.what)
                } else {
                    return Result(true, "")
                }
                return Result(false, result.error.what)
            }


        }).rxJava().observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    if (it.success) {
                        getView()?.establishSuccess()
                    } else {
                        getView()?.errMessage(it.error)
                    }
                }, {

                    getView()?.errMessage(getView()?.context()!!.getString(R.string.red_add_err_txt))
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
                                getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.create_bos_pay_import_fail))
                            } else {
                                val list = ArrayList<String>()
                                list.addAll(it.account_names)
                                getView()?.onAccount(list)
                            }
                        }, {
                            Log.e("HaiChecker", it.message)
                            getView()?.errMessage(getView()?.context()?.resources!!.getString(R.string.create_bos_pay_import_fail))
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
            getView()?.localAccountError(getView()?.context()?.resources!!.getString(R.string.create_bos_pay_import_fail))

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

    class Rst(success: Boolean, error: String, var date: ArrayList<String>) : Result(success, error) {}


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
                                account.create_backup = false
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
                                    accountAndCipherText.second!!.create_backup = false
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

}