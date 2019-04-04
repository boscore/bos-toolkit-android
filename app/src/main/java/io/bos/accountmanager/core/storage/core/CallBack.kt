package io.bos.accountmanager.core.storage.core

import io.bos.accountmanager.core.storage.core.response.StorageBaseResponse

interface CallBack<T: StorageBaseResponse> {
    fun run(): T
}