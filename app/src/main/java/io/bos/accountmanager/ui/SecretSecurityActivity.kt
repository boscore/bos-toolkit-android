package io.bos.accountmanager.ui

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.BuildConfig
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.core.storage.core.StorageRequest
import io.bos.accountmanager.core.utils.Question
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import kotlinx.android.synthetic.main.activity_secret_security.*
import rx.Observable
import rx.Subscription
import rx.functions.Func3
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates

/**
 * 设置密保
 */
@Route(path = Constants.RoutePath.ACTIVITY.SECRET_SECURITY_ACTIVITY)
class SecretSecurityActivity : AbstractBosActivity() {
    override fun byId(): Int {
        return R.layout.activity_secret_security
    }

    var secretProtection: HashMap<String, Question.QuestionItem> = HashMap()

    // 用户设置的密码
    @Autowired
    lateinit var password: String
    // 用户设置的密码提示
    @Autowired
    lateinit var tips: String
    // 存储对象
    lateinit var helper: PreferencesHelper
    // 安全问题对象
//    lateinit var question: Question

    // 表单判断，后期需要关闭
    private var subscription: Subscription? = null

    private val answers = ArrayList<String>()

    // 存入的三个安全问题
    private val verifyQuestion: ArrayList<Question.QuestionItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
    }

    override fun init() {
        super.init()
        helper = BOSApplication.get(this).getAppComponent().preferences()
        secretProtection = hashMapOf(
                Pair("5d863b4ad885541c", Question.QuestionItem(resources.getString(R.string.question_txt_mother), "5d863b4ad885541c")),
                Pair("8906a783203f342d", Question.QuestionItem(resources.getString(R.string.question_txt_father), "8906a783203f342d")),
                Pair("083c384b736517d3", Question.QuestionItem(resources.getString(R.string.question_txt_lover), "083c384b736517d3")),
                Pair("b50f73acff178f79", Question.QuestionItem(resources.getString(R.string.question_txt_friend_name), "b50f73acff178f79")),
                Pair("6bd0447d6aeb5455", Question.QuestionItem(resources.getString(R.string.question_txt_son_name), "6bd0447d6aeb5455")),
                Pair("e59ac9f21816c63f", Question.QuestionItem(resources.getString(R.string.question_txt_nickname), "e59ac9f21816c63f")),
                Pair("ec46070d79755525", Question.QuestionItem(resources.getString(R.string.question_txt_pet_name), "ec46070d79755525")),
                Pair("b80f3f8ee00dfb82", Question.QuestionItem(resources.getString(R.string.question_txt_graduation), "b80f3f8ee00dfb82")),
                Pair("0c606eb37b2afeac", Question.QuestionItem(resources.getString(R.string.question_txt_born_place), "0c606eb37b2afeac")),
                Pair("b11bf1929c4c914f", Question.QuestionItem(resources.getString(R.string.question_txt_mother_was_born), "b11bf1929c4c914f")),
                Pair("7461ae46c81bbfc6", Question.QuestionItem(resources.getString(R.string.question_txt_mothers_birthday), "7461ae46c81bbfc6")),
                Pair("da0ce1afca0a76fe", Question.QuestionItem(resources.getString(R.string.question_txt_fathers_birthday), "da0ce1afca0a76fe")),
                Pair("9e065595a35ff706", Question.QuestionItem(resources.getString(R.string.question_txt_love_birthday), "9e065595a35ff706")),
                Pair("b1a71ca4905a94fd", Question.QuestionItem(resources.getString(R.string.question_txt_friends_birthday), "b1a71ca4905a94fd")),
                Pair("3acb55eae9c8e9bd", Question.QuestionItem(resources.getString(R.string.question_txt_dirst_child), "3acb55eae9c8e9bd")),
                Pair("772bd405ceb2dc69", Question.QuestionItem(resources.getString(R.string.question_txt_second_child), "772bd405ceb2dc69")),
                Pair("ca6e2a572d3cef31", Question.QuestionItem(resources.getString(R.string.question_txt_birthday), "ca6e2a572d3cef31")),
                Pair("1bfee0b8035384bf", Question.QuestionItem(resources.getString(R.string.question_txt_unforgettable), "1bfee0b8035384bf")),
                Pair("e4aeb36924e07f6a", Question.QuestionItem(resources.getString(R.string.question_txt_travel_far), "e4aeb36924e07f6a")),
                Pair("f37c2034ae7c925b", Question.QuestionItem(resources.getString(R.string.question_txt_marry), "f37c2034ae7c925b")),
                Pair("67cdb073cc980faa", Question.QuestionItem(resources.getString(R.string.question_txt_fruits), "67cdb073cc980faa")),
                Pair("377c08d8837b0384", Question.QuestionItem(resources.getString(R.string.question_txt_food), "377c08d8837b0384")),
                Pair("d15045def02e4925", Question.QuestionItem(resources.getString(R.string.question_txt_like_things), "d15045def02e4925")),
                Pair("ff8a6a0ed55c14b1", Question.QuestionItem(resources.getString(R.string.question_txt_what_you_hate_todo), "ff8a6a0ed55c14b1")),
                Pair("22ab11aeb625c19c", Question.QuestionItem(resources.getString(R.string.question_txt_favorite_colours), "22ab11aeb625c19c")),
                Pair("a2a6208e7021e8cd", Question.QuestionItem(resources.getString(R.string.question_txt_motion), "a2a6208e7021e8cd")),
                Pair("3d6b67f5251e3d0c", Question.QuestionItem(resources.getString(R.string.question_txt_city), "3d6b67f5251e3d0c")),
                Pair("e0fe285d11b8aa88", Question.QuestionItem(resources.getString(R.string.question_txt_tourism), "e0fe285d11b8aa88")),
                Pair("bc7d08e04bb201a0", Question.QuestionItem(resources.getString(R.string.question_txt_disgusting), "bc7d08e04bb201a0"))

        )

    }

    override fun data() {
        super.data()
        // 第一次开始获取三个随机问题
        verifyQuestion.clear()
        verifyQuestion.addAll(getQuestion())
        secret_txt_one.text = verifyQuestion[0].question
        secret_txt_two.text = verifyQuestion[1].question
        secret_txt_three.text = verifyQuestion[2].question
    }

    override fun listener() {
        super.listener()

        back.setOnClickListener {
            finish()
        }
        // 设置换一个的事件
        secret_linear_click_one.setOnClickListener(refreshOnClickListener)
        secret_linear_click_two.setOnClickListener(refreshOnClickListener)
        secret_linear_click_three.setOnClickListener(refreshOnClickListener)
        // 表单列表
        val question_1 = RxTextView.textChanges(secret_edit_one)
        val question_2 = RxTextView.textChanges(secret_edit_two)
        val question_3 = RxTextView.textChanges(secret_edit_three)

        subscription = Observable.combineLatest(question_1, question_2, question_3, object : Func3<CharSequence, CharSequence, CharSequence, Pair<Boolean, String>> {
            override fun call(t1: CharSequence?, t2: CharSequence?, t3: CharSequence?): Pair<Boolean, String> {

                val empty = !TextUtils.isEmpty(t1) && !TextUtils.isEmpty(t2) && !TextUtils.isEmpty(t3)
                if (!empty) {
                    return false to getString(R.string.answer_empty)
                }

                val length = (t1!!.length > 1 && t1.length <= 16) && (t2!!.length > 1 && t2.length <= 16) && (t3!!.length > 1 && t3.length <= 16)
                if (!length) {
                    return false to getString(R.string.answer_length_error)
                }

                val eq = (!TextUtils.equals(t1, t2) && !TextUtils.equals(t1, t3) && !TextUtils.equals(t2, t3))
                if (!eq) {
                    return false to getString(R.string.answer_repeat)
                }


                answers.clear()
                answers.add(t1.toString() + verifyQuestion[0].id)
                answers.add(t2.toString() + verifyQuestion[1].id)
                answers.add(t3.toString() + verifyQuestion[2].id)
                return true to ""
            }
        }).subscribe {
            RxView.enabled(secret_login).call(it.first)
            if (!it.first) {
                Log.e("HaiChecker", it.second)
                secret_login.text = it.second
                if (!BuildConfig.DEBUG) {
                    secret_login.text = it.second
                } else {
                }
            } else {
                secret_login.text = resources.getString(R.string.security_hint_found)
            }
        }
        secret_login.setOnClickListener { itButton ->
            Run(object : Callback<Result> {
                override fun call(): Result {
                    // 答案排序
                    answers.sort()
                    val answerSort = StringBuffer()
                    for (answer in answers) {
                        answerSort.append(answer)
                    }
                    val result = Result()
                    // 安全问题加密密码
                    val pwdSha = Sha512.from(Base58.encode(answerSort.toString().toByteArray(Charset.forName("UTF-8"))).toByteArray())
                    val encrypt = CryptUtil.aesEncrypt(Arrays.copyOf(pwdSha.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), password.toByteArray(), PreferencesHelper.getIv(pwdSha))
                    val base58Encrypt = Base58.encode(encrypt)

                    // 需要加密一个验证密码的数据，用来判断下次用户输入的密码是否正确
                    val verifyPwdSha = Sha512.from(password.toByteArray())
                    val verifyEncrypt = CryptUtil.aesEncrypt(Arrays.copyOf(verifyPwdSha.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), "Starteos.io".toByteArray(), PreferencesHelper.getIv(verifyPwdSha))
                    val verifyBase58Encrypt = Base58.encode(verifyEncrypt)

                    // 需要两个加密数据、密码提示、选择的问题同时存储成功，否则删除所有加密，并且状态位无密码状态，需要用户重新设置密码
                    result.success = helper.putPwdCipher(base58Encrypt) && helper.putPwdVerify(verifyBase58Encrypt) && helper.putPwdTip(tips) && helper.putQuestion(Gson().toJson(verifyQuestion))
                    if (!result.success) {
                        result.error = "Shared Storage Error"
                        // 清空加密数据
                        helper.putPwdCipher("")
                        helper.putPwdVerify("")
                        helper.putPwdTip("")
                        helper.putQuestion("")
                    }
                    return result
                }
            }).rxJava()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .doOnSubscribe {
                        itButton.isEnabled = false
                    }
                    .subscribe({
                        if (it.success) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this, it.error, Toast.LENGTH_LONG).show()
                        }
                    }, {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }, {
                        itButton.isEnabled = true
                    })
        }
    }

    private val refreshOnClickListener = View.OnClickListener { v ->
        when (v?.id) {
            R.id.secret_linear_click_one -> {
                val q = refresh(verifyQuestion)
                verifyQuestion[0] = q
                secret_txt_one.text = verifyQuestion[0].question
            }
            R.id.secret_linear_click_two -> {
                val q = refresh(verifyQuestion)
                verifyQuestion[1] = q

                secret_txt_two.text = verifyQuestion[1].question
            }
            R.id.secret_linear_click_three -> {
                val q = refresh(verifyQuestion)
                verifyQuestion[2] = q
                secret_txt_three.text = verifyQuestion[2].question
            }
        }
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }


    /**
     * 获取三个问题
     */
    fun getQuestion(): ArrayList<Question.QuestionItem> {
        val q1 = secretProtection.keys.elementAt(getQuestionRandom())
        var q2 = secretProtection.keys.elementAt(getQuestionRandom())
        while (TextUtils.equals(q2, q1)) {
            q2 = secretProtection.keys.elementAt(getQuestionRandom())
        }
        var q3 = secretProtection.keys.elementAt(getQuestionRandom())
        while (TextUtils.equals(q3, q1) || TextUtils.equals(q3, q2)) {
            q3 = secretProtection.keys.elementAt(getQuestionRandom())
        }

        return arrayListOf(secretProtection[q1]!!, secretProtection[q2]!!, secretProtection[q3]!!)
    }

    private fun getQuestionRandom(): Int {
        return ((1 + Math.random() * (secretProtection.size - 1 - 1 + 1)).toInt())
    }

    /**
     * 刷新获取一个问题
     */
    fun refresh(data: ArrayList<Question.QuestionItem>): Question.QuestionItem {
        var q = secretProtection.keys.elementAt(getQuestionRandom())
        while (data.contains(secretProtection[q])) {
            q = secretProtection.keys.elementAt(getQuestionRandom())
        }
        return secretProtection[q]!!
    }
}
