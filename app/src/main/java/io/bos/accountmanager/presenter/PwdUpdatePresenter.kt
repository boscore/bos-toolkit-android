package io.bos.accountmanager.presenter

import android.text.TextUtils
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.core.utils.Question
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.ui.PwdUpdateActivity
import io.bos.accountmanager.view.PwdUpdateView
import io.reactivex.Flowable
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import io.starteos.jeos.raw.Reader
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

class PwdUpdatePresenter @Inject constructor() : AbstractPresenter<PwdUpdateView>() {
    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var shared: PreferencesHelper

    //忘记密码查看
    fun seePassword(answers: ArrayList<String>, verifyQuestion: ArrayList<Question.QuestionItem>, one: String, two: String, three: String){
        addDisposable(Run(object : Callback< EnDataResult> {
            override fun call(): EnDataResult {

                val result = PwdUpdateActivity.PwdResult(false, "", "", "")
                answers.add(one + verifyQuestion[0].id)
                answers.add(two + verifyQuestion[1].id)
                answers.add(three + verifyQuestion[2].id)
                answers.sort()
                val answerSort = StringBuffer()
                for (answer in answers) {
                    answerSort.append(answer)
                }
                val pwdSha = Sha512.from(Base58.encode(answerSort.toString().toByteArray(Charset.forName("UTF-8"))).toByteArray())

                val pass = CryptUtil.aesDecrypt(Arrays.copyOf(pwdSha.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(shared.getPwdCipher()), PreferencesHelper.getIv(pwdSha))
                if (pass == null) {
                    result.success = false
                    result.error = getView()?.context()!!.getString(R.string.question_error_str)
                    return EnDataResult(false, getView()?.context()!!.getString(R.string.question_error_str), "")
                }else{
                    return EnDataResult(true, "", String(pass))
                }

                return  EnDataResult(false, getView()?.context()!!.getString(R.string.question_error_str), "")
            }
        }).rxJava()
                .observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .subscribe({
                    getView()?.seetPwd(it.enData)
                }, {
                    getView()?.errSet(getView()?.context()!!.getString(R.string.question_error_str))
                }))
    }
    class EnDataResult(success: Boolean, error: String, val enData:String) : Result(success, error)

    fun verification(answers: ArrayList<String>, verifyQuestion: ArrayList<Question.QuestionItem>, one: String, two: String, three: String, newPassword: String, tip: String = "") {
        addDisposable(Run(object : Callback<PwdUpdateActivity.PwdResult> {
            override fun call(): PwdUpdateActivity.PwdResult {
                val result = PwdUpdateActivity.PwdResult(false, "", "", "")
                answers.add(one + verifyQuestion[0].id)
                answers.add(two + verifyQuestion[1].id)
                answers.add(three + verifyQuestion[2].id)
                answers.sort()
                //密保问题
                val answerSort = StringBuffer()
                for (answer in answers) {
                    answerSort.append(answer)
                }
                val pwdSha = Sha512.from(Base58.encode(answerSort.toString().toByteArray(Charset.forName("UTF-8"))).toByteArray())

                val pass = CryptUtil.aesDecrypt(Arrays.copyOf(pwdSha.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(shared.getPwdCipher()), PreferencesHelper.getIv(pwdSha))
                if (pass == null) {
                    result.success = false
                    result.error = getView()?.context()!!.getString(R.string.question_error_str)
                    return result
                }
                if (!TextUtils.equals(String(pass), newPassword)) {

                    // 安全问题加密密码
                    val pwdSha2 = Sha512.from(Base58.encode(answerSort.toString().toByteArray(Charset.forName("UTF-8"))).toByteArray())
                    val encrypt = CryptUtil.aesEncrypt(Arrays.copyOf(pwdSha2.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), newPassword.toByteArray(), PreferencesHelper.getIv(pwdSha2))
                    val base58Encrypt = Base58.encode(encrypt)
                    shared.putPwdCipher(base58Encrypt)

                    // 需要加密一个验证密码的数据，用来判断下次用户输入的密码是否正确
                    val verifyPwdSha = Sha512.from(newPassword.toByteArray())
                    val verifyEncrypt = CryptUtil.aesEncrypt(Arrays.copyOf(verifyPwdSha.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), "Starteos.io".toByteArray(), PreferencesHelper.getIv(verifyPwdSha))
                    val verifyBase58Encrypt = Base58.encode(verifyEncrypt)
                    shared.putPwdVerify(verifyBase58Encrypt)

                    shared.putPwdTip(tip)


                    result.newPassword = newPassword
                    result.oldPassword = String(pass)
                    result.error = ""
                    result.success = true
                    return result
                } else {
                    result.success = false
                    result.error = getView()!!.context()!!.getString(R.string.old_password_same)
                    return result
                }
            }

        }).rxJava()
                .flatMap {
                    if (it.success) {
                        return@flatMap update(it.oldPassword, it.newPassword)
                    } else {
                        return@flatMap Flowable.error<Result>(Throwable(it.error))
                    }
                }.observeOn(androidSchedulers)
                .subscribeOn(ioSchedulers)
                .doOnSubscribe {
                    getView()?.startUpdate()
                }
                .subscribe(
                        {
                            if (it.success) {
                                getView()?.updateSuccess()
                            } else {
                                getView()?.updateError(it.error)
                            }
                        }, {
                    getView()?.updateError(if (it.message == null) "" else it.message!!)
                }
                ))
    }

    private fun update(oldPassword: String, newPassword: String): Flowable<Result> {
        return Run(object : Callback<Result> {
            override fun call(): Result {
                val result = Result()
                val data = dataManager.accountDao.getAllSync()
                for (i in 0 until data.size) {
                    val datum = data[i]
                    val pwdHas = Sha512.from(oldPassword.toByteArray())
                    val oldCipher = Base58.decode(datum.cipherText)
                    val privateKey = CryptUtil.aesDecrypt(Arrays.copyOf(pwdHas.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), oldCipher, PreferencesHelper.getIv(pwdHas))
                    if (privateKey == null) {
                        result.success = false
                        result.error = getView()?.context()!!.getString(R.string.password_error)
                        return result
                    }
                    // newPassword
                    val newPwdHas = Sha512.from(newPassword.toByteArray())
                    val enData = CryptUtil.aesEncrypt(Arrays.copyOf(newPwdHas.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), privateKey, PreferencesHelper.getIv(newPwdHas))
                    val newPrivateKey = Base58.encode(enData)
                    data[i].cipherText = newPrivateKey
                }
                dataManager.accountDao.insertAll(Array(data.size) {
                    data[it]
                })
                result.success = true
                return result
            }
        }).rxJava()

    }
}