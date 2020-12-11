package com.notebook.android.ui.dashboard.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.notebook.android.R
import com.notebook.android.databinding.FragmentOfferViewProdLinkBinding

class OfferViewProdLink : Fragment() {

    private lateinit var fragOfferWebBinding:FragmentOfferViewProdLinkBinding
    private var offerWebLink:String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragOfferWebBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_offer_view_prod_link, container, false)

        /*fragOfferWebBinding.srlOfferWebLink.
        setColorSchemeColors(
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
        fragOfferWebBinding.srlOfferWebLink.setOnRefreshListener(this)*/

        if(arguments != null){
            val offerArgs = OfferViewProdLinkArgs.fromBundle(requireArguments())
            offerWebLink = offerArgs.offerWebLink
            Log.e("offer link", " :: $offerWebLink")
        }
        return fragOfferWebBinding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragOfferWebBinding.wvOfferLink.apply {
            setInitialScale(1)
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = false

            webViewClient = object : WebViewClient(){

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
//                    fragOfferWebBinding.srlOfferWebLink.isRefreshing = true
                    fragOfferWebBinding.lavDataNotAvailable.visibility = View.GONE
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
//                    fragOfferWebBinding.srlOfferWebLink.isRefreshing = false
                    fragOfferWebBinding.lavDataNotAvailable.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
//                    fragOfferWebBinding.srlOfferWebLink.isRefreshing = false
                    fragOfferWebBinding.lavDataNotAvailable.visibility = View.VISIBLE
                }
            }

            if(offerWebLink.equals("#")){
//                fragOfferWebBinding.srlOfferWebLink.isRefreshing = false
                fragOfferWebBinding.lavDataNotAvailable.visibility = View.VISIBLE
            }else{
                offerWebLink?.let { loadUrl(it) }
            }
        }
    }
}
