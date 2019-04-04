package io.bos.accountmanager.net.bean

/**
 * Created by Administrator on 2019/1/7/007.
 */

class RedEnvelopeRecordBean {

    /**
     * id : 1546422537731
     * type : 3
     * count : 2
     * amount : 5.0000 BOS
     * sender : v5v5v5v5v5v5
     * pubkey : EOS6hc2V2Ncb6LvUxyAgfNmnnj9EcRBuRnHB5eiJdE6XPMCB5Sdif
     * greetings : Test
     * expire : 1546508940
     * claims : []
     */

    var id: String? = null
    var type: Int = 0
    var count: Int = 0
    var amount: String? = null
    var sender: String? = null
    var pubkey: String? = null
    var greetings: String? = null
    var expire: Int = 0
    var claims: List<*>? = null
}
