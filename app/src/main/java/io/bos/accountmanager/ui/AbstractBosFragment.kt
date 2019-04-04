package io.bos.accountmanager.ui

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.tu.loadingdialog.LoadingDailog
import io.bos.accountmanager.di.component.ActivityComponent

abstract class AbstractBosFragment : Fragment() {

    @LayoutRes
    abstract fun fragmentLayout(): Int

    private var mActivity: AbstractBosActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AbstractBosActivity) {
            mActivity = context
        }
    }

    fun getActivityComponent(): ActivityComponent? {
        return mActivity?.getActivityComponent()
    }

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(fragmentLayout(), container, false)
    }

    fun toast(msg: String): LoadingDailog.Builder {
        val loadBuilder = LoadingDailog.Builder(context)
                .setMessage(msg)
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false)
        return loadBuilder
    }
}