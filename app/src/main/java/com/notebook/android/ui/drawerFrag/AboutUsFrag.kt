package com.notebook.android.ui.drawerFrag

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.R
import com.notebook.android.databinding.FragmentAboutUsBinding
import com.notebook.android.ui.drawerFrag.listener.ContactUsListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.ConnectivityUtil
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class AboutUsFrag : Fragment(){

    private lateinit var fragAboutUsFragBinding:FragmentAboutUsBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }
    private var aboutUsUrl:String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragAboutUsFragBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_about_us, container, false)
        aboutUsUrl = requireArguments().getString("aboutUsLink")
        Log.e("aboutUrl", " :: $aboutUsUrl")
        startWebView()

        fragAboutUsFragBinding.wvAboutUs.apply {
            clearCache(true)
            clearHistory()
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = false
            settings.defaultTextEncodingName = "utf-8"
        }

        if(ConnectivityUtil.isNetworkAvailable(mContext)){
            aboutUsUrl?.let { fragAboutUsFragBinding.wvAboutUs.loadUrl(it) }
            fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.GONE
            fragAboutUsFragBinding.wvAboutUs.visibility = View.VISIBLE
        }else{

            fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.VISIBLE
            fragAboutUsFragBinding.wvAboutUs.visibility = View.GONE
        }
        return fragAboutUsFragBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragAboutUsFragBinding.btnRetryInternet.setOnClickListener {
            if(ConnectivityUtil.isNetworkAvailable(mContext)){
                aboutUsUrl?.let { it1 -> fragAboutUsFragBinding.wvAboutUs.loadUrl(it1) }
                fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.GONE
                fragAboutUsFragBinding.wvAboutUs.visibility = View.VISIBLE
            }else{
                fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragAboutUsFragBinding.wvAboutUs.visibility = View.GONE
            }
        }
    }

    private fun startWebView() {

        fragAboutUsFragBinding.wvAboutUs.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                fragAboutUsFragBinding.pbAboutUsLoading.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    if(ConnectivityUtil.isNetworkAvailable(mContext)){
                        startWebView()
                        fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.GONE
                        fragAboutUsFragBinding.wvAboutUs.visibility = View.VISIBLE
                    }else{
                        fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.VISIBLE
                        fragAboutUsFragBinding.wvAboutUs.visibility = View.GONE
                    }
                    return false
                }else{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                fragAboutUsFragBinding.pbAboutUsLoading.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                fragAboutUsFragBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragAboutUsFragBinding.wvAboutUs.visibility = View.GONE
            }
        }
    }
}
