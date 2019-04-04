package io.bos.accountmanager.presenter

import io.bos.accountmanager.view.AbstractView

interface AbstractPresenterImpl<V : AbstractView> {
    /**
     * 界面View加载完成
     */
    fun attachView(view: V)

    /**
     * 界面销毁
     */
    fun detachView()

    /**
     * 获取当前View
     */
    fun getView(): V?

    /**
     * 当前界面是否关闭
     */
    fun isViewAttached(): Boolean
}