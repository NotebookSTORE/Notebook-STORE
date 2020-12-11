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
import androidx.navigation.Navigation
import com.facebook.*
import com.facebook.internal.ImageRequest
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail
import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentLoginBinding
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.responseListener.AuthLoginListener
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.Constant.FACEBOOK_LOGIN
import com.notebook.android.utility.Constant.GOOGLE_LOGIN
import com.notebook.android.utility.Constant.USER_TYPE_FACEBOOK_USER
import com.notebook.android.utility.Constant.USER_TYPE_GOOGLE_USER
import com.notebook.android.utility.Constant.WITHOUT_SOCIAL_LOGIN
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginFrag : Fragment(), KodeinAware,
    View.OnClickListener, AuthLoginListener {

    companion object{
        const val GOOGLE_SIGN_IN_CODE = 201
    }

    private lateinit var loginBinding: FragmentLoginBinding
    private lateinit var navController: NavController
    private lateinit var user: FirebaseUser

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance<AuthViewModelFactory>()
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    //Google Sign in instances...
    private var mGoogleSigninClient:GoogleSignInClient ?= null
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mCallbackManager: CallbackManager
    private lateinit var notebookPrefs: NotebookPrefs
    private var isEmail:Boolean = false

    private lateinit var errorToast:Toast
    private lateinit var successToast:Toast
    private lateinit var errorToastTextView:TextView
    private lateinit var successToastTextView:TextView

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
        FacebookSdk.sdkInitialize(mActivity)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        loginBinding.lifecycleOwner = this
        authViewModel.authLoginListener = this
        notebookPrefs = NotebookPrefs(mContext)
        setupGoogleInstances()
        setupFacebookInstances()
        setLoginTextClickable()

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            loginBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            loginBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.e("instance token", " :: ${it.token}")
            notebookPrefs.firebaseDeviceID = it.token
        }

        loginBinding.edtEmailAddr.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val value: String = p0.toString()
                isEmail = !isNumeric(value)
            }
        })
        return loginBinding.root
    }

    private var pattern:Pattern = Pattern.compile("-?\\d+(.\\d+)?")
    fun isNumeric(str: String): Boolean {
        return pattern.matcher(str).matches()
    }

    private fun setLoginTextClickable(){
        val ssLoginText = SpannableString(resources.getString(R.string.stringLoginNoAccount))
        ssLoginText.setSpan(
            ForegroundColorSpan(Color.parseColor("#ffffff")),
            22, ssLoginText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssLoginText.setSpan(StyleSpan(Typeface.BOLD), 22, ssLoginText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanRegNow =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                navController.navigate(R.id.action_loginFrag_to_signUpFrag)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssLoginText.setSpan(spanRegNow, 22, ssLoginText.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        loginBinding.tvDontHaveAccount.movementMethod = LinkMovementMethod.getInstance()
        loginBinding.tvDontHaveAccount.text = ssLoginText
    }

    private fun setupGoogleInstances() {
        mAuth = FirebaseAuth.getInstance()

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSigninClient = GoogleSignIn.getClient(mActivity, gso)
    }

    private fun setupFacebookInstances(){
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        LoginManager.getInstance().registerCallback(mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        setFacebookData(result!!)
//                        handleFacebookAccessToken(result?.accessToken?: AccessToken.getCurrentAccessToken())
                        Log.e("FB Callback", " :: on success called !!")
                    }

                    override fun onCancel() {
//                        errorToastTextView.text = ""
//                        errorToast.show()
                        Log.e("FB Callback", " :: onCancel called !!")
                    }

                    override fun onError(error: FacebookException?) {
                        if (error is FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut()
                            }
                        }
                        errorToastTextView.text = error?.localizedMessage
                        errorToast.show()
                        Log.e("FB Callback", " :: onError called !! :: ${error.toString()}")
                    }
                })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        loginBinding.btnLogin.setOnClickListener(this)
        loginBinding.cvFacebookLogin.setOnClickListener(this)
        loginBinding.cvGoogleLogin.setOnClickListener(this)
        loginBinding.tvForgotPwd.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){

            loginBinding.cvFacebookLogin -> {
                LoginManager.getInstance().logInWithReadPermissions(mActivity, listOf("email", "public_profile"))
            }

            loginBinding.cvGoogleLogin -> {
                mGoogleSigninClient?.signOut()?.addOnCompleteListener(mActivity) {
                    googleSignIn()
                }
            }

            loginBinding.btnLogin -> {
                val email = loginBinding.edtEmailAddr.text.toString()
                val password = loginBinding.edtPassword.text.toString()

                Log.e("isEmail", " :: ${isEmail}")
                if(isEmail){
                    if(TextUtils.isEmpty(email)){
                        loginBinding.tilEmailAddr.error = "Please enter email address"
                    }else if(!validateEmail(email)){
                        loginBinding.tilEmailAddr.error = "Please enter valid email address"
                    }else if(TextUtils.isEmpty(password)){
                        loginBinding.tilPassword.error = "Please enter password"
                        loginBinding.tilEmailAddr.error = ""
                    }else if(password.length < 8){
                        loginBinding.tilPassword.error = "Please enter atleast 8 char with alpha numeric password"
                        loginBinding.tilEmailAddr.error = ""
                    }else{
                        loginBinding.tilEmailAddr.error = ""
                        loginBinding.tilPassword.error = ""
                        authViewModel.login(email, password)
                    }
                }else{
                    if(TextUtils.isEmpty(email)){
                        loginBinding.tilEmailAddr.error = "Please enter mobile"
                    }else if(email.length < 10){
                        loginBinding.tilEmailAddr.error = "Please enter valid mobile number"
                    }else if(TextUtils.isEmpty(password)){
                        loginBinding.tilPassword.error = "Please enter password"
                        loginBinding.tilEmailAddr.error = ""
                    }else if(password.length < 8){
                        loginBinding.tilPassword.error = "Please enter atleast 8 char with alpha numeric password"
                        loginBinding.tilEmailAddr.error = ""
                    }else{
                        loginBinding.tilEmailAddr.error = ""
                        loginBinding.tilPassword.error = ""
                        authViewModel.login(email, password)
                    }
                }
            }

            loginBinding.tvForgotPwd -> {
                val navController = Navigation.findNavController(v)
                navController.navigate(R.id.action_loginFrag_to_resetPasswordByOptionFrag)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN_CODE){
            //Getting the GoogleSignIn Task
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?: GoogleSignInAccount.zaa("error")!!)
            } catch (e:ApiException) {
                mActivity.toastShow(e.message!!)
            }
        }
    }

    private fun googleSignIn() {
        //getting the google signin intent
        val signInIntent = mGoogleSigninClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
    }

    private fun firebaseAuthWithGoogle(accountData: GoogleSignInAccount) {
        Log.d("","firebaseAuthWithGoogle:" + accountData.id)

        //getting the auth credential
        val credential = GoogleAuthProvider.getCredential(accountData.idToken, null)

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential).addOnCompleteListener(mActivity) { task ->
                    if (task.isSuccessful) {
                        Log.d("", "signInWithCredential:success");
                        user = mAuth.currentUser!!
                        Log.e("user data", " :: ${user.displayName} :: ${user.email} :: ${user.photoUrl}" +
                                " :: ${user.phoneNumber} :: ${accountData.idToken}")
                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                            Log.e("instance token", " :: ${it.token}")
                            notebookPrefs.firebaseDeviceID = it.token
                        }

                        authViewModel.socialMobileLoginServer(user.displayName?:"",
                            user.email?:"", notebookPrefs.firebaseDeviceID?:"",
                            user.photoUrl.toString(), USER_TYPE_GOOGLE_USER, accountData.idToken?:"")
                    }
                }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("", "signInWithCredential:success")
                    user = mAuth.currentUser!!
                    Log.e("user data", " :: ${user.displayName} :: ${user.email}")
