package io.bos.accountmanager.core.storage.onedrive

import android.app.Activity
import android.app.Application
import android.util.Log
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.concurrency.SimpleWaiter
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.GraphErrorCodes
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.logger.DefaultLogger
import com.microsoft.graph.logger.ILogger
import com.microsoft.services.msa.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

abstract class MSAAuthAndroidAdapter constructor(application: Application) : IAuthenticationAdapter {
    /**
     * The authorization header name.
     */
    val AUTHORIZATION_HEADER_NAME = "Authorization"
    /**
     * The bearer prefix.
     */
    val OAUTH_BEARER_PREFIX = "Bearer "
    /**
     * The live auth client.
     */
    private val mLiveAuthClient: LiveAuthClient

    /**
     * The client id for this authenticator.
     * http://graph.microsoft.io/en-us/app-registration
     *
     * @return The client id.
     */
    abstract fun getClientId(): String

    /**
     * The scopes for this application.
     * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
     *
     * @return The scopes for this application.
     */
    abstract fun getScopes(): Array<String>

    /**
     * The logger instance.
     */
    private var mLogger: ILogger? = null

    init {
        mLogger = DefaultLogger()
        application.baseContext
        mLiveAuthClient = LiveAuthClient(
                application.applicationContext,
                getClientId(),
                Arrays.asList(*getScopes()),
                MicrosoftOAuth2Endpoint.instance)
    }

    override fun logout(callback: ICallback<Void>) {
        mLogger?.logDebug("Logout started")
        mLiveAuthClient.logout(object : LiveAuthListener {
            override fun onAuthError(exception: LiveAuthException?, userState: Any?) {
                val clientException = ClientException("Logout failure",
                        exception,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger?.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

            override fun onAuthComplete(status: LiveStatus?, session: LiveConnectSession?, userState: Any?) {
                callback.success(null)
            }

        })
    }

    override fun login(activity: Activity, callback: ICallback<HashMap<String, String>>) {
        if (hasValidSession()) {
            mLogger?.logDebug("Already logged in")
            callback.success(null)
            return
        }
        val listener = object : LiveAuthListener {
            override fun onAuthError(exception: LiveAuthException?, userState: Any?) {
                val clientException = ClientException("Login failure",
                        exception,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger?.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

            override fun onAuthComplete(status: LiveStatus?, session: LiveConnectSession?, userState: Any?) {
                mLogger?.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                        status,
                        session != null,
                        userState))
                if (status == LiveStatus.NOT_CONNECTED) {
                    mLogger?.logDebug("Received invalid login failure from silent authentication, ignoring.")
                    return
                }
                if (status == LiveStatus.CONNECTED) {
                    mLogger?.logDebug("Login completed")
                    val v = hashMapOf(Pair("accessToken", session!!.accessToken), Pair("expiresIn", session.expiresIn.toString()), Pair("refreshToken", session.refreshToken))
                    callback.success(v)
                    return
                }
                val clientException = ClientException("Unable to login successfully",
                        null,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger?.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

        }
        // Make sure the login process is started with the current activity information
        activity.runOnUiThread { mLiveAuthClient.login(activity, listener) }
    }

    private fun hasValidSession(): Boolean {
        return mLiveAuthClient.session != null && mLiveAuthClient.session.accessToken != null
    }

    override fun loginSilent(callback: ICallback<HashMap<String, String>>) {
        val listener = object : LiveAuthListener {
            override fun onAuthComplete(status: LiveStatus,
                                        session: LiveConnectSession?,
                                        userState: Any) {
                mLogger?.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                        status,
                        session !=
                                null,
                        userState))

                if (status == LiveStatus.CONNECTED) {
                    mLogger?.logDebug("Login completed")
                    val v = hashMapOf(Pair("accessToken", session!!.accessToken), Pair("expiresIn", session.expiresIn.toString()), Pair("refreshToken", session.refreshToken))
                    callback.success(v)
                    return
                }

                val clientException = ClientException("Unable to login silently", null,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger?.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

            override fun onAuthError(exception: LiveAuthException, userState: Any) {
                val clientException = ClientException("Unable to login silently", null,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger?.logError(clientException.message, clientException)
                callback.failure(clientException)
            }
        }
        mLiveAuthClient.loginSilent(listener)
    }

    override fun getAccessToken(): String {
        if (hasValidSession()) {
            mLogger?.logDebug("Found account information")
            if (mLiveAuthClient.session.isExpired) {
                mLogger?.logDebug("AccountTable access token is expired, refreshing")
                loginSilentBlocking()
            }
            return mLiveAuthClient.session.accessToken
        } else {
            val message = "Unable to get access token, No active account found"
            val exception = ClientException(message,
                    null,
                    GraphErrorCodes.AuthenticationFailure)
            mLogger?.logError(message, exception)
            throw exception
        }
    }

    /**
     * Login silently while blocking for the call to return
     *
     * @return the result of the login attempt
     * @throws ClientException The exception if there was an issue during the login attempt
     */
    @Throws(ClientException::class)
    private fun loginSilentBlocking(): HashMap<String, String> {
        val waiter = SimpleWaiter()
        val returnValue = AtomicReference<HashMap<String, String>>()
        val exceptionValue = AtomicReference<ClientException>()

        loginSilent(object : ICallback<HashMap<String, String>> {
            override fun success(hashMap: java.util.HashMap<String, String>) {
                returnValue.set(hashMap)
                waiter.signal()
            }

            override fun failure(ex: ClientException) {
                exceptionValue.set(ex)
                waiter.signal()
            }
        })

        waiter.waitForSignal()


        if (exceptionValue.get() != null) {
            throw exceptionValue.get()
        }

        return returnValue.get()
    }

    override fun authenticateRequest(request: IHttpRequest?) {
        // If the request already has an authorization header, do not intercept it.
        request?.headers?.forEach {
            if (it.name == AUTHORIZATION_HEADER_NAME) {
                Log.d("HaiChecker", "Found an existing authorization header!")
                return
            }
        }

        try {
            val accessToken = getAccessToken()
            request?.addHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + accessToken)
        } catch (e: ClientException) {
            val message = "Unable to authenticate request, No active account found"
            val exception = ClientException(message,
                    e,
                    GraphErrorCodes.AuthenticationFailure)
            mLogger?.logError(message, exception)
            throw exception
        }

    }
}