package io.bos.accountmanager.core.storage

import android.app.Activity
import io.bos.accountmanager.core.storage.protocol.Storage

object StorageFactory {
    fun createOneDrive(activity: Activity): Storage {
        return OneDrive.get(activity)
    }
}