//                    updateUI(user)

                    if(!user.email.isNullOrEmpty()){
                        authViewModel.mobileLoginExistChecking(user.email!!, USER_TYPE_FACEBOOK_USER)
                        notebookPrefs.loginType = FACEBOOK_LOGIN
                    }else{
                        val loginFragDirections : LoginFragDirections.ActionLoginFragToSocialLoginVerify =
                            LoginFragDirections.actionLoginFragToSocialLoginVerify(user.email?:"", user.photoUrl.toString(), USER_TYPE_FACEBOOK_USER,user.displayName?:"")
                        navController.navigate(loginFragDirections)
                        notebookPrefs.loginType = FACEBOOK_LOGIN
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    errorToastTextView.text = task.exception.toString()
                    errorToast.show()
                    Log.w("", "signInWithCredential:failure", task.exception)
                    /* Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()*/
//                    updateUI(null)
                }

            }
    }

    private fun setFacebookData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response -> // Application code
            try {
                Log.i("Response", response.toString())
                val email = response.jsonObject.getString("email")
                val firstName =
                    response.jsonObject.getString("first_name")
                val lastName =
                    response.jsonObject.getString("last_name")
                val idToken = response.jsonObject.getString("id")
                var profileURL = ""
                if (Profile.getCurrentProfile() != null) {
                    profileURL = ImageRequest.getProfilePictureUri(
                        Profile.getCurrentProfile().id, 500, 500
                    ).toString()
                }
                Log.e("fbData", " :: $email :: $firstName :: $lastName :: $profileURL :: $idToken")
                val username = "$firstName $lastName"
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    Log.e("instance token", " :: ${it.token}")
                    notebookPrefs.firebaseDeviceID = it.token
                }

                authViewModel.socialMobileLoginServer("$firstName $lastName",
                    email?:"", notebookPrefs.firebaseDeviceID?:"",
                    profileURL, USER_TYPE_FACEBOOK_USER, idToken?:"")

               /* if(!email.isNullOrEmpty()){
                    authViewModel.mobileLoginExistChecking(email, USER_TYPE_FACEBOOK_USER)
                    notebookPrefs.loginType = FACEBOOK_LOGIN
                }else{
                    val loginFragDirections : LoginFragDirections.ActionLoginFragToSocialLoginVerify =
                        LoginFragDirections.actionLoginFragToSocialLoginVerify(email?:"", profileURL, USER_TYPE_FACEBOOK_USER,username)
                    navController.navigate(loginFragDirections)
                    notebookPrefs.loginType = FACEBOOK_LOGIN
                }*/
            } catch (e: JSONException) {
                errorToastTextView.text = e.localizedMessage
                errorToast.show()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,email,first_name,last_name")
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show Loading Dialog")
    }

    override fun onSuccess(user: User) {
        loadingDialog.dismissAllowingStateLoss()
        if (!user.address.isNullOrEmpty()){
            notebookPrefs.defaultAddr = user.address
        }
        notebookPrefs.isVerified = user.is_verified?:0
        notebookPrefs.walletAmount = user.wallet_amounts
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token
        Handler().postDelayed({
            navController.popBackStack()
        }, 1200)
    }

    override fun onSuccessSocial(user: User, imageUpdate: Int, loginType: Int) {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token
        if(imageUpdate == 1){
            notebookPrefs.loginType = WITHOUT_SOCIAL_LOGIN
            if(loginType == 1){
                notebookPrefs.loginTypeOnImageUpdated = GOOGLE_LOGIN
            }else{
                notebookPrefs.loginTypeOnImageUpdated = FACEBOOK_LOGIN
            }
        }else{
            if(loginType == 1){
                notebookPrefs.loginType = GOOGLE_LOGIN
            }else{
                notebookPrefs.loginType = FACEBOOK_LOGIN
            }
        }
        if (!user.address.isNullOrEmpty()){
            notebookPrefs.defaultAddr = user.address
        }
        notebookPrefs.isVerified = user.is_verified?:0
        notebookPrefs.walletAmount = user.wallet_amounts
        Handler().postDelayed({
            navController.popBackStack()
        }, 1200)
    }

    override fun onFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    private fun showErrorView(msg:String){
        loginBinding.clErrorView.visibility = View.VISIBLE
        loginBinding.tvErrorText.text = msg
        loginBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            loginBinding.clErrorView.visibility = View.GONE
            loginBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onLoginUserExist(status: Int, imageUpdate:Int, loginType:Int) {
        loadingDialog.dismissAllowingStateLoss()
        if(status == 1){
            if(imageUpdate == 1){
                notebookPrefs.loginType = WITHOUT_SOCIAL_LOGIN
                if(loginType == 1){
                    notebookPrefs.loginTypeOnImageUpdated = GOOGLE_LOGIN
                }else{
                    notebookPrefs.loginTypeOnImageUpdated = FACEBOOK_LOGIN
                }
            }else{
                if(loginType == 1){
                    notebookPrefs.loginType = GOOGLE_LOGIN
                }else{
                    notebookPrefs.loginType = FACEBOOK_LOGIN
                }
            }
            Handler().postDelayed({
                navController.popBackStack()
            }, 1200)
        }else{
            val loginFragDirections : LoginFragDirections.ActionLoginFragToSocialLoginVerify =
                LoginFragDirections.actionLoginFragToSocialLoginVerify(user.email?:"", user.photoUrl.toString(), loginType,user.displayName?:"")
            navController.navigate(loginFragDirections)
        }
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }
}