package com.notebook.android.ui.myAccount

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.notebook.android.BuildConfig
import com.notebook.android.R
import com.notebook.android.databinding.FragmentMerchantViewSummaryWebViewPageBinding


class MerchantViewSummaryWebViewPage : Fragment() {

    private lateinit var fragMerchantViewSummaryWebViewBinding:FragmentMerchantViewSummaryWebViewPageBinding
    private var userID:Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragMerchantViewSummaryWebViewBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_merchant_view_summary_web_view_page, container, false)

        if(arguments != null){
            val merchantSummaryArgs = MerchantViewSummaryWebViewPageArgs.fromBundle(requireArguments())
            userID = merchantSummaryArgs.userID
            initWebView()
            setWebViewListener()
        }
       return fragMerchantViewSummaryWebViewBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(){
        fragMerchantViewSummaryWebViewBinding.webviewMerchantDetailLoad.apply {
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = false
            Log.e("userId", " :: $userID")
            loadUrl("https://notebookstore.in/api/merchantanalytic/${userID}")
        }
    }

    private fun setWebViewListener(){
        fragMerchantViewSummaryWebViewBinding.webviewMerchantDetailLoad.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                fragMerchantViewSummaryWebViewBinding.pbMerchantDetailLoader.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                fragMerchantViewSummaryWebViewBinding.pbMerchantDetailLoader.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }
}