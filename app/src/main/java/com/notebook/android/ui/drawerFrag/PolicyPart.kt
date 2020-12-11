package com.notebook.android.ui.drawerFrag

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.notebook.android.R
import com.notebook.android.databinding.FragmentPolicyPartBinding
import com.notebook.android.utility.ConnectivityUtil


class PolicyPart : Fragment() {

    private lateinit var fragmentPolicyPartBinding: FragmentPolicyPartBinding

    private var policyUrl:String ?= null
    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentPolicyPartBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_policy_part, container, false
        )
        policyUrl = requireArguments().getString("policyLink")
        startWebView()

        fragmentPolicyPartBinding.wvPolicyPart.apply {
            clearCache(true)
            clearHistory()
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = false
            settings.pluginState = WebSettings.PluginState.ON
            settings.defaultTextEncodingName = "utf-8"
        }

        if(ConnectivityUtil.isNetworkAvailable(mContext)){
            policyUrl?.let { fragmentPolicyPartBinding.wvPolicyPart.loadUrl(it) }
            fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.GONE
            fragmentPolicyPartBinding.wvPolicyPart.visibility = View.VISIBLE
        }else{

            fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.VISIBLE
            fragmentPolicyPartBinding.wvPolicyPart.visibility = View.GONE
        }
        return fragmentPolicyPartBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentPolicyPartBinding.btnRetryInternet.setOnClickListener {
            if(ConnectivityUtil.isNetworkAvailable(mContext)){
                policyUrl?.let { it1 -> fragmentPolicyPartBinding.wvPolicyPart.loadUrl(it1) }
                fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.GONE
                fragmentPolicyPartBinding.wvPolicyPart.visibility = View.VISIBLE
            }else{
                fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragmentPolicyPartBinding.wvPolicyPart.visibility = View.GONE
            }
        }
    }

    private fun startWebView() {

        fragmentPolicyPartBinding.wvPolicyPart.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                fragmentPolicyPartBinding.pbPolicyPartLoading.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    if(ConnectivityUtil.isNetworkAvailable(mContext)){
                        startWebView()
                        fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.GONE
                        fragmentPolicyPartBinding.wvPolicyPart.visibility = View.VISIBLE
                    }else{
                        fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.VISIBLE
                        fragmentPolicyPartBinding.wvPolicyPart.visibility = View.GONE
                    }
                    return false
                }else{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                fragmentPolicyPartBinding.pbPolicyPartLoading.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                fragmentPolicyPartBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragmentPolicyPartBinding.wvPolicyPart.visibility = View.GONE
            }
        }
    }
}
