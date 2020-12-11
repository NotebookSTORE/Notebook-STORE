package com.notebook.android.ui.merchant.frag

import android.content.Context
import android.content.Intent
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
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.notebook.android.BuildConfig
import com.notebook.android.R
import com.notebook.android.adapter.merchant.SliderAdapter
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentMerchantMainBinding
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.merchant.MerchantViewModel
import com.notebook.android.ui.merchant.responseListener.MerchantBenefitListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.CouponAlertDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList

class MerchantMainFrag : Fragment(), View.OnClickListener, KodeinAware, MerchantBenefitListener{

    private lateinit var merchantMainBinding:FragmentMerchantMainBinding
    private val imgArray:Array<Int> = arrayOf(R.drawable.slide1, R.drawable.slide2)

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var navController: NavController
    override val kodein by kodein()
    private val viewModelFactory: MerchantVMFactory by instance()
    private val merchantVM: MerchantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MerchantViewModel::class.java)
    }

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
        merchantVM.merchantBenefitData()
    }

    private lateinit var merchantBannerList:ArrayList<Banner>
    private lateinit var timer:Timer
    private val DELAY_MS: Long = 4000 //delay in milliseconds before task is to be executed
    private val PERIOD_MS: Long = 6000 // time in milliseconds between successive task executions.

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        merchantMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_merchant_main, container, false
        )
        merchantMainBinding.lifecycleOwner = this
        merchantVM.merchantBenefitListener = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(
            R.layout.custom_toast_layout,
            merchantMainBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(
            R.layout.error_custom_toast_layout,
            merchantMainBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        setupTextClickable()
        merchantMainBinding.btnApplyPrimeMerchantON.setOnClickListener(this)
        merchantMainBinding.btnApplyRegularMerchantON.setOnClickListener(this)
        merchantMainBinding.imgAppLogo.setOnClickListener(this)

        merchantVM.getBannerData(Constant.BANNER_TYPE_MERCHANT)

        merchantBannerList = ArrayList()
        timer = Timer()
        timer.scheduleAtFixedRate(SliderTimerForMerchant(), DELAY_MS, PERIOD_MS)
        return merchantMainBinding.root
    }

    internal inner class SliderTimerForMerchant() : TimerTask() {
        override fun run() {
            activity?.let {
                it.runOnUiThread {
                    if (merchantMainBinding.vpImageSlider.currentItem < merchantBannerList.size - 1) {
                        merchantMainBinding.vpImageSlider.currentItem=
                            merchantMainBinding.vpImageSlider.currentItem + 1
                    } else {
                        merchantMainBinding.vpImageSlider.currentItem = 0
                    }
                }
            }
        }
    }

    private fun setupTextClickable(){
        //Text Clickable for more details of Regular Merchants....
        val ssRegularMoreDetail = SpannableString(resources.getString(R.string.strMoreDetails))
        ssRegularMoreDetail.setSpan(
            ForegroundColorSpan(Color.parseColor("#222222")),
            17, ssRegularMoreDetail.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssRegularMoreDetail.setSpan(
            StyleSpan(Typeface.BOLD),
            17,
            ssRegularMoreDetail.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        ssRegularMoreDetail.setSpan(UnderlineSpan(), 17, ssRegularMoreDetail.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanRegNow =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                navController.navigate(R.id.action_merchantMainFrag_to_merchantRegularFrag)
                Log.e("", " :: fldjflkdsjfljf")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssRegularMoreDetail.setSpan(
            spanRegNow,
            17,
            ssRegularMoreDetail.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        merchantMainBinding.tvForMoreRegularMerchantDetails.movementMethod = LinkMovementMethod.getInstance()
        merchantMainBinding.tvForMoreRegularMerchantDetails.text = ssRegularMoreDetail

        //Text Clickable for more details of Prime Merchants....
        val ssPrimeMoreDetail = SpannableString(resources.getString(R.string.strMoreDetails))
        ssPrimeMoreDetail.setSpan(
            ForegroundColorSpan(Color.parseColor("#222222")),
            17, ssPrimeMoreDetail.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssPrimeMoreDetail.setSpan(
            StyleSpan(Typeface.BOLD),
            17,
            ssPrimeMoreDetail.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        ssPrimeMoreDetail.setSpan(UnderlineSpan(), 17, ssPrimeMoreDetail.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanPrimeMerchant =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                navController.navigate(R.id.action_merchantMainFrag_to_merchantPrimeFrag)
                Log.e("", " :: fldjflkdsjfljf")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssPrimeMoreDetail.setSpan(
            spanPrimeMerchant,
            17,
            ssPrimeMoreDetail.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        merchantMainBinding.tvForMorePrimeMerchantDetails.movementMethod = LinkMovementMethod.getInstance()
        merchantMainBinding.tvForMorePrimeMerchantDetails.text = ssPrimeMoreDetail
    }

    private var userData: User?= null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        merchantVM.getUserData().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                userData = it
                merchantVM.fetchAddressFromServer(userData!!.id, userData!!.token!!)

                if (userData!!.usertype == 1) {
                    if (userData!!.status == 0) {
                        if (notebookPrefs.primeUserUpgradeAvail == 1) {
                            navController.navigate(R.id.primeMerchantFormFrag)
                        }
                    } else if (userData!!.status == 1) {
                        merchantMainBinding.clRegularMerchant.visibility = View.GONE
                        merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                        merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                        merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                    } else {
                        merchantMainBinding.clRegularMerchant.visibility = View.GONE
                        merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                        merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                        merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                    }
                } else if (userData!!.usertype == 0) {
                    if (userData!!.status == 0) {
                        merchantMainBinding.clRegularMerchant.visibility = View.GONE
                        merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                        merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                        merchantMainBinding.btnApplyRegularMerchantON.isEnabled = false
                    } else if (userData!!.status == 1) {
                        merchantMainBinding.clRegularMerchant.visibility = View.VISIBLE
                        merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                        merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                        merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                    } else {
                        merchantMainBinding.clRegularMerchant.visibility = View.VISIBLE
                        merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                        merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                        merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                    }
                } else {
                    merchantMainBinding.clRegularMerchant.visibility = View.VISIBLE
                    merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                    merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
                    merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                }
            } else {
                userData = null
                merchantMainBinding.clRegularMerchant.visibility = View.VISIBLE
                merchantMainBinding.clPrimeMerchant.visibility = View.VISIBLE
                merchantMainBinding.btnApplyRegularMerchantON.isEnabled = true
                merchantMainBinding.btnApplyPrimeMerchantON.isEnabled = true
            }
        })
    }

    override fun onClick(v: View?) {
        when(v){

            merchantMainBinding.btnApplyPrimeMerchantON -> {
                if (userData != null) {
                    if (userData!!.usertype == 1) {
                        if (userData!!.status == 1) {
                            navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                        } else if (userData!!.status == 0) {

                        } else {
                            val userLoginRequestPopup = CouponAlertDialog()
                            userLoginRequestPopup.isCancelable = true
                            val bundle = Bundle()
                            bundle.putString(
                                "displayTitle",
                                "Your Prime Merchant KYC is Successfully Submitted.\n\n" +
                                        "Pending for KYC Approval"
                            )
                            userLoginRequestPopup.arguments = bundle
                            userLoginRequestPopup.show(
                                mActivity.supportFragmentManager,
                                "User login request popup !!"
                            )
                        }
                    } else if (userData!!.usertype == 0) {
                        if (userData!!.status == 0) {
                            navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                        } else if (userData!!.status == 1) {
                            navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                        } else {
//                            navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                            val userLoginRequestPopup = CouponAlertDialog()
                            userLoginRequestPopup.isCancelable = true
                            val bundle = Bundle()
                            bundle.putString(
                                "displayTitle",
                                "Your Regular Merchant KYC is Successfully Submitted.\n\n" +
                                        "Pending for KYC Approval"
                            )
                            userLoginRequestPopup.arguments = bundle
                            userLoginRequestPopup.show(
                                mActivity.supportFragmentManager,
                                "User login request popup !!"
                            )
                        }
                    } else {
                        navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                    }
                } else {
                    navController.navigate(R.id.action_merchantMainFrag_to_primeMerchantFormFrag)
                }
            }

            merchantMainBinding.btnApplyRegularMerchantON -> {
                if (userData != null) {
                    if (userData!!.usertype == 0) {
                        if (userData!!.status == 1) {
                            navController.navigate(R.id.action_merchantMainFrag_to_regularMerchantFormFrag)
                        } else if (userData!!.status == 0) {

                        } else {
//                            navController.navigate(R.id.action_merchantMainFrag_to_regularMerchantFormFrag)
                            val userLoginRequestPopup = CouponAlertDialog()
                            userLoginRequestPopup.isCancelable = true
                            val bundle = Bundle()
                            bundle.putString(
                                "displayTitle",
                                "Your Regular Merchant KYC is Successfully Submitted.\n\n" +
                                        "Pending for KYC Approval"
                            )
                            userLoginRequestPopup.arguments = bundle
                            userLoginRequestPopup.show(
                                mActivity.supportFragmentManager,
                                "User login request popup !!"
                            )
                        }
                    } else if (userData!!.usertype == 9) {
                        navController.navigate(R.id.action_merchantMainFrag_to_regularMerchantFormFrag)
                    }
                } else {
                    navController.navigate(R.id.action_merchantMainFrag_to_regularMerchantFormFrag)
                }
            }

            merchantMainBinding.imgAppLogo -> {
                openURL()
//                navController.navigate(R.id.action_merchantMainFrag_to_notebookPdfPage)
            }
        }
    }

    private fun openURL() {
        val pdfUrl = "${BuildConfig.SERVER_URL}${BuildConfig.NOTEBOOK_BENEFITS_PDF_URL}"
        val uri = Uri.parse("http://docs.google.com/viewer?url=$pdfUrl")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "text/html")
        startActivity(intent)
    }

    override fun onApiCallStarted() {
       /* if(mActivity.supportFragmentManager.findFragmentByTag("Show loading dialog") == null){
            loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
        }else{
            loadingDialog.requireDialog().show()
        }*/
    }

    override fun onSuccessBannerResponse(bannerresponse: List<Banner>?) {
        merchantBannerList = bannerresponse as ArrayList<Banner>
        val sliderAdapter = SliderAdapter(mContext, bannerresponse)
        merchantMainBinding.vpImageSlider.adapter = sliderAdapter
        merchantMainBinding.tlImageSliderIndicator.setupWithViewPager(merchantMainBinding.vpImageSlider)
    }

    override fun onSuccessResponse(successMsg: String, primeSubscriptionCharge: String) {
        notebookPrefs.primeSubscriptionCharge = primeSubscriptionCharge
    }

    override fun onSuccessDefaultAddress(defaultAddr: String) {
        notebookPrefs.defaultAddrModal = defaultAddr
    }

    override fun onFailure(msg: String) {
//       loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
//        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
//        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }
}
