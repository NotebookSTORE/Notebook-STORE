package com.notebook.android.ui.merchant.frag

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.notebook.android.BuildConfig

import com.notebook.android.R
import com.notebook.android.databinding.FragmentNotebookPdfPageBinding

class NotebookPdfPage : Fragment() {

    private lateinit var notebookPdfBinding:FragmentNotebookPdfPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notebookPdfBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_notebook_pdf_page, container, false)

        initWebView()
        setWebViewListener()
        return notebookPdfBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(){
        notebookPdfBinding.webviewPdfLoad.apply {
            settings.javaScriptEnabled = true
            settings.pluginState = WebSettings.PluginState.ON
            settings.builtInZoomControls = false
            loadUrl("http://demo.mbrcables.com/notebookstore/merchantdetail/become-a--prime-merchant/1")
        }
    }

    private fun setWebViewListener(){
        notebookPdfBinding.webviewPdfLoad.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                notebookPdfBinding.pbPdfLoader.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                notebookPdfBinding.pbPdfLoader.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
    }
}
