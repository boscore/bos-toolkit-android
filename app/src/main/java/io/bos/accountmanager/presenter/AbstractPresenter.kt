package io.bos.accountmanager.presenter

import io.bos.accountmanager.view.AbstractView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class AbstractPresenter<V : AbstractView> : AbstractPresenterImpl<V> {
    private var viewAttached: Boolean = false
    private var absView: V? = null
    protected val androidSchedulers = AndroidSchedulers.mainThread()
    protected val ioSchedulers = Schedulers.io()
    protected val newSchedulers = Schedulers.newThread()
    private var mCompositeDisposable: CompositeDisposable? = null

    override fun attachView(view: V) {
        absView = view
        viewAttached = true
    }

    override fun detachView() {
        absView = null
        viewAttached = false
        mCompositeDisposable?.clear()
    }

    override fun getView(): V? {
        return absView
    }

    override fun isViewAttached(): Boolean {
        return viewAttached && absView != null
    }

    protected fun addDisposable(d: Disposable) {
        if (null == mCompositeDisposable) {
            mCompositeDisposable = CompositeDisposable()
        }

        if (!mCompositeDisposable!!.isDisposed) {
            mCompositeDisposable?.add(d)
        }
    }
}