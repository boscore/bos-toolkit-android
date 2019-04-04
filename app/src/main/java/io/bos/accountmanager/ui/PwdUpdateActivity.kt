package io.bos.accountmanager.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.core.utils.Question
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.data.local.prefs.PreferencesHelper_Factory
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.MainPresenter
import io.bos.accountmanager.presenter.PwdUpdatePresenter
import io.bos.accountmanager.ui.dialog.TipsSureDialog
import io.bos.accountmanager.view.MainView
import io.bos.accountmanager.view.PwdUpdateView
import kotlinx.android.synthetic.main.activity_pwd_update.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

/**
 * 修改密码
 */
@Route(path = Constants.RoutePath.ACTIVITY.PWD_UPDATE_ACTIVITY)
class PwdUpdateActivity : AbstractActivity<PwdUpdateView, PwdUpdatePresenter>(), PwdUpdateView {


    @Autowired
    @JvmField
    var type: Int = 0   //0是修改密码   1是找回密码
    var dialog: LoadingDailog? = null
    // 安全问题对象
//    lateinit var question: Question
    private var tipsSureDialog: TipsSureDialog? = null
    var secretProtection: HashMap<String, Question.QuestionItem> = HashMap()

    override fun startUpdate() =
            Toast.makeText(this, getString(R.string.change_pwd_start), Toast.LENGTH_LONG).show()

    override fun updateError(error: String) = Toast.makeText(this, error, Toast.LENGTH_LONG).show()

    override fun updateSuccess() {
        Toast.makeText(this, getString(R.string.change_pwd_success), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun seetPwd(date: String) {
        super.seetPwd(date)
        dialog?.dismiss()
        tipsSureDialog = TipsSureDialog.newInstance(date, resources.getString(R.string.cloud_management_cancel), resources.getString(R.string.quote_btn_confirm))
        tipsSureDialog?.tipsSureCallback = object : TipsSureDialog.TipsSureCallback {
            override fun onCancelLeftClick() {
                tipsSureDialog?.dismiss()

            }

            override fun onSureRightClick() {
                tipsSureDialog?.dismiss()

            }

        }
        tipsSureDialog?.show(supportFragmentManager, "AccountList")
    }

    override fun errSet(err: String) {
        super.errSet(err)
        dialog?.dismiss()
        Toast.makeText(this, err, Toast.LENGTH_LONG).show()
    }

    private lateinit var shared: PreferencesHelper
    //获取问题对象
    private var verifyQuestion: ArrayList<Question.QuestionItem> = ArrayList()
    private val answers = ArrayList<String>()

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): PwdUpdateView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_pwd_update
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        //0是修改密码   1是找回密码
        if (type == 0) {
            pwd_confirm.text = resources.getString(R.string.pwd_txt_updet_title)

        } else {
            pwd_confirm.text = resources.getString(R.string.quote_btn_confirm)
        }

        var loadBuilder = LoadingDailog.Builder(this@PwdUpdateActivity)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();


    }

    override fun data() {
        super.data()
        val j = JsonParser().parse(shared.getQuestion())
        if (j.isJsonArray) {
            for (jsonElement in j.asJsonArray) {
                val json = jsonElement.asJsonObject
                val item = Question.QuestionItem(json.get("question").asString, json.get("id").asString)
                verifyQuestion.add(item)
            }
        }
        update_txt_one.text = String.format("1.%s", getSecretProtection(verifyQuestion[0]))
        update_txt_two.text = String.format("2.%s", getSecretProtection(verifyQuestion[1]))
        update_txt_three.text = String.format("3.%s", getSecretProtection(verifyQuestion[2]))
    }

    override fun init() {
        super.init()
        shared = BOSApplication.get(this).getAppComponent().preferences()
//        question = BOSApplication.get(this).getAppComponent().questions()
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


    override fun listener() {
        super.listener()
        back.setOnClickListener {
            finish()
        }

        pwd_confirm.setOnClickListener { it1 ->
            if (TextUtils.isEmpty(update_edit_one.text.toString()) || TextUtils.isEmpty(update_edit_two.text.toString()) || TextUtils.isEmpty(update_edit_three.text.toString())) {
                Toast.makeText(this@PwdUpdateActivity, resources.getString(R.string.pwd_update_err_empty), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //0是修改密码   1是找回密码
            if (type == 0) {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.PWD_CONFIRM_UPDATE_ACTIVITY)
                        .withString("one", update_edit_one.text.toString().trim())
                        .withString("two", update_edit_two.text.toString().trim())
                        .withString("three", update_edit_three.text.toString().trim())
                        .withString("verifyString", Gson().toJson(verifyQuestion))
                        .withInt("type", type)
                        .navigation()

            } else {

                if (NoDoubleClickUtils.isDoubleClick()) {
                    dialog?.show()
                    answers.clear()
                    presenter.seePassword(answers, verifyQuestion, update_edit_one.text.toString(), update_edit_two.text.toString(), update_edit_three.text.toString())
                }

            }

        }
    }

    object NoDoubleClickUtils {
        private val SPACE_TIME = 500//2次点击的间隔时间，单位ms
        private var lastClickTime: Long = 0
        fun isDoubleClick(): Boolean {
            val currentTime = System.currentTimeMillis()
            val isClick: Boolean
            if (currentTime - lastClickTime > SPACE_TIME) {
                isClick = true
            } else {
                isClick = false
            }
            lastClickTime = currentTime
            return isClick
        }
    }

    class PwdResult(success: Boolean, error: String, var oldPassword: String, var newPassword: String) : Result(success, error)

    fun getSecretProtection(verify: Question.QuestionItem): String {
        return secretProtection[verify.id]!!.question

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
