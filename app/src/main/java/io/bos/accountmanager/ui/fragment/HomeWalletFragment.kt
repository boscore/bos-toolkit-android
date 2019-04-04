package io.bos.accountmanager.ui.fragment

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import io.bos.accountmanager.R
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.Constants
import io.bos.accountmanager.MainActivity
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.ui.AbstractBosFragment
import kotlinx.android.synthetic.main.activity_home_wallet__fragment.*
import java.util.ArrayList


/**
 * 钱包首页
 */

@Route(path = Constants.RoutePath.FRAGMENT.HOME_WALLET_FRAGMENT)
class HomeWalletFragment : AbstractBosFragment() {


    override fun fragmentLayout(): Int {

        return R.layout.activity_home_wallet__fragment
    }

    private var dataSource: ArrayList<AccountTable> = ArrayList<AccountTable>()


    fun onBalance() {
        swipe.isRefreshing = false

    }

    fun onAccounts(data: ArrayList<AccountTable>) {
        dataSource.clear()
        dataSource.addAll(data)
        adapter.notifyDataSetChanged()


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWindowStatusBarColor(activity!!)
        }
        var imge = view.findViewById<ImageView>(R.id.imput_image_close)
        imge.setOnClickListener({
            val popupwindow_menu = layoutInflater.inflate(R.layout.pop_man_select, null, false)
            var pop = PopupWindow(popupwindow_menu, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true)
            pop.showAsDropDown(imge, 0, -50)

            popupwindow_menu.setOnTouchListener { _, _ ->
                if (pop.isShowing) {
                    pop.dismiss()
                }
                false
            }
            var popRedCore = popupwindow_menu.findViewById<RelativeLayout>(R.id.pop_red_core)
            //点击红包
            popRedCore.setOnClickListener({
                if (pop.isShowing) {
                    pop.dismiss()
                }

                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY)
                        .navigation(context)
            })


            //导入账号
            var popImport = popupwindow_menu.findViewById<RelativeLayout>(R.id.pop_import)
            //点击红包
            popImport.setOnClickListener({
                if (pop.isShowing) {
                    pop.dismiss()
                }
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.IMPORT_ACCOUNT_ACTIVITY)
                        .navigation(context)
            })


        })

        imput_recycler.layoutManager = LinearLayoutManager(context)
        imput_recycler.adapter = adapter
        adapter.setEnableLoadMore(false)

        swipe.setOnRefreshListener {
            if (activity is MainActivity) {
                (activity as MainActivity).getBalance()
            }
        }
        if (activity is MainActivity) {
            (activity as MainActivity).getBalance()
        }

    }


    private val adapter = object : BaseQuickAdapter<AccountTable, BaseViewHolder>(R.layout.item_wallet_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: AccountTable?) {
            val item_wallet_click = helper!!.getView<RelativeLayout>(R.id.item_wallet_click)
            val item_wallet_backups = helper.getView<ImageView>(R.id.item_wallet_backups)
            val item_wallet_name = helper.getView<TextView>(R.id.item_wallet_name)
            helper.setText(R.id.item_wallet_balance, item?.balance)
            item_wallet_name.text = item?.accountName
            if (item?.backup == true) {
                item_wallet_backups.visibility = View.VISIBLE
            } else {
                item_wallet_backups.visibility = View.GONE
            }
            item_wallet_click.setOnClickListener {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WALLET_MANAGE_ACTIVITY).withString("accountName", item?.accountName)
                        .navigation()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun setWindowStatusBarColor(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(R.color.colorWhite)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            //底部导航栏
            //window.setNavigationBarColor(activity.getResources().getColor(colorResId))
        }

    }
}
