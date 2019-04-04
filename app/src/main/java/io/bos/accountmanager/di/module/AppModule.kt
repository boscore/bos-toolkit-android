package io.bos.accountmanager.di.module

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.BuildConfig
import io.bos.accountmanager.Constants
import io.bos.accountmanager.data.local.db.DataBase
import io.bos.accountmanager.data.local.db.dao.AccountDao
import io.bos.accountmanager.data.local.db.dao.EstablishAccountDao
import io.bos.accountmanager.di.ApplicationContext
import io.bos.accountmanager.net.EOSApiService
import io.reactivex.schedulers.Schedulers
import io.starteos.jeos.net.StartEOS
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule(mApplication: BOSApplication) {
    private val mApp: BOSApplication = mApplication

    companion object {
        val executor = ThreadPoolExecutor(2,
                24,
                5L,
                TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(256),
                ThreadPoolExecutor.DiscardOldestPolicy())
        val scheduler = Schedulers.from(executor)
        val factory = RxJava2CallAdapterFactory.createWithScheduler(scheduler)
    }

    @Provides
    fun provideApp(): Application = mApp

    @Provides
    @ApplicationContext
    fun provideAppContext(): Context = mApp

    @Provides
    @Singleton
    fun startHttp(): StartEOS {
        return StartFactory.build(HttpService(Constants.Const.URL))
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideAccountDao(dataBase: DataBase): AccountDao {
        return dataBase.accountDao()
    }

    @Provides
    @Singleton
    fun provideEstablishAccountDao(dataBase: DataBase): EstablishAccountDao {
        return dataBase.establishAccount()
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DataBase {
        return Room.databaseBuilder(context, DataBase::class.java, "bos.db").build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(factory)
                .client(okHttpClient)
                .build()
    }


    @Provides
    @Singleton
    fun eOSApiModule(okHttpClient: OkHttpClient): EOSApiService {
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.Const.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .callbackExecutor(executor)
                .addCallAdapterFactory(factory)
                .client(okHttpClient)
                .build()
        return retrofit.create(EOSApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideHttp(): OkHttpClient {
        val ilog = HttpLoggingInterceptor { message ->
            if (BuildConfig.DEBUG) {
                if (message.contains("-->") && message.contains("http")) {
                    Log.e("", "\r\n\r\n\r\n")
                    Log.e("okhttp开始请求", "------------------------------------------------------------------------------------------------------")
                }
                if (message.contains("<--") && message.contains("ms")) {
                    Log.e("", "\r\n\r\n\r\n")
                    Log.e("okhttp开始响应", "------------------------------------------------------------------------------------------------------")
                }
                //                    Log.e("okhttp　　　　　", "" + message);
                Log.e("okhttp　　　　　", message)
                if (message.contains("-->") && message.contains("END")) {
                    Log.e("okhttp结束请求", "------------------------------------------------------------------------------------------------------")
                    Log.e("", "\r\n\r\n\r\n")
                }
                if (message.contains("<--") && message.contains("END")) {
                    Log.e("okhttp结束响应", "------------------------------------------------------------------------------------------------------")
                    Log.e("", "\r\n\r\n\r\n")
                }
            }
        }
        ilog.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(ilog)
                .build()
    }
}