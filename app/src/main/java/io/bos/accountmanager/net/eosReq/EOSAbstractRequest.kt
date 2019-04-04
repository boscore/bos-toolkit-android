package io.bos.accountmanager.net.eosReq

/**
 * Created by haichecker on 18-6-19.
 */
abstract class EOSAbstractRequest {
//    override fun contentType(): MediaType? {
//        return MediaType.parse("application/json")
//    }
//
//    override fun writeTo(sink: BufferedSink?) {
//        sink?.write(Gson().toJson(getData()).toByteArray())
//    }
//
    abstract fun getData(): Any
}