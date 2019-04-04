package io.bos.accountmanager.core.storage.core.response

class ExistResponse(var isExist: Boolean, var error: String = "") : StorageBaseResponse(isExist, error)