package io.bos.accountmanager.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import io.bos.accountmanager.di.ApplicationContext
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesHelper @Inject constructor(@ApplicationContext val context: Context) {
    companion object {
        const val ENCRYPT_KEY_LEN = 32
        fun getIv(hash: Sha512): ByteArray {
            return Arrays.copyOfRange(hash.getBytes(), ENCRYPT_KEY_LEN, ENCRYPT_KEY_LEN + 16)
        }

        fun getUUID(context: Context): String {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var tmSerial: String
            var tmDevice: String
            var tmPhone: String
            var androidId: String
            val readPhoneState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
            if (readPhoneState == PackageManager.PERMISSION_GRANTED) {
                tmDevice = "" + tm.deviceId;
                tmSerial = "" + tm.simSerialNumber;
                androidId = "" + android.provider.Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                val deviceUuid = UUID(androidId.hashCode().toLong(), tmDevice.hashCode() as Long shl 32 or tmSerial.hashCode().toLong())
                return deviceUuid.toString()
            }
            throw Exception("Permission Error")
        }
    }

    val PWD_CIPHER_TEXT: String = Base58.encode("PWD_CIPHER_TEXT".toByteArray())
    val PWD_QUESTION: String = Base58.encode("PWD_QUESTION".toByteArray())
    val PWD_CIPHER_TEXT_VERIFY: String = Base58.encode("PWD_CIPHER_TEXT_VERIFY".toByteArray())
    val PWD_TIPS: String = Base58.encode("PWD_TIPS".toByteArray())
    val PRE_FERENCES: String = Base58.encode("info".toByteArray())
    val DATA_PASS: ByteArray = "Starteos.io".toByteArray()
    val pwd = Sha512.from(DATA_PASS)
    private val mPrefs: SharedPreferences = context.getSharedPreferences(PRE_FERENCES, Context.MODE_PRIVATE)

    //    private lateinit var mPrefs: SharedPreferences
    init {
//        UUID.randomUUID().timestamp()
    }

    fun isSettingPwd(): Boolean {
        return !(TextUtils.isEmpty(getPwdCipher()) && TextUtils.isEmpty(getPwdVerify()))
    }

    fun putPwdTip(tip: String): Boolean {
        return mPrefs.edit().putString(PWD_TIPS, encrypt(tip)).commit()
    }

    fun getPwdTip(): String {
        val data = mPrefs.getString(PWD_TIPS, "")
        if (TextUtils.isEmpty(data)) {
            return ""
        }
        return decrypt(data!!)
    }

    fun putPwdCipher(cipher: String): Boolean {
        return mPrefs.edit().putString(PWD_CIPHER_TEXT, encrypt(cipher)).commit()
    }


    fun getPwdCipher(): String {
        val cipher = mPrefs.getString(PWD_CIPHER_TEXT, "")
        if (TextUtils.isEmpty(cipher)) {
            return ""
        }
        return decrypt(cipher!!)
    }

    fun putQuestion(question: String): Boolean {
        return mPrefs.edit().putString(PWD_QUESTION, encrypt(question)).commit()
    }

    fun getQuestion(): String {
        val data = mPrefs.getString(PWD_QUESTION, "")
        if (TextUtils.isEmpty(data)) {
            return ""
        }
        return decrypt(data!!)
    }

    fun putPwdVerify(cipher: String): Boolean {
        return mPrefs.edit().putString(PWD_CIPHER_TEXT_VERIFY, encrypt(cipher)).commit()
    }

    fun getPwdVerify(): String {
        val cipher = mPrefs.getString(PWD_CIPHER_TEXT_VERIFY, "")
        if (TextUtils.isEmpty(cipher)) {
            return ""
        }
        return decrypt(cipher!!)
    }

    private fun encrypt(data: String): String {
        if (TextUtils.isEmpty(data)) {
            return data
        }
        val encrypt = CryptUtil.aesEncrypt(Arrays.copyOf(pwd.bytes, ENCRYPT_KEY_LEN), data.toByteArray(), getIv(pwd))
        return Base58.encode(encrypt)
    }

    private fun decrypt(encryptData: String): String {
        if (TextUtils.isEmpty(encryptData)) {
            return encryptData
        }
        val encrypt = CryptUtil.aesDecrypt(Arrays.copyOf(pwd.bytes, ENCRYPT_KEY_LEN), Base58.decode(encryptData), getIv(pwd))
        return String(encrypt)
    }


}