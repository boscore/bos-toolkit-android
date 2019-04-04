package io.bos.accountmanager.core.storage.core.response

class LastOneResponse(result: Boolean, message: String = "") : StorageBaseResponse(result, message) {
    var id = ""
    var name = ""
}