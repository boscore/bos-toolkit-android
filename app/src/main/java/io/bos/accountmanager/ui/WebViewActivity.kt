package io.bos.accountmanager.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.activity_web_view.*

/**
 * 网页跳转
 */

@Route(path = Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY)
class WebViewActivity : AbstractBosActivity() {

    @Autowired
    @JvmField
    var url: String = ""
    @Autowired
    @JvmField
    var title: String = ""

    override fun byId(): Int {
        return R.layout.activity_web_view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        txt_title.text = title
        if (TextUtils.isEmpty(url)) {
            url = "https://www.baidu.com"
        }


        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowFileAccess = true
        webSettings.builtInZoomControls = true
        webSettings.domStorageEnabled = true
        webSettings.setSupportZoom(true)
        webSettings.useWideViewPort = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.loadUrl(url)


        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.contains("platformapi/startApp")!!) {
                    try {
                        var uri = Uri.parse(url);
                        var intent = Intent.parseUri(url,
                                Intent.URI_INTENT_SCHEME);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        // intent.setSelector(null);
                        startActivity(intent);
                    } catch (e: Exception) {
                    }
                } else {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

            }
        }

        webView.setInitialScale(25)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.INVISIBLE
                } else {
                    if (View.INVISIBLE === progressBar.visibility) {
                        progressBar.visibility = View.VISIBLE
                    }
                    progressBar.progress = newProgress
                }
                super.onProgressChanged(view, newProgress)
            }
        }

        back.setOnClickListener({
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }

        })


    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//监听返回键，如果可以后退就后退
            if (webView.canGoBack()) {
                if (webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)

    }

}
