package io.bos.accountmanager.core.storage.core.response

import java.nio.charset.Charset

class DownloadResponse(result: Boolean, message: String = "") : StorageBaseResponse(result, message) {
    var data: ByteArray = ByteArray(0)

    fun string(): String {
        return String(data, Charset.forName("UTF-8"))
    }
}