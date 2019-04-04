package io.bos.accountmanager.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.utils.Question
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.PwdUpdatePresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.view.PwdUpdateView
import kotlinx.android.synthetic.main.activity_pwd_confirm_update.*

/**
 * 确认修改
 */
@Route(path = Constants.RoutePath.ACTIVITY.PWD_CONFIRM_UPDATE_ACTIVITY)
class PwdConfirmUpdateActivity : AbstractActivity<PwdUpdateView, PwdUpdatePresenter>(), PwdUpdateView {
    @Autowired
    @JvmField
    var type: Int = 0   //0是修改密码   1是找回密码

    // 传过来的问题
    @Autowired
    lateinit var verifyString: String

    @Autowired
    lateinit var one: String

    @Autowired
    lateinit var two: String

    @Autowired
    lateinit var three: String

    //获取问题对象
    private var verifyQuestion: ArrayList<Question.QuestionItem> = ArrayList()
    private val answers = ArrayList<String>()


    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    var dialog: LoadingDailog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): PwdUpdateView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_pwd_confirm_update
    }

    override fun startUpdate() {

    }

    override fun updateError(error: String) {
        dialog?.dismiss()
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun updateSuccess() {
        dialog?.dismiss()
        Toast.makeText(this, getString(R.string.change_pwd_success), Toast.LENGTH_LONG).show()
        ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)

        var list = Gson().fromJson<ArrayList<Question.QuestionItem>>(verifyString, object : com.google.common.reflect.TypeToken<ArrayList<Question.QuestionItem>>() {}.type)
        verifyQuestion.addAll(list)


        var loadBuilder = LoadingDailog.Builder(this@PwdConfirmUpdateActivity)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        pwd_confirm.setOnClickListener {
            if (TextUtils.isEmpty(pwd_edit_new.text.toString()) && TextUtils.isEmpty(pwd_edit_repeat.text.toString())) {
                Toast.makeText(this@PwdConfirmUpdateActivity, resources.getString(R.string.pwd_update_err_new), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pwd_edit_new.text!!.length < 8 || pwd_edit_repeat.text!!.length < 8) {
                Toast.makeText(this@PwdConfirmUpdateActivity, resources.getString(R.string.pwd_update_err_num), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!TextUtils.equals(pwd_edit_new.text.toString(), pwd_edit_repeat.text.toString())) {
                Toast.makeText(this@PwdConfirmUpdateActivity, resources.getString(R.string.pwd_update_err_identical), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //0是修改密码   1是找回密码
            if (type == 0) {
                pwdView?.decryption(shared.getPwdVerify(), "verify")
            } else {
                dialog?.show()
                presenter.verification(answers, verifyQuestion, one, two, three, pwd_edit_new.text.toString(), pwd_edit_hint.text.toString())
            }

        }

        back.setOnClickListener {
            finish()
        }

        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {

                dialog?.show()
                presenter.verification(answers, verifyQuestion, one, two, three, pwd_edit_new.text.toString(), pwd_edit_hint.text.toString())

            }

        }
    }
}
