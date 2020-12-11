package com.notebook.android.ui.myAccount.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.gocashfree.cashfreesdk.CFPaymentService

import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentWalletBinding
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.payment.PaymentData
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.ui.myAccount.address.listener.AddWalletResponseListener
import com.notebook.android.ui.myAccount.profile.ProfileVM
import com.notebook.android.ui.myAccount.profile.ProfileVMFactory
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class WalletFrag : Fragment(), View.OnClickListener, KodeinAware,
    UserLogoutDialog.UserLoginPopupListener, AddWalletResponseListener {

    private lateinit var fragmentWalletBinding: FragmentWalletBinding
    private lateinit var navController: NavController
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

    private var afterPaymentData: MutableLiveData<AfterPaymentRawData> = MutableLiveData()
    private var paymentLiveData: MutableLiveData<PaymentData> = MutableLiveData()
    private var userData:User ?= null
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

        fragmentWalletBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_wallet, container, false)
        fragmentWalletBinding.lifecycleOwner = this
        profileVM.addWalletListener = this

        if(arguments != null){
            val walletArgs = WalletFragArgs.fromBundle(requireArguments())
            fragmentWalletBinding.tvAvailableBalanceText.text = "Available Balance ₹ ${walletArgs.walletAmount}"
        }

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentWalletBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentWalletBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragmentWalletBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        profileVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                userData = user
            }else{
                userData = user
            }
        })

        paymentLiveData.observe(viewLifecycleOwner, Observer {
            if(it !=null){
                val paymentMethodDirections: WalletFragDirections.ActionWalletFragToPaymentCODSuccessScreen =
                    WalletFragDirections.actionWalletFragToPaymentCODSuccessScreen(it.orderID,
                        it.orderAmount, it.status, it.msg)
                navController.navigate(paymentMethodDirections)
            }
        })

        afterPaymentData.observe(viewLifecycleOwner, Observer {
            if(it != null){
                val walletSuccessRaw = WalletSuccess(it.userID, it.token, it.orderId, it.status)
                profileVM.walletSuccessAfterAddFromServer(walletSuccessRaw, it.amount, it.txtMsg)
            }
        })

        profileVM.cfTokenObserver.observe(viewLifecycleOwner, Observer {
            doPayment(it.cftoken!!, it.orderid!!, it.amount.toString(), userData!!.phone!!, userData!!.email!!)
        })

        fragmentWalletBinding.btnProceedToAddMoney.setOnClickListener(this)
    }

    private fun showErrorView(msg:String){
        fragmentWalletBinding.clErrorView.visibility = View.VISIBLE
        fragmentWalletBinding.tvErrorText.text = msg
        fragmentWalletBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentWalletBinding.clErrorView.visibility = View.GONE
            fragmentWalletBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onClick(v: View?) {
        when(v){
            fragmentWalletBinding.btnProceedToAddMoney -> {
                val amount = fragmentWalletBinding.edtAddAmount.text.toString()
                if (TextUtils.isEmpty(amount)){
                    showErrorView("Please enter amount to add")
                }else if (amount.toInt() < 10){
                    showErrorView("Please enter amount 10 or above to add into your wallet !!")
                }else{

                    if(userData != null){
                        if (!userData!!.phone.isNullOrEmpty()) {
                            if (notebookPrefs.isVerified == 1) {
                                val addWallet = AddWallet(amount.toFloat(), userData!!.id, userData!!.token!!)
                                profileVM.addWalletFromServer(addWallet)
                            }else{
                                errorToastTextView.text = "Please verify your phone number for proceed to payment"
                                errorToast.show()
                                navController.navigate(R.id.addDetailFrag)
                            }
                        }else{
                            errorToastTextView.text = "Please update your phone number for proceed to payment"
                            errorToast.show()
                            navController.navigate(R.id.addDetailFrag)
                        }
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

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onApiAfterWalletAddCall() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccess(orderID:String, status:Int, amount:Float, txtMsg:String) {
        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = "Amount added successfully"
        successToast.show()
        val paymentData = PaymentData(orderID, amount, txtMsg,status)
        paymentLiveData.value = paymentData
    }

    override fun onSuccessCFToken() {
        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        profileVM.deleteUser()
        profileVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onWalletFailure(orderID:String, status:Int, amount:Float, txtMsg:String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = txtMsg
        errorToast.show()
        val paymentData = PaymentData(orderID, amount, txtMsg,status)
        paymentLiveData.value = paymentData
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

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
//        super.onActivityResult(requestCode, resultCode, data)
        //Same request code for all payment APIs.
        //Prints all extras. Replace with app logic.

        if (resultCode == Activity.RESULT_OK) {
            Log.e("CFToken", "ReqCode :  :: ${CFPaymentService.PARAM_CUSTOMER_EMAIL}" + CFPaymentService.REQ_CODE)
        }

        if (data != null) {
            val bundle = data.extras
            if (bundle != null)
                for (key in bundle.keySet()) {
                    if (bundle.getString(key) != null) {
                        Log.e("CFToken", key + " : " + bundle.getString(key))
                        val orderId = bundle.getString("orderId")
                        val orderAmount = bundle.getString("orderAmount")
                        val txtStatus = bundle.getString("txStatus")
                        val txtMsg = bundle.getString("txMsg")
                        if(txtStatus.equals("SUCCESS")){
                            val afterPaymentRawData = AfterPaymentRawData(1, 1,
                                userData!!.token!!, userData!!.id, orderId!!, orderAmount!!.toFloat(),
                                1, txtMsg?:"",0)
                           afterPaymentData.value = afterPaymentRawData
                        }else if(txtStatus.equals("FAILED")){
                            val afterPaymentRawData = AfterPaymentRawData(0, 1,
                                userData!!.token!!, userData!!.id, orderId!!,
                                orderAmount!!.toFloat(), 1, txtMsg?:"", 0)
                            afterPaymentData.value = afterPaymentRawData
                        }else{
                            errorToastTextView.text  = "Transaction cancelled"
                            errorToast.show()
                        }
                    }
                }
        }
    }

    private fun doPayment(cfToken:String, orderId:String, orderAmount:String, custPhone:String, custEmail:String){
        Log.e("cfTokenGenerationData", " :: $orderId  :: $cfToken :: $custPhone :: $custEmail")
        val dataSendMap = HashMap<String, String>()
        dataSendMap.put("appId", "641661082cab7d0e28c654cef66146")
        dataSendMap.put("orderId", orderId)
        dataSendMap.put("orderAmount", orderAmount)
        dataSendMap.put("orderCurrency", "INR")
        dataSendMap.put("customerPhone", custPhone)
        dataSendMap.put("customerEmail", custEmail)

        CFPaymentService.getCFPaymentServiceInstance().doPayment(mActivity, dataSendMap, cfToken, "PROD",
            "#1979BC", "#FFFFFF", false)
    }
}
