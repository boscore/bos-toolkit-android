package io.bos.accountmanager.ui

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.AbstractPresenter
import io.bos.accountmanager.view.AbstractView
import javax.inject.Inject

abstract class AbstractFragment<V : AbstractView, P : AbstractPresenter<V>> : AbstractBosFragment(), AbstractView {
    @Inject
    lateinit var presenter: P

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInjects(getActivityComponent())
        presenter.attachView(attachView())
    }

    abstract fun attachView(): V

    abstract fun initInjects(activityComponent: ActivityComponent?)
}