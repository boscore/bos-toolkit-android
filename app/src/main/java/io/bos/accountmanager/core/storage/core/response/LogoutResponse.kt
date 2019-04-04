package io.bos.accountmanager.core.storage.core.response

class LogoutResponse(var isLogout: Boolean, var error: String = "") : StorageBaseResponse(isLogout, error)