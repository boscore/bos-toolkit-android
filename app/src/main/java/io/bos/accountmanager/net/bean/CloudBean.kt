package io.bos.accountmanager.net.bean

class CloudBean(){
       var accountName:String?=""
       var cipherText: String=""
//    private  var privateKey:String=""//私钥
//    private  var publicKey:String?=""//公钥
       var  money:String="0.0000 BOS"
       var publicKey: String = ""
       var publicName:String=""//公钥名称
       var backup:Boolean=false//是否备份
       var isActive:Boolean=false//是否是active权限
       var isSelect:Boolean=false;//是否选择
       var  selectPrivat:String="";//选择的active权限
       var  listKey = ArrayList<SecretKeyBean>()//所有的active权限
}