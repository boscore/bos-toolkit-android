package io.bos.accountmanager.core.storage

import android.annotation.SuppressLint
import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.concurrency.SimpleWaiter
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.core.GraphErrorCodes
import com.microsoft.graph.core.IClientConfig
import com.microsoft.graph.extensions.*
import com.microsoft.graph.logger.LoggerLevel
import io.bos.accountmanager.core.storage.core.CallBack
import io.bos.accountmanager.core.storage.core.StorageRequest
import io.bos.accountmanager.core.storage.core.response.*
import io.bos.accountmanager.core.storage.onedrive.IAuthenticationAdapter
import io.bos.accountmanager.core.storage.onedrive.MSAAuthAndroidAdapter
import io.bos.accountmanager.core.storage.protocol.Storage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

//import com.microsoft.

class OneDrive private constructor(val activity: Activity) : Storage {

    private var mClintConfig: IClientConfig
    var mClint: IGraphServiceClient
    var auth: IAuthenticationAdapter

    companion object {
        const val APP_FOLDER_PATH = "backup"
        const val CLIENT_ID = "3e3a6600-2c3d-445d-8ad3-07940921c25d"
        @SuppressLint("StaticFieldLeak")
        private var instance: OneDrive? = null

        @Synchronized
        fun get(activity: Activity): OneDrive {
            synchronized(OneDrive::class.java) {
                if (instance == null) {
                    instance = OneDrive(activity)
                }
                return instance!!
            }
        }
    }

    init {
        auth = object : MSAAuthAndroidAdapter(activity.application) {
            override fun getClientId(): String {
                return CLIENT_ID
            }

            override fun getScopes(): Array<String> {
                return arrayOf(
                        "Files.ReadWrite.AppFolder"
                )
//                return arrayOf(
//                        "openid",
//                        "profile",
//                        "User.ReadWrite",
//                        "User.ReadBasic.All",
//                        "Sites.ReadWrite.All",
//                        "Contacts.ReadWrite",
//                        "People.Read",
//                        "Notes.ReadWrite.All",
//                        "Tasks.ReadWrite",
//                        "Mail.ReadWrite",
//                        "Files.ReadWrite.All",
//                        "Calendars.ReadWrite"
//                )
            }
        }
        mClintConfig = DefaultClientConfig.createWithAuthenticationProvider(auth)
        mClintConfig.logger.loggingLevel = LoggerLevel.Debug
        mClint = GraphServiceClient.Builder().fromConfig(mClintConfig).buildClient()
    }

    override fun isLogin(): StorageRequest<StorageBaseResponse> {
        return StorageRequest(object : CallBack<StorageBaseResponse> {
            override fun run(): StorageBaseResponse {
                return try {
                    auth.getAccessToken()
                    StorageBaseResponse(true)
                } catch (e: Exception) {
                    StorageBaseResponse(false)
                }
            }
        })
    }

    override fun login(activity: Activity): StorageRequest<LoginResponse> {
        return StorageRequest(object : CallBack<LoginResponse> {
            override fun run(): LoginResponse {
                try {
                    if (isLogin().send().result) {
                        return LoginResponse(!TextUtils.isEmpty(auth.getAccessToken()))
                    } else {
                        throw ClientException("", Throwable(""), GraphErrorCodes.Unauthenticated)
                    }
                } catch (e: Exception) {
                    val waiter = SimpleWaiter()
                    val returnValue = AtomicReference<Boolean>()
                    val exceptionValue = AtomicReference<ClientException>()
                    auth.login(activity, object : ICallback<HashMap<String, String>> {
                        override fun success(result: HashMap<String, String>) {
                            returnValue.set(true)
                            waiter.signal()
                        }

                        override fun failure(ex: ClientException?) {
                            exceptionValue.set(ex)
                            waiter.signal()
                        }
                    })
                    waiter.waitForSignal()
                    val loginResponse = LoginResponse(exceptionValue.get() == null)
                    if (exceptionValue.get() != null) {
                        loginResponse.result = false
                        loginResponse.error = exceptionValue.get().message!!
                    } else {
                        loginResponse.result = true
                    }
                    return loginResponse
                }
            }
        })
    }

    override fun logout(): StorageRequest<LogoutResponse> {
        return StorageRequest(object : CallBack<LogoutResponse> {
            override fun run(): LogoutResponse {
                if (!isLogin().send().result) {
                    return LogoutResponse(true)
                }

                val waiter = SimpleWaiter()
                val returnValue = AtomicReference<Boolean>()
                val exceptionValue = AtomicReference<ClientException>()

                auth.logout(object : ICallback<Void> {
                    override fun success(result: Void?) {
                        returnValue.set(true)
                        waiter.signal()
                    }

                    override fun failure(ex: ClientException?) {
                        exceptionValue.set(ex)
                        waiter.signal()
                    }
                })
                waiter.waitForSignal()
                val ex = exceptionValue.get()
                val result = LogoutResponse(ex == null)
                result.message = if (ex != null) ex.message!! else ""
                return result
            }
        })
    }

