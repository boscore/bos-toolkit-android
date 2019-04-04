package io.bos.accountmanager.core.storage.core.response

class LoginResponse(var isLogin: Boolean, var error: String = "") : StorageBaseResponse(isLogin, error)