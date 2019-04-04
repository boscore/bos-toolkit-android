package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable

interface MainView : AbstractView {
    fun onAccounts(accounts: List<AccountTable>) {}

    fun onBalance(){}

    fun getAccountIs(accounts: List<AccountTable>){}

    fun errAccount(){}
}