package com.notebook.android.ui.myOrder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.notebook.android.R
import com.notebook.android.databinding.OrderTrackWebViewBinding

class OrderTrackWebView : Fragment() {

    private lateinit var activityOrderTrackWebViewBinding: OrderTrackWebViewBinding

    companion object{
        var orderUrl:String?=null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityOrderTrackWebViewBinding = DataBindingUtil.inflate(inflater,
            R.layout.order_track_web_view, container, false)

        initWebView()
        setWebViewListener()
        return activityOrderTrackWebViewBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(){
        activityOrderTrackWebViewBinding.webViewTracking.apply {
            settings.javaScriptEnabled = true
            settings.pluginState = WebSettings.PluginState.ON
            settings.builtInZoomControls = false
            if (!orderUrl.isNullOrBlank()) {
                loadUrl(orderUrl!!)
            }else{
                Toast.makeText(requireContext(), "Cannot load the tracking website.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setWebViewListener(){
        activityOrderTrackWebViewBinding.webViewTracking.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                activityOrderTrackWebViewBinding.loader.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                activityOrderTrackWebViewBinding.loader.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
    }
}
