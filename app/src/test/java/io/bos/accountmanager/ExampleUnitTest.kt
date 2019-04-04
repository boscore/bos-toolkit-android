package io.bos.accountmanager

import com.google.gson.Gson
import io.starteos.jeos.Name
import io.starteos.jeos.Symbol
import io.starteos.jeos.crypto.digest.Sha256
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.HexUtils
import io.starteos.jeos.net.StartFactory
import io.starteos.jeos.net.core.HttpService
import io.starteos.jeos.raw.Writer
import io.starteos.jeos.raw.core.Action
import io.starteos.jeos.raw.core.Signature
import io.starteos.jeos.raw.core.TypePublicKey
import io.starteos.jeos.transaction.PackedTransaction
import io.starteos.jeos.transaction.SignedTransaction
import io.starteos.jeos.transaction.type.ChainTypeId
import org.junit.Test

import org.junit.Assert.*
import java.nio.charset.Charset
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        System.out.println(Name("close").name_long())

        val write = Writer(255)
        val data = arrayListOf<String>("1", "2", "3")

        write.putUint(data.size.toLong())
        for (datum in data) {
            write.putString(datum)
        }
        System.out.println(HexUtils.toHex(write.toBytes()))
        assertEquals(4, 2 + 2)
    }



    //创建账号转账
    @Test
    fun CreateTransfers(){
        var name="dengzhebin33"
        val key = EosPrivateKey()
        var   privateKey = key.toString()
        var   publicKey = key.publicKey.toString(Constants.Const.WALLETTYPE)
        System.out.println("公钥==="+publicKey)
        System.out.println("私钥==="+privateKey)


        //地址
        val start = StartFactory.build(HttpService(Constants.Const.URL))
        //首先获取info
        val info =  start.info().send()
        //准备开始签名
        val txn = SignedTransaction()
        val id = System.currentTimeMillis() + Random().nextInt(10000)

        //需要签名的私钥
        val sign =EosPrivateKey("5KNr8svTKXuzBf8qg1pWs7uDHzsGXf9R6fXY31EB5Mt1RofZmMh")
        //通过eosio.token的transfer接口进行 拼接memo
        val memo ="act^$name^$publicKey^$publicKey"
        //转账的账号
        val from = Name("v5v5v5v5v5v5")
        //转给哪个合约 现在是测试合约
        val to = Name(Constants.Const.BOS_CONTRACT)
        //金额
        val quantity = Symbol("5.0000 BOS")


        //根据函数顺序进行put
        val wdata = Writer(255)
        from.pack(wdata)
        to.pack(wdata)
        quantity.pack(wdata)
        wdata.putString(memo)

        //放入id
        txn.setReferenceBlock(info.head_block_id)
        //设置超时时间
        txn.expiration = info.getTimeAfterHeadBlockTime(30000)
        //封装action      合约名字， 函数名，账号@权限名字       wdata put的数据bytese格式
        val action = Action.toAction("eosio.token","transfer","v5v5v5v5v5v5@active",wdata.toBytes())
        //添加action  可以多个添加
        txn.addAction(action)

        //整个的加密
        txn.sign(sign, ChainTypeId(info.chain_id))
        //发送请求
        val result = start.pushTransaction(PackedTransaction(txn)).send()

        //判断是否成功
        if(result.isError){
            System.out.println(result.error.what)
        }else{
            System.out.println("成功")
        }


    }






   //创建账号
    @Test
    fun createAccountRed(){
       val newOwner  = EosPrivateKey()
       val newActive = EosPrivateKey()
       System.out.println("newOwner公钥==="+newOwner.publicKey.toString())
       System.out.println("newOwner私钥==="+newOwner.toString())
       System.out.println("newActive公钥==="+newActive.publicKey.toString())
       System.out.println("newActive私钥==="+newActive.toString())

         //红包的私钥
        val privateKeyActive = "5JhCANpbTUAeWMUBBDMQPKNKkxrHuUWn9aWz2p3snWuyWkrUAuu"
        val privateKey = EosPrivateKey(privateKeyActive)
       //根据函数方法开始封装   要创建的账号
        val account = Name("v5v5v5v5v5v1")

       val owner_key = TypePublicKey(newOwner.publicKey.toString(Constants.Const.WALLETTYPE))
       val active_key = TypePublicKey(newActive.publicKey.toString(Constants.Const.WALLETTYPE))
       //红包id
        val id = 1546516530432
        //签名的sig  客户端使用私钥对红包id进行ecdsa签名生成，每个签名只能使用一次
        val s = Sha256.from(id.toString().toByteArray(Charset.forName("UTF-8")))
        val signId = privateKey.sign(s).toString()
        val sig = Signature(signId)

        //地址
        val start = StartFactory.build(HttpService(Constants.Const.URL))
        //首先获取info
        val info =  start.info().send()
        //开始put
        val writer = Writer(255)
        account.pack(writer)
        owner_key.pack(writer)
        active_key.pack(writer)
        writer.putLong(id)
        sig.pack(writer)


        val txn = SignedTransaction()
        txn.expiration = info.getTimeAfterHeadBlockTime(30000)
        txn.setReferenceBlock(info.head_block_id)
        //必须用有redpacket权限的账号
        val action = Action.toAction(Constants.Const.BOS_CONTRACT,"create","bosfreetouse@redpacket",writer.toBytes())

        txn.addAction(action)
       //必须用有redpacket权限
        txn.sign(EosPrivateKey("5Jg3KtArcxdsk2opXpyBNqKeZ7ah9SFLPg2Xx8vHFGCnfRGffkD"), ChainTypeId(info.chain_id))

        val result = start.pushTransaction(PackedTransaction(txn)).send()
//        System.out.println(Gson().toJson(result))
       //判断是否成功
       if(result.isError){
           System.out.println(result.error.what)
       }else{
           System.out.println("成功")
       }


    }

    /**
     * 所需参数：
     * 1.创建者公钥
     * 2.创建者账号名
     * 3.红包金额
     * 4.创建者权限
     */
    @Test
    fun createred(){

        val key = EosPrivateKey()
     var   privateKey = key.toString()
     var   publicKey = key.publicKey.toString(Constants.Const.WALLETTYPE)
        System.out.println("公钥==="+publicKey)
        System.out.println("私钥==="+privateKey)


        //提交者的的当前账号公私钥
        val privateKeyOwner = "5K2F8hErqaJPh8aBRQPrQRTzZgrgVnvzBZJrBSBNxjZgbxFn5Hn"
        val privateKeyActive = "5KNr8svTKXuzBf8qg1pWs7uDHzsGXf9R6fXY31EB5Mt1RofZmMh"
        //地址
        val start = StartFactory.build(HttpService(Constants.Const.URL))
        //首先获取info
        val info =  start.info().send()
        //准备开始签名
        val txn = SignedTransaction()
        val id = System.currentTimeMillis() + Random().nextInt(10000)
        System.out.println("id==="+id)
        //需要签名的公私钥
        val sign = EosPrivateKey(privateKeyOwner)
        //通过eosio.token的transfer接口进行 拼接memo
        val memo = "hb^3^$id^1^${publicKey}^v5v5v5v5v5v5^Test"
        //转账的账号
        val from = Name("v5v5v5v5v5v5")
        //转给哪个合约 现在是测试合约
        val to = Name(Constants.Const.BOS_CONTRACT)
        //金额
        val quantity = Symbol("5.0000 BOS")
        //根据函数顺序进行put
        val wdata = Writer(255)
        from.pack(wdata)
        to.pack(wdata)
        quantity.pack(wdata)
        wdata.putString(memo)
        //放入id
        txn.setReferenceBlock(info.head_block_id)
        //设置超时时间
        txn.expiration = info.getTimeAfterHeadBlockTime(30000)
        //封装action      合约名字， 函数名，账号@权限名字       wdata put的数据bytese格式
        val action = Action.toAction("eosio.token","transfer","v5v5v5v5v5v5@owner",wdata.toBytes())
        //添加action  可以多个添加
        txn.addAction(action)
        //整个的加密
        txn.sign(sign, ChainTypeId(info.chain_id))
        //发送请求
        val result = start.pushTransaction(PackedTransaction(txn)).send()
        //判断是否成功
        if(result.isError){
            System.out.println(result.error.what)
         }else{
            System.out.println("成功")
        }

//        System.out.println(Gson().toJson(result))



    }


    @Test
    fun get( ) {
                 val privateKeyActive = "5Jc1svFUYgmYuLVWeQ1cyddwjyFaXzhzRRKMMLuioM3DjJdXTp1"
                val privateKey = EosPrivateKey(privateKeyActive)
                //领取的账号
                val account = Name("v5v5v5v5v5v4")
                //签名的sig  客户端使用私钥对红包id进行ecdsa签名生成，每个签名只能使用一次
                val s = Sha256.from("1546868016429".toString().toByteArray(Charset.forName("UTF-8")))
                val signId = privateKey.sign(s).toString()
                val sig = Signature(signId)

                //地址
                val start = StartFactory.build(HttpService(Constants.Const.URL))
                //首先获取info
                val info = start.info().send()
                //开始put
                val writer = Writer(255)
                account.pack(writer)
                writer.putLong(1546868016429)
                sig.pack(writer)


                val txn = SignedTransaction()
                txn.expiration = info.getTimeAfterHeadBlockTime(30000)
                txn.setReferenceBlock(info.head_block_id)
                //必须用有redpacket权限的账号
                val action = Action.toAction(Constants.Const.BOS_CONTRACT, "get", "bosfreetouse@redpacket", writer.toBytes())

                txn.addAction(action)
                //必须用有redpacket权限
                txn.sign(EosPrivateKey("5Jg3KtArcxdsk2opXpyBNqKeZ7ah9SFLPg2Xx8vHFGCnfRGffkD"), ChainTypeId(info.chain_id))

                val result = start.pushTransaction(PackedTransaction(txn)).send()
                if (result.isError) {
                     System.out.println("失败")
                } else {
                    System.out.println("成功")
                }
            }




}
