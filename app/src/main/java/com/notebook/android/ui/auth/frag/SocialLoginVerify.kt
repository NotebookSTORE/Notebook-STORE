package com.notebook.android.ui.auth.frag

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.iid.FirebaseInstanceId
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail

import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentSocialLoginVerifyBinding
import com.notebook.android.model.auth.RegistrationResponse
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.responseListener.AuthResponseListener
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.VerificationPopupDialog
import com.notebook.android.ui.popupDialogFrag.VerificationSuccesDialog
import com.notebook.android.utility.Constant.FACEBOOK_LOGIN
import com.notebook.android.utility.Constant.GOOGLE_LOGIN
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SocialLoginVerify : Fragment(), KodeinAware, AuthResponseListener,
    SuccessVerificationListener, OtpVerificationListener {

    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance()
    private lateinit var fragSocialMobileBinding:FragmentSocialLoginVerifyBinding
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private lateinit var navController: NavController
    private lateinit var socialArgs:SocialLoginVerifyArgs

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

        fragSocialMobileBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_social_login_verify, container, false)
        authViewModel.authResponseListener = this

        if(arguments != null){
            socialArgs = SocialLoginVerifyArgs.fromBundle(requireArguments())
            Glide.with(mContext).load(socialArgs.profileImage).into(fragSocialMobileBinding.imgUserProfile)
            Log.e("mobileLoginData", " :: ${socialArgs.email} :: ${socialArgs.username}")
            fragSocialMobileBinding.edtEmailAddr.setText(socialArgs.email)
            fragSocialMobileBinding.edtFullName.setText(socialArgs.username)
        }

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragSocialMobileBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragSocialMobileBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragSocialMobileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        fragSocialMobileBinding.btnVerifyMobile.setOnClickListener {
            val mobNumber = fragSocialMobileBinding.edtMobile.text.toString()
            val email = fragSocialMobileBinding.edtEmailAddr.text.toString()
            val username = fragSocialMobileBinding.edtFullName.text.toString()

            if(TextUtils.isEmpty(username)){
                showErrorView("Please enter username")
            }else if(TextUtils.isEmpty(email)){
                showErrorView("Please enter email address")
            }else if(!validateEmail(email)){
                showErrorView("Please enter valid email")
            }else if(TextUtils.isEmpty(mobNumber)){
                showErrorView("Please enter mobile number")
            }else if(mobNumber.length < 10){
                showErrorView("Please enter valid mobile number")
            }else{
                fragSocialMobileBinding.tilMobile.isErrorEnabled = false
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    Log.e("instance token", " :: ${it.token}")
                    notebookPrefs.firebaseDeviceID = it.token
                }
                Log.e("deviceID", " :: ${notebookPrefs.firebaseDeviceID}")
                authViewModel.socialMobileLogin(username, email, notebookPrefs.firebaseDeviceID?:"",
                    socialArgs.userType, mobNumber, socialArgs.profileImage)
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
        bundle.putString("mobile", fragSocialMobileBinding.edtMobile.text.toString())
        bundle.putString("otp", response.otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(mActivity.supportFragmentManager, "Show Verification Popup !!")
    }

    override fun onFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
//        showErrorView(msg)
    }

    private fun showErrorView(msg:String){
        fragSocialMobileBinding.clErrorView.visibility = View.VISIBLE
        fragSocialMobileBinding.tvErrorText.text = msg
        fragSocialMobileBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragSocialMobileBinding.clErrorView.visibility = View.GONE
            fragSocialMobileBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onOtpSuccess(resp: User?) {
        if(resp != null){
            if (!resp.address.isNullOrEmpty()){
                notebookPrefs.defaultAddr = resp.address
            }
            val verificationSuccessDialog = VerificationSuccesDialog()
            verificationSuccessDialog.isCancelable = false
            verificationSuccessDialog.setSuccessListener(this)
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

    override fun otpVerifyData(otpValue: String) {
        authViewModel.verifyOtp(fragSocialMobileBinding.edtMobile.text.toString(), otpValue)
    }

    override fun resendOtpCall(resend: Boolean) {
        val mobNumber = fragSocialMobileBinding.edtMobile.text.toString()
        authViewModel.socialMobileLogin(socialArgs.username, socialArgs.email?:fragSocialMobileBinding.edtEmailAddr.text.toString(),
            notebookPrefs.firebaseDeviceID?:"", socialArgs.userType,
            mobNumber, socialArgs.profileImage)
    }

    override fun userRegisteredSuccessfully(isSuccess: Boolean) {
        if(socialArgs.userType == 1){
            notebookPrefs.loginType = GOOGLE_LOGIN
        }else{
            notebookPrefs.loginType = FACEBOOK_LOGIN
        }
        Handler().postDelayed({
            navController.popBackStack(R.id.homeFrag, false)
        }, 1200)
    }
}
