package io.bos.accountmanager.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.DisplayMetrics
import android.util.Log

import java.util.Locale

import io.bos.accountmanager.R

/**
 *
 * Created by Administrator on 2019/1/9/009.
 */

object LocalManageUtil {
    private val TAG = "LocalManageUtil"

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    fun getSystemLocale(context: Context): Locale {
        return SPUtil.getInstance(context)!!.systemCurrentLocal
    }

    fun getSelectLanguage(context: Context): String {
        when (SPUtil.getInstance(context)!!.selectLanguage) {
            0 -> return context.getString(R.string.language_auto)
            1 -> return context.getString(R.string.language_cn)
            2 -> return context.getString(R.string.language_traditional)
            3 -> return context.getString(R.string.language_en)
            else -> return context.getString(R.string.language_en)
        }
    }

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    fun getSetLanguageLocale(context: Context): Locale {

        when (SPUtil.getInstance(context)!!.selectLanguage) {
            0 -> return getSystemLocale(context)
            1 -> return Locale.CHINA
            2 -> return Locale.TAIWAN
            3 -> return Locale.ENGLISH
            else -> return Locale.ENGLISH
        }
    }

    fun saveSelectLanguage(context: Context, select: Int) {
        SPUtil.getInstance(context)!!.saveLanguage(select)
        setApplicationLanguage(context)
    }

    fun setLocal(context: Context): Context {
        return updateResources(context, getSetLanguageLocale(context))
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        var context = context
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    /**
     * 设置语言类型
     */
    fun setApplicationLanguage(context: Context) {
        val resources = context.applicationContext.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        val locale = getSetLanguageLocale(context)
        config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.locales = localeList
            context.applicationContext.createConfigurationContext(config)
            Locale.setDefault(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    fun saveSystemCurrentLanguage(context: Context) {
        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0)
        } else {
            locale = Locale.getDefault()
        }
        Log.d(TAG, locale.language)
        SPUtil.getInstance(context)!!.systemCurrentLocal = locale
    }

    fun onConfigurationChanged(context: Context) {
        saveSystemCurrentLanguage(context)
        setLocal(context)
        setApplicationLanguage(context)
    }
}
