package io.bos.accountmanager.view

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.os.IBinder

interface AbstractView {
    fun owner(): LifecycleOwner
    /**
     * 隐藏界面的软键盘
     */
    fun hideKeyboard()

    /**
     * 隐藏View的软键盘
     */
    fun hideKeyboard(windowToken: IBinder?)

    /**
     * 上下文
     */
    fun context(): Context?
}