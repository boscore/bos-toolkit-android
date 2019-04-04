package io.bos.accountmanager.di.component

import android.app.Application
import dagger.Component
import io.bos.accountmanager.di.module.AppModule
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.core.utils.Question
import io.bos.accountmanager.data.local.DataManager
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.net.NetManager
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    abstract fun inject(app: BOSApplication)
    abstract fun application(): Application
    abstract fun dataManager(): DataManager
    abstract fun preferences(): PreferencesHelper
    abstract fun questions(): Question
    abstract fun netManger(): NetManager
}