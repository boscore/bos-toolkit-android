package io.bos.accountmanager.core.storage.core

import android.text.TextUtils
import io.bos.accountmanager.core.storage.core.response.StorageBaseResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class StorageRequest<T : StorageBaseResponse> constructor(private val callBack: CallBack<T>) {
    fun send(): T {
        return callBack.run()
    }

    fun rxJava(): Flowable<T> {
        return Flowable.create({
            val result = callBack.run()
            if (TextUtils.isEmpty(result.message)) {
                it.onNext(result)
            } else {
                it.onError(Throwable(result.message))
            }
            it.onComplete()
        }, BackpressureStrategy.ERROR)
    }
}