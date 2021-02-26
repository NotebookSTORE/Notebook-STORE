package com.notebook.android.ui.drawerFrag

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.notebook.android.BuildConfig

import com.notebook.android.R
import com.notebook.android.adapter.drawer.FaqDataAdapter
import com.notebook.android.databinding.FragmentFaqsBinding
import com.notebook.android.ui.drawerFrag.listener.FaqDataListener
import com.notebook.android.utility.ConnectivityUtil
import com.notebook.android.utility.ConnectivityUtil.isNetworkAvailable
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class FaqsFrag : Fragment()/*, KodeinAware, FaqDataListener*/ {

    private lateinit var fragmentFaqsBinding: FragmentFaqsBinding
   /* override val kodein by kodein()
    private val viewModelFactory : DrawerPartVMFactory by instance()
    private lateinit var navController: NavController
    private val drawerPartVM:DrawerPartVM by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(DrawerPartVM::class.java)
    }*/

    private lateinit var mContext:Context
    private lateinit var mActivity:FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
//        drawerPartVM.getFaqData()
        faqUrl = requireArguments().getString("faqLink")
        Log.e("faqUrl", " :: $faqUrl")
    }

    private var faqUrl:String ?= null  //"http://notebookstore.in/faq"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentFaqsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_faqs, container, false)

//        setupRecyclerView()
//        drawerPartVM.faqDataListener = this
        startWebView()

        fragmentFaqsBinding.wvFaqs.apply {
            clearCache(true)
            clearHistory()
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = false
            settings.defaultTextEncodingName = "utf-8"
        }

        if(isNetworkAvailable(mContext)){
            faqUrl?.let { fragmentFaqsBinding.wvFaqs.loadUrl(it) }
            fragmentFaqsBinding.clInternetNotAvailable.visibility = View.GONE
            fragmentFaqsBinding.wvFaqs.visibility = View.VISIBLE
        }else{

            fragmentFaqsBinding.clInternetNotAvailable.visibility = View.VISIBLE
            fragmentFaqsBinding.wvFaqs.visibility = View.GONE
        }
        return fragmentFaqsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentFaqsBinding.btnRetryInternet.setOnClickListener {
            if(isNetworkAvailable(mContext)){
                faqUrl?.let { it1 -> fragmentFaqsBinding.wvFaqs.loadUrl(it1) }
                fragmentFaqsBinding.clInternetNotAvailable.visibility = View.GONE
                fragmentFaqsBinding.wvFaqs.visibility = View.VISIBLE
            }else{
                fragmentFaqsBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragmentFaqsBinding.wvFaqs.visibility = View.GONE
            }
        }
    }

    private fun startWebView() {

        fragmentFaqsBinding.wvFaqs.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                fragmentFaqsBinding.pbFaqsLoading.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    if(isNetworkAvailable(mContext)){
                        startWebView()
                        fragmentFaqsBinding.clInternetNotAvailable.visibility = View.GONE
                        fragmentFaqsBinding.wvFaqs.visibility = View.VISIBLE
                    }else{
                        fragmentFaqsBinding.clInternetNotAvailable.visibility = View.VISIBLE
                        fragmentFaqsBinding.wvFaqs.visibility = View.GONE
                    }
                    return false
                }else{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                fragmentFaqsBinding.pbFaqsLoading.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
//                Toast.makeText(this@PrivacyPolicyPage, "Error:$description", Toast.LENGTH_SHORT).show()
                fragmentFaqsBinding.clInternetNotAvailable.visibility = View.VISIBLE
                fragmentFaqsBinding.wvFaqs.visibility = View.GONE
            }
        }
    }

    /*private fun setupRecyclerView(){
        val layoutManagerFaqs = LinearLayoutManager(requireContext())
        fragmentFaqsBinding.recViewFaqs.apply {
            layoutManager = layoutManagerFaqs
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        drawerPartVM.getAllFaqDataFromDB().observe(viewLifecycleOwner, Observer {
            val faqAdapter = FaqDataAdapter(requireContext(), it)
            fragmentFaqsBinding.recViewFaqs.adapter = faqAdapter
        })
    }

    override fun onApiCallStarted() {}

    override fun onSuccess(msg: String?) {}

    override fun onFailure(msg: String) {}*/
}
