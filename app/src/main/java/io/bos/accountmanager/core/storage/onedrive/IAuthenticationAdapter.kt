package io.bos.accountmanager.core.storage.onedrive;

import android.app.Activity
import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException

interface IAuthenticationAdapter : IAuthenticationProvider {

    fun logout(callback: ICallback<Void>);

    fun login(activity: Activity, callback: ICallback<HashMap<String, String>>)

    fun loginSilent(callback: ICallback<HashMap<String, String>>)

    @Throws(ClientException::class)
    fun getAccessToken(): String
}