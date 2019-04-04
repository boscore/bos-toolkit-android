package io.bos.accountmanager.core.run

import android.text.TextUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe


open class Run<T : Result> constructor(private val callBack: Callback<T>) {
    fun run(): T {
        return callBack.call()
    }

    fun rxJava(): Flowable<T> {
        return Flowable.create({
            val result = callBack.call()
            if (result.success) {
                it.onNext(result)
            } else {
                it.onError(Throwable(result.error))
            }
            it.onComplete()
        }, BackpressureStrategy.ERROR)
    }

}