package io.bos.accountmanager.data.local

import io.bos.accountmanager.data.local.db.DataBase
import io.bos.accountmanager.data.local.db.dao.AccountDao
import io.bos.accountmanager.data.local.db.dao.EstablishAccountDao
import io.starteos.jeos.net.StartEOS

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor(var database: DataBase,var accountDao: AccountDao,val start : StartEOS,var establishAccountDao: EstablishAccountDao) {

}