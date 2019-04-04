package io.bos.accountmanager.core.run

interface Callback<T : Result> {
    fun call(): T
}