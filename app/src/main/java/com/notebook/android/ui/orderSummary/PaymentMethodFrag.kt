package com.notebook.android.ui.orderSummary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.gocashfree.cashfreesdk.CFPaymentService
import com.google.gson.Gson
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentPaymentMethodBinding
import com.notebook.android.model.cashfree.CFTokenResponse
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.payment.PaymentData
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.Constant.PAYMENT_METHOD_CASHFREE
import com.notebook.android.utility.Constant.PAYMENT_METHOD_COD
import com.notebook.android.utility.Constant.PAYMENT_METHOD_WALLET
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PaymentMethodFrag : Fragment(), View.OnClickListener, KodeinAware, OrderResponseListener {

    private lateinit var fragmentPaymentMethodBinding: FragmentPaymentMethodBinding
    private lateinit var navController:NavController
    private lateinit var rdPayment:RadioButton
    private lateinit var paymentMethodSelectedValue:String
    private var addAmountValue:Float = 0f
    private lateinit var addressData:OrderPaymentDetail
    private var orderId:String ?= null

    override val kodein by kodein()
    private val viewModelFactory : OrderSummaryVMFactory by instance()
    private val orderSummaryVM: OrderSummaryVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(OrderSummaryVM::class.java)
    }
    private var paymentLiveData: MutableLiveData<PaymentData> = MutableLiveData()
    private var afterPaymentData: MutableLiveData<AfterPaymentRawData> = MutableLiveData()

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
    private var isAddWalletCall = false

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }
    private var paymentType:Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentPaymentMethodBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_payment_method, container, false)
        fragmentPaymentMethodBinding.lifecycleOwner = this
        orderSummaryVM.orderSummaryListener = this

        rdPayment = fragmentPaymentMethodBinding.root.findViewById(fragmentPaymentMethodBinding.
        rgPaymentMethod.checkedRadioButtonId)
        paymentMethodSelectedValue = rdPayment.text.toString()

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentPaymentMethodBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentPaymentMethodBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        if(arguments != null){
            val paymentMethodArgs =
                PaymentMethodFragArgs.fromBundle(
                    requireArguments()
                )
            fragmentPaymentMethodBinding.edtAmountToAdd.setText("₹ ${paymentMethodArgs.addWalletAmount}")
            addressData = Gson().fromJson(paymentMethodArgs.orderSummaryDetail, OrderPaymentDetail::class.java)
            addAmountValue = paymentMethodArgs.addWalletAmount
            orderSummaryVM.getWalletAmountFromServer(WalletAmountRaw(addressData.userID, addressData.token))
        }

        Log.e("cashonStatus", " :: ${notebookPrefs.codOptionPaymentAvail}")
        if(notebookPrefs.codOptionPaymentAvail){
            fragmentPaymentMethodBinding.rbCOD.visibility = View.VISIBLE
        }else{
            fragmentPaymentMethodBinding.rbCOD.visibility = View.GONE
        }

      /*  if(addressData.primeUpdated == 1){
            fragmentPaymentMethodBinding.rbCOD.visibility = View.GONE
        }else{
            fragmentPaymentMethodBinding.rbCOD.visibility = View.VISIBLE
        }*/

        notebookPrefs.orderSummaryCoupon = false
        return fragmentPaymentMethodBinding.root
    }

    private fun showErrorView(msg:String){
        fragmentPaymentMethodBinding.clErrorView.visibility = View.VISIBLE
        fragmentPaymentMethodBinding.tvErrorText.text = msg
        fragmentPaymentMethodBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentPaymentMethodBinding.clErrorView.visibility = View.GONE
            fragmentPaymentMethodBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        fragmentPaymentMethodBinding.rgPaymentMethod.setOnCheckedChangeListener {_, checkedId ->
            rdPayment = fragmentPaymentMethodBinding.root.findViewById(checkedId) as RadioButton
            Log.e("selected radio value", " :: ${rdPayment.text}")
            paymentMethodSelectedValue = rdPayment.text.toString()
        }

        orderSummaryVM.cfTokenObserver.observe(viewLifecycleOwner, Observer {
            if (it != null){
                Log.e("bugs", " :: cfTokenObserver")
                doPayment(it.cftoken!!, it.orderid!!, it.amount.toString(), addressData.phone, addressData.email)
            }
        })

        paymentLiveData.observe(viewLifecycleOwner, Observer {
            if(it !=null){
                val paymentMethodDirections: PaymentMethodFragDirections.ActionPaymentMethodFragToPaymentCODSuccessScreen =
                    PaymentMethodFragDirections.actionPaymentMethodFragToPaymentCODSuccessScreen(it.orderID,
                        it.orderAmount, it.status, it.msg)
                navController.navigate(paymentMethodDirections)
            }
        })

        afterPaymentData.observe(viewLifecycleOwner, Observer {
            if(it != null){
                if (!isAddWalletCall){
                    orderSummaryVM.paymentSaveToDB(it)
                }
            }
        })

        afterPaymentData.observe(viewLifecycleOwner, Observer {
            if(it != null){
                if (isAddWalletCall){
                    val walletSuccessRaw = WalletSuccess(it.userID, it.token, it.orderId, it.status)
                    orderSummaryVM.walletSuccessAfterAddFromServer(walletSuccessRaw, it.amount, it.txtMsg)
                }
            }
        })

        fragmentPaymentMethodBinding.btnNextPaymentMethod.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            fragmentPaymentMethodBinding.btnNextPaymentMethod -> {
                if(paymentMethodSelectedValue.equals("Cashfree",true)){
                    addressData.paymentmethod = PAYMENT_METHOD_CASHFREE
                    paymentType = 1
                    orderSummaryVM.orderPlacedByCOD(addressData, PAYMENT_METHOD_CASHFREE)
                }else  if(paymentMethodSelectedValue.equals("Wallet",true)){
                    addressData.paymentmethod = PAYMENT_METHOD_WALLET
                    paymentType = 2

                    Log.e("walletResponse", " :: ")
                    if(notebookPrefs.walletAmount!!.toFloat().compareTo(addressData.amountafterdiscount) >= 0){
                        orderSummaryVM.orderPlacedByCOD(addressData, PAYMENT_METHOD_WALLET)
                        isAddWalletCall = false
                    }else{
                        isAddWalletCall = true
                        if (notebookPrefs.walletAmount!!.toFloat() <= 0) {
                            notebookPrefs.walletAmount = "0"
                        }
                        val amountAddToWallet = addressData.amountafterdiscount.minus(notebookPrefs.walletAmount!!.toFloat())
                        val addWallet = AddWallet(amountAddToWallet, addressData.userID, addressData.token)
                        orderSummaryVM.addWalletFromServer(addWallet)
                    }
                }else{
                    addressData.paymentmethod = PAYMENT_METHOD_COD
                    paymentType = 0
                    orderSummaryVM.orderPlacedByCOD(addressData, PAYMENT_METHOD_COD)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
//        super.onActivityResult(requestCode, resultCode, data)
        //Same request code for all payment APIs.
        //Prints all extras. Replace with app logic.

        Log.e("reqCode", "${CFPaymentService.REQ_CODE}")

        if (resultCode == Activity.RESULT_OK) {
            Log.e("CFToken", "ReqCode :  :: ${CFPaymentService.PARAM_CUSTOMER_EMAIL}" + CFPaymentService.REQ_CODE)
        }else{
            Log.e("reqCodeElse", "${CFPaymentService.REQ_CODE}")
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
                            val afterPaymentRawData = AfterPaymentRawData(1, paymentType,
                                addressData.token, addressData.userID, orderId!!, addressData.amountafterdiscount,
                                addressData.paymentType, txtMsg?:"", addressData.primeUpdated)
                            afterPaymentData.value = afterPaymentRawData
                        }else if(txtStatus.equals("FAILED")){
                            val afterPaymentRawData = AfterPaymentRawData(0, paymentType,
                                addressData.token, addressData.userID, orderId!!, addressData.amountafterdiscount,
                                addressData.paymentType, txtMsg?:"", addressData.primeUpdated)
                            afterPaymentData.value = afterPaymentRawData
                        }else{
                            errorToastTextView.text = "Transaction cancelled"
                            errorToast.show()
                        }
                    }
                }
        }else{
            Log.e("reqCode", "${CFPaymentService.REQ_CODE}")
        }
    }

    //api key -> 641661082cab7d0e28c654cef66146
    //secret key -> 11966d7df5942bcf192f0f5a8ebfbd98ab8535a1
    private fun doPayment(cfToken:String, orderId:String, orderAmount:String, custPhone:String, custEmail:String){
        val dataSendMap = HashMap<String, String>()
        dataSendMap.put("appId", "641661082cab7d0e28c654cef66146")
        dataSendMap.put("orderId", orderId)
        dataSendMap.put("orderAmount", orderAmount)
        dataSendMap.put("orderCurrency", "INR")
        dataSendMap.put("customerPhone", custPhone)
        dataSendMap.put("customerEmail", custEmail)

        CFPaymentService.getCFPaymentServiceInstance().doPayment(mActivity, dataSendMap,
            cfToken, "PROD", "#1979BC", "#FFFFFF", false)
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onApiCallAfterPayment() {
        loadingDialog.showsDialog = true
    }

    override fun onSuccessOrder(it: CFTokenResponse, type: String) {
        loadingDialog.dismissAllowingStateLoss()
        orderId = it.orderid

        when (type) {
            PAYMENT_METHOD_CASHFREE -> {
                Log.e("bugs", " :: PAYMENT_METHOD_CASHFREE")
                doPayment(it.cftoken, it.orderid, it.Amountafterdiscount.toString(),
                    addressData.phone, addressData.email)
            }
            PAYMENT_METHOD_COD -> {
                val afterPaymentRawData = AfterPaymentRawData(1, paymentType,
                    addressData.token, addressData.userID, it.orderid,
                    addressData.amountafterdiscount, addressData.paymentType, it.msg?:"", 0)
                afterPaymentData.value = afterPaymentRawData
              /*  val paymentData = PaymentData(it.orderid, it.Amountafterdiscount, it.msg, 1)
                paymentLiveData.value = paymentData*/
            }
            else -> {
                val afterPaymentRawData = AfterPaymentRawData(1, paymentType,
                    addressData.token, addressData.userID, it.orderid, addressData.amountafterdiscount,
                    addressData.paymentType, it.msg?:"", 0)
                afterPaymentData.value = afterPaymentRawData
                val paymentData = PaymentData(it.orderid, it.Amountafterdiscount, it.msg, 1)
                paymentLiveData.value = paymentData
            }
        }
    }

    override fun onApiAfterWalletAddCall() {
        loadingDialog.showsDialog = true
    }

    override fun onSuccess(orderID: String, status: Int, amount: Float, txtMsg: String) {
       loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.walletAmount = amount.toString()
    }

    override fun onSuccessCFToken() {
        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onWalletFailure(orderID: String, status: Int, amount: Float, txtMsg: String) {
        loadingDialog.dismissAllowingStateLoss()
    }

    override fun walletAmount(amount: String) {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.walletAmount = amount
    }

    override fun onSuccesPayment(orderID: String, status: Int, amount: Float, txtMsg: String) {
        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = txtMsg
        successToast.show()
        val paymentData = PaymentData(orderID, amount, txtMsg, status)
        paymentLiveData.value = paymentData
    }

    override fun onFailurePayment(orderID: String, status: Int, amount: Float, txtMsg: String) {
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
}
