package io.bos.accountmanager.di.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.hconline.iso.di.ActivityContext
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(activity: AppCompatActivity) {
    private var mActivity: AppCompatActivity = activity

    @Provides
    @ActivityContext
    internal fun provideContext(): Context {
        return mActivity
    }

    @Provides
    internal fun provideActivity(): AppCompatActivity {
        return mActivity
    }
}