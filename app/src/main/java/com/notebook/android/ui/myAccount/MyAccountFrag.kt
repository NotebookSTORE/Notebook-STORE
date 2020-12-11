package com.notebook.android.ui.myAccount

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.max.ecomaxgo.maxpe.view.flight.utility.getUserImageFullPath
import com.notebook.android.BuildConfig
import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentMyAccountBinding
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.ui.dashboard.listener.LogoutListener
import com.notebook.android.ui.dashboard.listener.UserProfileUpdateListener
import com.notebook.android.ui.myAccount.profile.ProfileVM
import com.notebook.android.ui.myAccount.profile.ProfileVMFactory
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.CouponAlertDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.LogoutDialogFrag
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MyAccountFrag : Fragment(), View.OnClickListener, LogoutListener, KodeinAware,
    UserProfileUpdateListener, UserLogoutDialog.UserLoginPopupListener{

    private lateinit var fragmentMyAccountBinding: FragmentMyAccountBinding
    private lateinit var navController:NavController

    override val kodein by kodein()
    private val viewModelFactory: ProfileVMFactory by instance()
    private val profileVM: ProfileVM by lazy{
        ViewModelProvider(this, viewModelFactory).get(ProfileVM::class.java)
    }
    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private var userData:User ?= null
    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext:Context
    private lateinit var mActivity:FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    private var mGoogleSigninClient: GoogleSignInClient?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSigninClient = GoogleSignIn.getClient(mActivity, gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentMyAccountBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_account, container, false)
        fragmentMyAccountBinding.lifecycleOwner = this
        profileVM.profileUpdateListener = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentMyAccountBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentMyAccountBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        profileVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){

                userData = user
                fragmentMyAccountBinding.tvLogout.visibility = View.VISIBLE
                Log.e("img url", " :: ${getUserImageFullPath(user.profile_image?:"")}) :: ${user.profile_image}")

                if(user.name.isNullOrEmpty()){
                    fragmentMyAccountBinding.tvNameProfile.text = user.username?:"User Name"
                }else{
                    fragmentMyAccountBinding.tvNameProfile.text = user.name?:"User Name"
                }
                fragmentMyAccountBinding.tvUserEmail.text = user.email
                if (!user.address.isNullOrEmpty()){
                    notebookPrefs.defaultAddr = user.address
                }

                if(user.phone.isNullOrEmpty()){
                    fragmentMyAccountBinding.tvUserPhone.visibility = View.GONE
                }else{
                    fragmentMyAccountBinding.tvUserPhone.visibility = View.VISIBLE
                    fragmentMyAccountBinding.tvUserPhone.text = "+91 ${user.phone}"
                }

                if(userData!!.usertype == 1){
                    if(notebookPrefs.primeUserUpgradeAvail == 1){
                        fragmentMyAccountBinding.tvMerchantUpgradeLink.visibility = View.VISIBLE
                        fragmentMyAccountBinding.tvMerchantUpgradeLink.text = "Renew Subscription"

                        if(userData!!.registerfor == 2){
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime - Institute"
                        }else if(userData!!.registerfor == 1){
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime - Individual"
                        }else{
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime Merchant"
                        }
                    }else{
                        if(userData!!.status == 0){

                        }else if(userData!!.status == 1){

                        }else{
                            mHandler.postDelayed({
                                navController.popBackStack(R.id.homeFrag, false)
                            }, 10000)
                        }

                        if(userData!!.registerfor == 2){
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime - Institute"
                        }else if(userData!!.registerfor == 1){
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime - Individual"
                        }else{
                            fragmentMyAccountBinding.tvMerchantType.text = "Prime Merchant"
                        }
                        fragmentMyAccountBinding.tvMerchantUpgradeLink.visibility = View.GONE
                    }

                }else if(userData!!.usertype == 0){
                    if(userData!!.status == 0){

                    }else if(userData!!.status == 1){

                    }else{
                        mHandler.postDelayed({
                            navController.popBackStack(R.id.homeFrag, false)
                        }, 10000)
                    }
                    if(userData!!.registerfor == 2){
                        fragmentMyAccountBinding.tvMerchantType.text = "Regular - Institute"
                    }else if(userData!!.registerfor == 1){
                        fragmentMyAccountBinding.tvMerchantType.text = "Regular - Individual"
                    }else{
                        fragmentMyAccountBinding.tvMerchantType.text = "Regular Merchant"
                    }
                    fragmentMyAccountBinding.tvMerchantUpgradeLink.visibility = View.VISIBLE
                    fragmentMyAccountBinding.tvMerchantUpgradeLink.text = "Upgrade to Prime"

                }else{
                    fragmentMyAccountBinding.tvMerchantType.text = "Normal Merchant"
                    fragmentMyAccountBinding.tvMerchantUpgradeLink.visibility = View.VISIBLE
                    fragmentMyAccountBinding.tvMerchantUpgradeLink.text = "Upgrade Merchant"
                }

                if(user.wallet_amounts?.isNotEmpty() == true){
                   notebookPrefs.walletAmount = "${user.wallet_amounts}"
                }else{
                    notebookPrefs.walletAmount = "0"
                }

                //wallet get api callback....
                val walletAmountRaw = WalletAmountRaw(user.id, user.token!!)
                if (!userData!!.phone.isNullOrEmpty()) {
                    if (notebookPrefs.isVerified == 1) {
                        profileVM.getWalletAmountFromServer(walletAmountRaw)
                    }else{
                        errorToastTextView.text = "Please verify your phone number for proceed to payment"
                        errorToast.show()
//                        navController.navigate(R.id.addDetailFrag)
                    }
                }else{
                    errorToastTextView.text = "Please update your phone number for proceed to payment"
                    errorToast.show()
//                    navController.navigate(R.id.addDetailFrag)
                }

                if(notebookPrefs.loginType.equals(Constant.GOOGLE_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.my_account_profile)
                        .into(fragmentMyAccountBinding.imgUserProfile)
                }else if(notebookPrefs.loginType.equals(Constant.FACEBOOK_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.my_account_profile)
                        .into(fragmentMyAccountBinding.imgUserProfile)
                }else{
                    if(!user.profile_image.isNullOrEmpty()){
                        Glide.with(this)
                            .load(getUserImageFullPath(user.profile_image?:""))
                            .placeholder(R.drawable.my_account_profile)
                            .into(fragmentMyAccountBinding.imgUserProfile)
                    }
                }
            }else{
                userData = null
                fragmentMyAccountBinding.tvLogout.visibility = View.GONE
            }
        })

        fragmentMyAccountBinding.lifecycleOwner = this
        return fragmentMyAccountBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(notebookPrefs.defaultAddr.isNullOrEmpty()){
            fragmentMyAccountBinding.tvDefaultAddress.visibility = View.GONE
        }else{
            fragmentMyAccountBinding.tvDefaultAddress.visibility = View.VISIBLE
            fragmentMyAccountBinding.tvDefaultAddress.text = notebookPrefs.defaultAddr
        }

        if(notebookPrefs.walletAmount?.isNotEmpty() == true){
            fragmentMyAccountBinding.tvWalletAmountData.text = "₹ ${notebookPrefs.walletAmount}"
        }else{
            fragmentMyAccountBinding.tvWalletAmountData.text = "₹ 0"
        }

        navController = Navigation.findNavController(view)
        fragmentMyAccountBinding.imgEditDetail.setOnClickListener(this)
        fragmentMyAccountBinding.clMerchantDetails.setOnClickListener(this)
        fragmentMyAccountBinding.clMyWallet.setOnClickListener(this)
        fragmentMyAccountBinding.tvEditAddress.setOnClickListener(this)
        fragmentMyAccountBinding.clSendMerchantRequest.setOnClickListener(this)
        fragmentMyAccountBinding.clMyOrders.setOnClickListener(this)
        fragmentMyAccountBinding.clMyWishlist.setOnClickListener(this)
        fragmentMyAccountBinding.tvLogout.setOnClickListener(this)
        fragmentMyAccountBinding.clHelpAndSupport.setOnClickListener(this)
        fragmentMyAccountBinding.tvMerchantUpgradeLink.setOnClickListener(this)

    }

    /*fun showInfoToastWithTypeface(view: View) {
        KCustomToast.infoToast(mContext, "This is a custom info Toast with custom font",
            KCustomToast.GRAVITY_CENTER) *//*ResourcesCompat.getFont(context,R.font.bad_script)*//*
    }
*/
    override fun onClick(v: View?) {
        when(v){

            fragmentMyAccountBinding.imgEditDetail -> {
                profileVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
                    if(user != null){
                        navController.navigate(R.id.action_myAccountFrag_to_addDetailFrag)
                    }else{
                        navController.navigate(R.id.action_myAccountFrag_to_loginFrag)
                    }
                })
            }

            fragmentMyAccountBinding.tvMerchantUpgradeLink -> {
                val merchantLink = fragmentMyAccountBinding.tvMerchantUpgradeLink.text.toString()
                if(merchantLink.equals("Upgrade to Prime", true)){
                    if(userData != null){
                        if (userData!!.usertype == 1) {
                            if (userData!!.status == 1) {
                                navController.navigate(R.id.primeMerchantFormFrag)
                            }else if (userData!!.status == 0) {

                            } else {
                                val userLoginRequestPopup = CouponAlertDialog()
                                userLoginRequestPopup.isCancelable = true
                                val bundle = Bundle()
                                bundle.putString("displayTitle", "Your Prime Merchant KYC is Successfully Submitted.\n\n" +
                                        "Pending for KYC Approval")
                                userLoginRequestPopup.arguments = bundle
                                userLoginRequestPopup.show(
                                    mActivity.supportFragmentManager,
                                    "User login request popup !!"
                                )
                            }
                        }else if(userData!!.usertype == 0){
                            if(userData!!.status == 0){
                                navController.navigate(R.id.primeMerchantFormFrag)
                            }else  if(userData!!.status == 1){
                                navController.navigate(R.id.primeMerchantFormFrag)
                            }else{
                                val userLoginRequestPopup = CouponAlertDialog()
                                userLoginRequestPopup.isCancelable = true
                                val bundle = Bundle()
                                bundle.putString("displayTitle", "Your Regular Merchant KYC is Successfully Submitted.\n\n" +
                                        "Pending for KYC Approval")
                                userLoginRequestPopup.arguments = bundle
                                userLoginRequestPopup.show(
                                    mActivity.supportFragmentManager,
                                    "User login request popup !!"
                                )
                            }
                        }else{
                            navController.navigate(R.id.primeMerchantFormFrag)
                        }
                    }else{
                        navController.navigate(R.id.primeMerchantFormFrag)
                    }
                }else if(merchantLink.equals("Upgrade Merchant", true)){
                    navController.navigate(R.id.merchantMainFrag)
                }else{
                    navController.navigate(R.id.primeMerchantFormFrag)
                }
            }

            fragmentMyAccountBinding.clMerchantDetails -> {
                if (userData != null){
                    if(!userData!!.merchant_id.isNullOrEmpty()){
                        navController.navigate(R.id.action_myAccountFrag_to_merchantProfileFrag)
                    }else{
                        navController.navigate(R.id.merchantMainFrag)
                    }
                }else{
                    navController.navigate(R.id.merchantMainFrag)
                }
            }


            fragmentMyAccountBinding.clMyWallet -> {
                val myAccountFragDirections:MyAccountFragDirections.ActionMyAccountFragToWalletFrag =
                    MyAccountFragDirections.actionMyAccountFragToWalletFrag(notebookPrefs.walletAmount)
                navController.navigate(myAccountFragDirections)
            }

            fragmentMyAccountBinding.tvEditAddress -> {
                navController.navigate(R.id.action_myAccountFrag_to_savedAddressFrag)
            }

            fragmentMyAccountBinding.clSendMerchantRequest ->{
                createProductShareDeepLink()
//                navController.navigate(R.id.action_myAccountFrag_to_merchantMainFrag)
            }

            fragmentMyAccountBinding.clMyOrders ->{
                navController.navigate(R.id.action_myAccountFrag_to_orderFrag)
            }

            fragmentMyAccountBinding.clMyWishlist ->{
                navController.navigate(R.id.action_myAccountFrag_to_wishlistFrag)
            }

            fragmentMyAccountBinding.clHelpAndSupport ->{
                navController.navigate(R.id.action_myAccountFrag_to_helpSupportFrag)
            }

            fragmentMyAccountBinding.tvLogout ->{
                val logoutDialog = LogoutDialogFrag()
                logoutDialog.isCancelable = false
                logoutDialog.setLogoutListener(this)
                logoutDialog.show(mActivity.supportFragmentManager, "Show Logout Dialog")
            }
        }
    }

    private fun shareApp(){
        try {
            val refferalCode: String?
            if(userData != null){
                if(userData!!.referralcode.isNullOrEmpty()){
                    refferalCode = ""
                }else{
                    refferalCode = userData!!.referralcode
                }
            }else{
                refferalCode = ""
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Notebook Store")
            var shareMessage = "Hey. I found out a cool App where we can buy Stationary product\n\n"
            shareMessage = "${shareMessage}https://play.google.com/store/apps/details?id" +
                    "=${BuildConfig.APPLICATION_ID}&hl=en&referrer=${refferalCode}"/*${userData!!.referralcode}*/
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share with..."))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    fun createProductShareDeepLink() {
        Log.e("main", "create link ")
        val dynamicLink: DynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://demo.mbrcables.com/notebookstore/"))
            .setDynamicLinkDomain("notebookstore.page.link") // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build()) // Open links with com.example.ios on iOS
            //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
            .buildDynamicLink()
        //click -- link -- google play store -- inistalled/ or not  ----
        val dynamicLinkUri: Uri = dynamicLink.getUri()
        Log.e("main", "  Long refer " + dynamicLink.getUri())
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link

        Log.e("refferalCode", " :: ${userData?.referralcode}")
        val refferalCode: String?
        if(userData != null){
            if(userData!!.referralcode.isNullOrEmpty()){
                refferalCode = ""
            }else{
                refferalCode = userData!!.referralcode
            }
        }else{
            refferalCode = ""
        }

        // manual link
        val sharelinktext = "https://notebookstore.page.link/?" +
                "link=https://demo.mbrcables.com/notebookstore/myProductShare.php?reffer=$refferalCode" +
                "&apn=" + mActivity.packageName +
                "&st=" + "Notebook Store" +
                "&sd=" + "www.notebookstore.in"
//                "&si=" + "${Constant.PRODUCT_IMAGE_PATH}${prodModel.image}"

//        Log.e("imageLink", " :: ${Constant.PRODUCT_IMAGE_PATH}${prodModel.image}")


        // shorten the link
        val shortLinkTask: Task<ShortDynamicLink> = FirebaseDynamicLinks.getInstance()
            .createDynamicLink() //.setLongLink(dynamicLink.getUri())
            .setLongLink(Uri.parse(sharelinktext)) // manually
            .buildShortDynamicLink()
            .addOnCompleteListener(mActivity
            ) { task ->
                if (task.isSuccessful) {
                    // Short link created
                    val shortLink: Uri = task.result?.shortLink ?: Uri.EMPTY
                    val flowchartLink: Uri = task.result?.previewLink ?: Uri.EMPTY
                    Log.e("main ", "short link $shortLink")
                    // share app dialog

                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Notebook Store")
                        val shareMessage = "Hey. I found out a cool App where we can buy Stationary product\n\n$shortLink"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "Share with..."))
                    } catch (e: Exception) {
                        //e.toString();
                    }

                    /*Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shortLink.toString())
                        type = "text/plain"
                        startActivity(this)
                    }*/
                } else {
                    // Error
                    // ...
                    Log.e("main", " error " + task.exception)
                    errorToastTextView.text = task.exception?.localizedMessage
                    errorToast.show()
                }
            }
    }

    override fun logoutListener() {
        profileVM.userLogoutFromServer(userData!!.id, userData!!.token!!)
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Loading dialog show")
    }

    override fun onApiCallOtpVerifyStarted() {

    }

    override fun onSuccess(user: User?) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onOtpSuccess(resp: User?) {

    }

    override fun otpVerifyWhenProfileUpdate(otp: String?) {
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        profileVM.deleteUser()
        profileVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onSuccessLogout() {
        loadingDialog.dismissAllowingStateLoss()
        profileVM.deleteUser()
        profileVM.clearCartTableFromDB()
        notebookPrefs.clearPreference()

        notebookPrefs.defaultAddr = ""
        notebookPrefs.defaultAddrModal = ""

        Log.e("loginType", " :: ${notebookPrefs.loginType}")
        if(notebookPrefs.loginType.equals(Constant.GOOGLE_LOGIN, true)){
            logoutGoogleLogin()
            Log.e("loginType inside", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginType.equals(Constant.FACEBOOK_LOGIN, true)){
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            navController.popBackStack()
            navController.navigate(R.id.loginFrag)

            Log.e("loginType inside", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginTypeOnImageUpdated.equals(Constant.GOOGLE_LOGIN, true)){
            logoutGoogleLogin()
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginTypeOnImageUpdated.equals(Constant.FACEBOOK_LOGIN, true)){
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else{
            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
        }
    }

    private val mHandler:Handler = Handler()
    override fun onPause() {
        super.onPause()

        mHandler.removeCallbacksAndMessages(null)
    }

    private fun logoutGoogleLogin() {
//        mGoogleSigninClient?.revokeAccess()?.addOnCompleteListener(mActivity) {}
        mGoogleSigninClient?.signOut()?.addOnCompleteListener(mActivity) {
           FirebaseAuth.getInstance().signOut()
            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
        }
    }

    override fun walletAmount(amount: String) {
        loadingDialog.dismissAllowingStateLoss()
        if(amount.isNotEmpty()){
            if (amount.toFloat() <= 0) {
                notebookPrefs.walletAmount = "0"
                fragmentMyAccountBinding.tvWalletAmountData.text = "₹ 0"
            } else {
                notebookPrefs.walletAmount = amount
                fragmentMyAccountBinding.tvWalletAmountData.text = "₹ $amount"
            }
        }else{
            notebookPrefs.walletAmount = amount
            fragmentMyAccountBinding.tvWalletAmountData.text = "₹ 0"
        }

    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }
}
