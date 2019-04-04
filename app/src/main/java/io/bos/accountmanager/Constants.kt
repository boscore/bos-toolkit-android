package io.bos.accountmanager

class Constants {
    object Const {
        val TX_EXPIRATION_IN_MILSEC = 30000
        val LANGUAGE = "language"
        val URL="http://api-bos.starteos.io"
        //http://api-bos.starteos.io   http://47.254.82.241:80

        val BOS_CONTRACT="redpacket"
        val WALLETTYPE="EOS"
    }

    object RoutePath {
        const val MAIN_ACTIVITY: String = "/main/activity"

        object FRAGMENT {

            const val PERSONAL_CENTER_FRAGMENT: String = "/main/fragment/personal/center"
            const val HOME_WALLET_FRAGMENT: String = "/main/fragment/home/wallet"
            const val RED_RECEIVE_RECORD_FRAGMENT: String = "/main/fragment/red/receive/record"
            const val RED_SEND_OUT_RECORD_FRAGMENT: String = "/main/fragment/red/send/out/record"
            const val RED_ENVELOPE_ACCOUNT_FRAGMENT: String = "/main/fragment/red/envelope/account"
            const val RED_ENVELOPE_ORDINARY_FRAGMENT: String = "/main/fragment/red/envelope/ordinary"
            const val RED_ENVELOPE_RANDOM_FRAGMENT: String = "/main/fragment/red/envelope/random"


            const val IMPORT_KEYSTORE_FRAGMENT: String = "/main/fragment/red/import/keystore"
            const val IMPORT_PRIVATE_KEY_FRAGMENT: String = "/main/fragment/import/private/key"
            const val IMPORT_CLOUD_FRAGMENT: String = "/main/fragment/import/cloud"
        }

        object ACTIVITY {

            const val HOME_ACTIVITY: String = "/main/activity/home"
            const val LANGUAGE_ACTIVITY: String = "/main/activity/language"

            const val QUOTA_DEPLOY_ACTIVITY: String = "/main/activity/quota/deploy"
            const val WALLET_MANAGE_ACTIVITY: String = "/main/activity/wallet/manage"
            const val LANGUAGE_CHANGE_ACTIVITY: String = "/main/activity/language/change"
            const val PWD_UPDATE_ACTIVITY: String = "/main/activity/pwd/update"
            const val SECRET_SECURITY_ACTIVITY: String = "/main/activity/secret/security"
            const val FOUND_ACCOUNT_ACTIVITY: String = "/main/activity/found/account"
            const val NEW_READ_ENVELOPERS_ACTIVITY: String = "/main/activity/found/new_read"
            const val IMPORT_ACCOUNT_ACTIVITY: String = "/main/activity/found/import_account"
            const val CLOUD_MANAGEMENT_ACTIVITY: String = "/main/activity/cloud/management"
            const val CLOUD_IMPORT_LIST_ACTIVITY: String = "/main/activity/cloud/import"
            const val SENIOR_SETUP_ACTIVITY: String = "/main/activity/senior/setup"
            const val AUTHORITY_SETTING_ACTIVITY: String = "/main/activity/authority/setting"
            const val UPDATE_POWER_ACTIVITY: String = "/main/activity/update/power"
            const val EXPORT_KEYT_TIPS_ACTIVITY: String = "/main/activity/export/keyt/tips"
            const val EXPORT_PRIVATE_KEY_ACTIVITY: String = "/main/activity/export/private/key"
            const val BOS_RED_ENVELOPE_ACTIVITY: String = "/main/activity/eos/red/envelope"
            const val RED_ENVELOPE_RECORD_ACTIVITY: String = "/main/activity/red/envelope/record"
            const val RED_RECEIVE_SUCCESS_ACTIVITY: String = "/main/activity/red/receive/success"
            const val RED_ADD_SUCCESS_ACTIVITY: String = "/main/activity/red/add/success"
            const val RED_RECORD_ACTIVITY: String = "/main/activity/red/record"
            const val ACCOUNT_LIST_ACTIVITY: String = "/main/activity/account/list"

            const val RECEIVE_RED_ENVELOPE_ACTIVITY: String = "/main/activity/receive/red/envelope"
            const val CREATE_BOS_ACCOUNT_ACTIVITY: String = "/main/activity/create/bos/account"
            const val EXTERNAL_IMPORT_ACTIVITY: String = "/main/activity/external/import"
            const val WEB_VIEW_ACTIVITY: String = "/main/activity/web/view"
            const val PWD_CONFIRM_UPDATE_ACTIVITY: String = "/main/activity/pwd/confirm/update"



        }


    }


}