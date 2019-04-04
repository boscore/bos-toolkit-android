package io.bos.accountmanager.utils

import android.content.Context
import android.content.SharedPreferences

import java.util.Locale

/**
 * Created by Administrator on 2019/1/9/009.
 */

class SPUtil(context: Context) {
    private val SP_NAME = "language_setting"
    private val TAG_LANGUAGE = "language_select"
    private val TAG_SYSTEM_LANGUAGE = "system_language"

    private val mSharedPreferences: SharedPreferences

    var systemCurrentLocal = Locale.ENGLISH

    val selectLanguage: Int
        get() = mSharedPreferences.getInt(TAG_LANGUAGE, 0)


    init {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }


    fun saveLanguage(select: Int) {
        val edit = mSharedPreferences.edit()
        edit.putInt(TAG_LANGUAGE, select)
        edit.commit()
    }

    companion object {
        @Volatile private var instance: SPUtil? = null

        fun getInstance(context: Context): SPUtil? {
            if (instance == null) {
                synchronized(SPUtil::class.java) {
                    if (instance == null) {
                        instance = SPUtil(context)
                    }
                }
            }
            return instance
        }
    }
}
