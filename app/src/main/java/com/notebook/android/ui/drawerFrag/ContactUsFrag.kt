package com.notebook.android.ui.drawerFrag

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
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
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentContactUsBinding
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener
import com.notebook.android.ui.drawerFrag.listener.ContactUsListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.ContactSuccessDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ContactUsFrag : Fragment(), KodeinAware, View.OnClickListener, ContactUsListener,
    SuccessVerificationListener {

    private lateinit var fragContactUsBinding:FragmentContactUsBinding
    override val kodein by kodein()
    private val viewModelFactory : DrawerPartVMFactory by instance()
    private lateinit var navController: NavController
    private val drawerPartVM:DrawerPartVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(DrawerPartVM::class.java)
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView
    private var supportEmail:String ?= null
    private var supportPhone:String ?= null

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

        fragContactUsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_contact_us, container, false)
        drawerPartVM.contactUsListener = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragContactUsBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragContactUsBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        drawerPartVM.getHelpSupportData()
        return fragContactUsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        fragContactUsBinding.tvContactEmail.text = notebookPrefs.userContactEmail
        fragContactUsBinding.tvPhoneData.text = "+91-${notebookPrefs.userContactPhone}"
        fragContactUsBinding.imgButtonContactUs.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragContactUsBinding.imgButtonContactUs -> {
                val fName = fragContactUsBinding.edtFullName.text.toString()
                val mobile = fragContactUsBinding.edtPhoneNumber.text.toString()
                val email = fragContactUsBinding.edtEmailID.text.toString()
                val msg = fragContactUsBinding.edtWriteYourMessage.text.toString()

                if(TextUtils.isEmpty(fName)){
                    errorToastTextView.text = "Please enter your full name"
                    errorToast.show()
                    fragContactUsBinding.tvFullnameValidation.text = "Please enter your full name"
                }else if(TextUtils.isEmpty(mobile)){
                    errorToastTextView.text = "Please enter mobile number"
                    errorToast.show()
                    fragContactUsBinding.tvFullnameValidation.text = "Please enter your full name"
                }else if(mobile.length < 10){
                    errorToastTextView.text = "Please enter valid mobile number"
                    errorToast.show()
                }else if(TextUtils.isEmpty(email)){
                    errorToastTextView.text = "Please enter email address"
                    errorToast.show()
                    fragContactUsBinding.tvFullnameValidation.text = "Please enter your full name"
                }else if(!validateEmail(email)){
                    errorToastTextView.text = "Please enter valid email address"
                    errorToast.show()
                }else if(TextUtils.isEmpty(msg)){
                    errorToastTextView.text = "Please enter your message here"
                    errorToast.show()
                    fragContactUsBinding.tvFullnameValidation.text = "Please enter your full name"
                }else{
                    drawerPartVM.postContactUsDataServer(fName,mobile, email, msg)
                }
            }
        }
    }

    override fun onApiCallStarted() {
      loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccess(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        val successDialog = ContactSuccessDialog()
        successDialog.isCancelable = false
        successDialog.setSuccessListener(this)
        val bundle = Bundle()
        bundle.putString("msg", msg)
        successDialog.arguments = bundle
        successDialog.show(mActivity.supportFragmentManager, "Verification Successful !!")
    }

    override fun onSuccesHelpSupportData(helpSupportData: HelpSupportData.HelpSupportMain) {
        notebookPrefs.userContactEmail = helpSupportData.email
        notebookPrefs.userContactPhone = helpSupportData.phone
        supportEmail = notebookPrefs.userContactEmail
        supportPhone = notebookPrefs.userContactPhone

        fragContactUsBinding.tvContactEmail.text = notebookPrefs.userContactEmail
        fragContactUsBinding.tvPhoneData.text = "+91-${notebookPrefs.userContactPhone}"
    }

    override fun onFailure(error: String) {
       loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = error
        errorToast.show()
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

    override fun userRegisteredSuccessfully(isSuccess: Boolean) {
        navController.popBackStack(R.id.homeFrag, false)
    }
}