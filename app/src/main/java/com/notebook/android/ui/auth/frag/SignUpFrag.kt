package com.notebook.android.ui.auth.frag

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail
import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.data.preferences.RefferalPreferance
import com.notebook.android.databinding.FragmentSignUpBinding
import com.notebook.android.model.auth.RegistrationResponse
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.responseListener.AuthResponseListener
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.dashboard.MainDashboardPage
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.VerificationPopupDialog
import com.notebook.android.ui.popupDialogFrag.VerificationSuccesDialog
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignUpFrag : Fragment(), KodeinAware, AuthResponseListener, OtpVerificationListener,
    SuccessVerificationListener {

    private lateinit var signUpBinding: FragmentSignUpBinding
    private var ssRegNow:SpannableString ?= null
    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance()
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }
    private lateinit var navController:NavController

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private val refferalPrefs: RefferalPreferance by lazy {
        RefferalPreferance(mContext)
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        signUpBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        signUpBinding.lifecycleOwner = this
        authViewModel.authResponseListener = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            signUpBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            signUpBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        ssRegNow = SpannableString(resources.getString(R.string.stringSignUpAlready))
        ssRegNow!!.setSpan(ForegroundColorSpan(Color.parseColor("#222222")),
            25, ssRegNow!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssRegNow!!.setSpan(StyleSpan(Typeface.BOLD), 25, ssRegNow!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        ssRegNow.setSpan(UnderlineSpan(), 25, ssRegNow.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanRegNow =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFrag, true).build()
                val navController = Navigation.findNavController(widget)
                navController.navigate(R.id.action_signUpFrag_to_loginFrag, null, navOptions)
                Log.e("", " :: fldjflkdsjfljf")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssRegNow!!.setSpan(spanRegNow, 25, ssRegNow!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        signUpBinding.tvAlreadyHaveAccount.movementMethod = LinkMovementMethod.getInstance()
        signUpBinding.tvAlreadyHaveAccount.text = ssRegNow

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.e("instance token", " :: $it")
            notebookPrefs.firebaseDeviceID = it
        }
        Log.e("deviceID", " :: ${notebookPrefs.firebaseDeviceID}")
        return signUpBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(refferalPrefs.refferCode?.isNotEmpty() == true || notebookPrefs.merchantRefferalID?.isNotEmpty() == true){
            signUpBinding.edtRefferalCode.apply {
                if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
                    notebookPrefs.merchantRefferalID = refferalPrefs.refferCode;
                }
                setText(notebookPrefs.merchantRefferalID)
                isEnabled = false
                isFocusable = false
            }
        }else{
            signUpBinding.edtRefferalCode.apply {
                isEnabled = true
                isFocusable = true
            }
        }

        navController = Navigation.findNavController(view)
        signUpBinding.btnSignUp.setOnClickListener {
            val email = signUpBinding.edtEmailAddr.text.toString()
            val mobNumber = signUpBinding.edtMobile.text.toString()
            val password = signUpBinding.edtPassword.text.toString()
            val confPassword = signUpBinding.edtConfirmPassword.text.toString()
            val fullname = signUpBinding.edtUsername.text.toString()
            val refferalCode = signUpBinding.edtRefferalCode.text.toString()

            if(TextUtils.isEmpty(fullname)){
                showErrorView("Please enter full name")
            } else if(TextUtils.isEmpty(email)){
                showErrorView("Please enter email address")
            }else if(!validateEmail(email)){
               showErrorView("Please enter valid email")
            }else if(TextUtils.isEmpty(mobNumber)){
                showErrorView("Please enter mobile number")
            }else if(mobNumber.length < 10){
                showErrorView("Please enter valid mobile number")
            }else if(TextUtils.isEmpty(password)){
                showErrorView("Please enter password")
            }else if(password.length < 8){
                showErrorView("Please enter atleast 8 char with alpha numeric password")
            }else if(TextUtils.isEmpty(confPassword)){
                showErrorView("Please enter confirm password")
            }else if(!password.equals(confPassword)){
                showErrorView("Your password not match, please check it again")
            }else{
                FirebaseMessaging.getInstance().token.addOnSuccessListener {
                    Log.e("instance token", " :: $it")
                    notebookPrefs.firebaseDeviceID = it
                }
                Log.e("deviceID", " :: ${notebookPrefs.firebaseDeviceID}")
                authViewModel.userSignUpCall(fullname, email, mobNumber, password,
                    notebookPrefs.firebaseDeviceID?:"",
                    Constant.NORMAL_MERCHANT_TYPE, refferalCode)
            }
        }
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show Loading Dialog")
    }

    override fun onSuccess(response: RegistrationResponse) {
        loadingDialog.dismissAllowingStateLoss()
        val verificationPopupDialog = VerificationPopupDialog()
        verificationPopupDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("mobile", signUpBinding.edtMobile.text.toString())
        bundle.putString("otp", response.otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(mActivity.supportFragmentManager, "Show Verification Popup !!")
    }

    override fun onFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onOtpSuccess(resp: User?) {
        if(resp != null){
            if (!resp.address.isNullOrEmpty()){
                notebookPrefs.defaultAddr = resp.address
            }
            notebookPrefs.isVerified = resp.is_verified?:0
            notebookPrefs.walletAmount = resp.wallet_amounts
            notebookPrefs.loginType = Constant.WITHOUT_SOCIAL_LOGIN
            notebookPrefs.userID = resp.id
            notebookPrefs.userToken = resp.token
            val verificationSuccessDialog = VerificationSuccesDialog()
            val bundle = Bundle()
            bundle.putString("successText", "User registered successfully")
            verificationSuccessDialog.setSuccessListener(this)
            verificationSuccessDialog.arguments = bundle
            verificationSuccessDialog.isCancelable = false
            verificationSuccessDialog.show(mActivity.supportFragmentManager, "Verification Successful !!")
        }else{
            errorToastTextView.text = "User data is not available"
            errorToast.show()
        }
        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInternetNotAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    private fun showErrorView(msg:String){
        signUpBinding.clErrorView.visibility = View.VISIBLE
        signUpBinding.tvErrorText.text = msg
        signUpBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            signUpBinding.clErrorView.visibility = View.GONE
            signUpBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun otpVerifyData(otpValue: String) {
        authViewModel.verifyOtp(signUpBinding.edtMobile.text.toString(), otpValue)
    }

    override fun resendOtpCall(resend: Boolean) {
        val email = signUpBinding.edtEmailAddr.text.toString()
        val mobNumber = signUpBinding.edtMobile.text.toString()
        val password = signUpBinding.edtPassword.text.toString()
        val fullname = signUpBinding.edtUsername.text.toString()
        val refferalCode = signUpBinding.edtRefferalCode.text.toString()
        authViewModel.userSignUpCall(fullname, email, mobNumber, password,
            notebookPrefs.firebaseDeviceID?:"",
            Constant.NORMAL_MERCHANT_TYPE, refferalCode)
    }

    override fun userRegisteredSuccessfully(isSuccess: Boolean) {
        notebookPrefs.loginType = Constant.WITHOUT_SOCIAL_LOGIN
        Handler().postDelayed({
            navController.popBackStack(R.id.homeFrag, false)
        }, 1200)
    }
}
