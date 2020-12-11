package com.notebook.android.ui.myAccount.helpSupport

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentFeedbackBinding
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.ui.myAccount.helpSupport.listener.HelpSupportListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.ConfirmationDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class FeedbackFrag : Fragment(), KodeinAware, HelpSupportListener,
    CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    ConfirmationDialog.ConfirmDialogDismiss, UserLogoutDialog.UserLoginPopupListener {

    private lateinit var fragmentFeedbackBinding: FragmentFeedbackBinding
    override val kodein by kodein()
    private val viewModelFactory : HelpSupportVMFactory by instance()
    private val helpSupportVM: HelpSupportVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(HelpSupportVM::class.java)
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private lateinit var navController: NavController
    private var user: User?= null

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext: Context
    private lateinit var mActivity:FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentFeedbackBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_feedback, container, false)
        helpSupportVM.helpSupportListener = this
        fragmentFeedbackBinding.lifecycleOwner = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentFeedbackBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentFeedbackBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragmentFeedbackBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        helpSupportVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                fragmentFeedbackBinding.tvUsernameFeedback.visibility = View.VISIBLE
                fragmentFeedbackBinding.tvUsernameFeedback.text = user.name
                this.user = user
            }else{
                fragmentFeedbackBinding.tvUsernameFeedback.visibility = View.GONE
                this.user = null
            }
        })

        fragmentFeedbackBinding.ratingBarFeedback.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, ratingValue, p2 ->
                Log.e("Rating Value", " :: $ratingValue")
                when (ratingValue) {
                    0.5F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Very Poor"
                    }
                    1.0F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Very Poor"
                    }
                    1.5F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Poor"
                    }
                    2.0F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Poor"
                    }
                    2.5F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Poor"
                    }
                    3.0F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Good"
                    }
                    3.5F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Good"
                    }
                    4.0F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Very Good"
                    }
                    4.5F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Excellent"
                    }
                    5.0F -> {
                        fragmentFeedbackBinding.tvRatingText.text = "Awesome"
                    }
                }
            }

        fragmentFeedbackBinding.scHideMyName.setOnCheckedChangeListener(this)
        fragmentFeedbackBinding.btnFeedbackSubmit.setOnClickListener(this)
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccesHelpSupportData(helpSupportData: HelpSupportData.HelpSupportMain) {

    }

    override fun onSuccess(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        val confirmDialog = ConfirmationDialog()
        confirmDialog.isCancelable = false
        val bundle = Bundle()
        confirmDialog.setDialogListener(this)
        bundle.putString("toastMsg", "Feedback register successfully !!")
        confirmDialog.arguments = bundle
        confirmDialog.show(mActivity.supportFragmentManager, "Custom Toast Popup !!")
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

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        helpSupportVM.deleteUser()
        helpSupportVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    private fun showErrorView(msg:String){
        fragmentFeedbackBinding.clErrorView.visibility = View.VISIBLE
        fragmentFeedbackBinding.tvErrorText.text = msg
        fragmentFeedbackBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentFeedbackBinding.clErrorView.visibility = View.GONE
            fragmentFeedbackBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun ondismissed() {
        Handler().postDelayed({
            navController.popBackStack()
        }, 1200)
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        if(p1){
            fragmentFeedbackBinding.tvUsernameFeedback.visibility = View.INVISIBLE
            fragmentFeedbackBinding.tvHideMyNameFeedback.text = "Show My Name"
        }else{
            fragmentFeedbackBinding.tvUsernameFeedback.visibility = View.VISIBLE
            fragmentFeedbackBinding.tvHideMyNameFeedback.text = "Hide My Name"
        }
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragmentFeedbackBinding.btnFeedbackSubmit -> {
                val feedbackMsg = fragmentFeedbackBinding.edtWriteReviews.text.toString()
                if(TextUtils.isEmpty(feedbackMsg)){
                    val msg = "Please type some reviews for Notebook Store !!"
                    showErrorView(msg)
                }else{
                    if(user != null){
                        val rating = fragmentFeedbackBinding.ratingBarFeedback.rating
                        helpSupportVM.registerAppFeedback(user!!.id!!, user!!.token!!, user!!.email!!, user!!.name!!,rating, feedbackMsg)
                    }else{
                        val userLoginRequestPopup = UserLogoutDialog()
                        userLoginRequestPopup.isCancelable = false
                        userLoginRequestPopup.setUserLoginRequestListener(this)
                        userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                    }
                }
            }
        }
    }
}