    /**
     * @param fileName 文件名，不包括路径
     */
    override fun exist(fileName: String): StorageRequest<ExistResponse> {

        return StorageRequest(object : CallBack<ExistResponse> {
            override fun run(): ExistResponse {
                if (!isLogin().send().result) {
                    return ExistResponse(false)
                }

                val waiter = SimpleWaiter()
                val returnValue = AtomicReference<Boolean>()
                val exceptionValue = AtomicReference<ClientException>()

                mClint.me.drive.getSpecial("approot")
                        .getItemWithPath(APP_FOLDER_PATH)
                        .getChildren(fileName)
                        .buildRequest()
                        .get(object : ICallback<DriveItem> {
                            override fun success(result: DriveItem?) {
                                Log.e("exist", result.toString())
                                returnValue.set(true)
                                waiter.signal()
                            }

                            override fun failure(ex: ClientException?) {
                                Log.e("exist", ex.toString())
                                returnValue.set(false)
                                exceptionValue.set(ex)
                                waiter.signal()
                            }

                        })
                waiter.waitForSignal()

                val result = ExistResponse(returnValue.get())
                result.message = if (result.isExist) "" else exceptionValue.get().message!!
                return result
            }
        })
    }

    /**
     * @param data 数据
     * @param fileName 文件名，不包括路径
     */
    override fun upload(data: String, fileName: String): StorageRequest<UploadResponse> {

        return StorageRequest(object : CallBack<UploadResponse> {
            override fun run(): UploadResponse {
                if (!isLogin().send().result) {
                    return UploadResponse(false)
                }

                val waiter = SimpleWaiter()
                val returnValue = AtomicReference<Boolean>()
                val exceptionValue = AtomicReference<ClientException>()

                mClint.me.drive.getSpecial("approot")
                        .getItemWithPath(APP_FOLDER_PATH)
                        .children
                        .byId(fileName)
                        .content
                        .buildRequest()
                        .put(data.toByteArray(), object : ICallback<DriveItem> {
                            override fun success(result: DriveItem?) {
                                Log.e("upload", result.toString())
                                returnValue.set(true)
                                waiter.signal()
                            }

                            override fun failure(ex: ClientException?) {
                                Log.e("upload", ex.toString())
                                returnValue.set(false)
                                exceptionValue.set(ex)
                                waiter.signal()
                            }
                        })
                waiter.waitForSignal()
                val result = UploadResponse(returnValue.get())
                result.message = if (result.result) "" else exceptionValue.get().message!!
                return result
            }
        })
    }

    override fun download(fileName: String): StorageRequest<DownloadResponse> {

        return StorageRequest(object : CallBack<DownloadResponse> {
            override fun run(): DownloadResponse {
                if (!isLogin().send().result) {
                    return DownloadResponse(false)
                }
                val inputStream = mClint.me.drive.getSpecial("approot")
                        .getItemWithPath(APP_FOLDER_PATH)
                        .getChildren(fileName)
                        .content
                        .buildRequest()
                        .get()
                val data = readInputStream(inputStream)
                inputStream?.close()
                val response = DownloadResponse(true)
                response.data = data
                return response
            }
        })
    }

    override fun lastOne(): StorageRequest<LastOneResponse> {
        return StorageRequest(object : CallBack<LastOneResponse> {
            override fun run(): LastOneResponse {
                if (!isLogin().send().result) {
                    return LastOneResponse(false)
                }

                val response = LastOneResponse(true)

                val waiter = SimpleWaiter()

                val urlCreate = mClint.me.drive.getSpecial("approot")
                        .getItemWithPath(APP_FOLDER_PATH)
                        .children
                DriveItemCollectionRequest(urlCreate.requestUrl + "?\$orderby=createdDateTime desc&\$top=1&\$select=id,name", mClint, arrayListOf())
                        .get(object : ICallback<IDriveItemCollectionPage> {
                            override fun success(result: IDriveItemCollectionPage?) {
                                if (result != null && result.currentPage.size != 0) {
                                    response.result = true
                                    response.id = result.currentPage[0].id
                                    response.name = result.currentPage[0].name
                                } else {
                                    response.result = false
                                    response.message = "none"
                                }
                                waiter.signal()
                            }

                            override fun failure(ex: ClientException?) {
                                response.result = false
                                response.message = ex!!.message!!
                                waiter.signal()
                            }

                        })
                waiter.waitForSignal()
                return response
            }
        })
    }

    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readInputStream(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(1024)
        var len: Int = 0
        val bos = ByteArrayOutputStream()
        while (true) {
            len = inputStream.read(buffer)
            if (len < 0) {
                break
            }
            bos.write(buffer, 0, len)
        }
        bos.close()
        return bos.toByteArray()
    }


}