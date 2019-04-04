package io.bos.accountmanager.core.storage.protocol

import android.app.Activity
import io.bos.accountmanager.core.storage.core.StorageRequest
import io.bos.accountmanager.core.storage.core.response.*

interface Storage {
    enum class StorageType {
        ONE_DRIVE
    }

    fun login(activity: Activity): StorageRequest<LoginResponse>

    fun isLogin(): StorageRequest<StorageBaseResponse>

    fun logout(): StorageRequest<LogoutResponse>

    fun exist(fileName: String): StorageRequest<ExistResponse>

    fun upload(data: String, fileName: String): StorageRequest<UploadResponse>

    fun download(fileName: String): StorageRequest<DownloadResponse>

    fun lastOne(): StorageRequest<LastOneResponse>

}