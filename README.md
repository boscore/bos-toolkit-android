# BOS Toolkit

## 更可用的链 为DApp而生

* 支持红包创建账户，快捷红包发送
* 支持keystore多账户云端备份，告别繁复备份
* 支持多账户导入导出,BOS生态钱包可一键导入账户

## 第三方调用说明

通过URL Scheme调起本工具箱获取账户active private key：

| key      | value             |
| -------- | ----------------- |
| protocol | bos               |
| host     | bos.get.account   |
| path     | externalImport    |
| param    | action=getAccount |

### 示例

#### 调起

```kotlin
 val intent = Intent()
 intent.data = Uri.parse("bos://bos.get.account/externalImport?action=getAccount")
 startActivityForResult(intent,  200)
 ```

#### 接收结果

 ```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==200 &&data!=null&&resultCode== Activity.RESULT_OK){
            //返回data数据，通过Base58 Decode获取到最终数据
            var base58Param=data?.getStringExtra("data");
            var base58Data=String(Base58.decode(base58Param))
        }
    }
```

**最终结果**

```json
{
    "action":"getAccount",
    "data":[
        {
            "account_name":"dengzhebin33",
            "keys":[
                "5HtH2h1F9yWtw8z7END56S9ma4UVTCp16bgaqVZYAHxwLiJxtJA"
            ],
            "type":"BOS"
        }
    ]
}
```
# bos-toolkit-android
