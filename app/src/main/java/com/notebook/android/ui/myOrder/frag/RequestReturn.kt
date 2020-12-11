package com.notebook.android.ui.myOrder.frag

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.OrderHistory
import com.notebook.android.data.db.entities.User
import com.notebook.android.databinding.FragmentRequestReturnBinding
import com.notebook.android.ui.myOrder.MyOrderVM
import com.notebook.android.ui.myOrder.MyOrderVMFactory
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.myOrder.listener.OrderHistoryListener
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.RequestReturnDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class RequestReturn : Fragment(), KodeinAware, View.OnClickListener,
    RequestReturnDialog.RequestReturnListener, OrderHistoryListener {

    override val kodein by kodein()
    private val viewModelFactory : MyOrderVMFactory by instance()
    private val myOrderVM: MyOrderVM by lazy{
        ViewModelProvider(this, viewModelFactory).get(MyOrderVM::class.java)
    }
    private lateinit var fragmentReturnBinding: FragmentRequestReturnBinding
    private lateinit var navController: NavController
    private lateinit var orderHistoryData: OrderHistory

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private lateinit var userData: User
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

        if(arguments != null){
            val orderSummaryArgs = RequestReturnArgs.fromBundle(requireArguments())
            orderHistoryData = orderSummaryArgs.orderHistoryModal
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentReturnBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_request_return, container, false)
        myOrderVM.orderHistoryListener = this
        fragmentReturnBinding.lifecycleOwner = this

        fragmentReturnBinding.setVariable(BR.orderHistoryModel, orderHistoryData)
        fragmentReturnBinding.executePendingBindings()

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentReturnBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentReturnBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragmentReturnBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        myOrderVM.getUserData().observe(viewLifecycleOwner, Observer {
            if(it != null){
                userData = it
            }
        })

        fragmentReturnBinding.btnContinueRequest.setOnClickListener(this)
        fragmentReturnBinding.edtReasonforReturn.setOnClickListener(this)
    }

    fun showErrorView(msg: String) {
        fragmentReturnBinding.clErrorView.visibility = View.VISIBLE
        fragmentReturnBinding.tvErrorText.text = msg
        fragmentReturnBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentReturnBinding.clErrorView.visibility = View.GONE
            fragmentReturnBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragmentReturnBinding.btnContinueRequest -> {
                val reason = fragmentReturnBinding.edtReasonforReturn.text.toString()
                if(TextUtils.isEmpty(reason)){
                    showErrorView("Please fill request for return")
                }else{
                    if (orderHistoryData.delivered_date.isNullOrEmpty()){
                        errorToastTextView.text = "Please update your delivered date"
                        errorToast.show()
                    }else{
                        myOrderVM.returnOrderPolicy(userData.id, userData.token!!,
                            orderHistoryData.orderId, orderHistoryData.cartproduct_id!!,reason,
                            orderHistoryData.delivered_date?:"")
                    }

                }
            }

            fragmentReturnBinding.edtReasonforReturn -> {
                val returnDialog = RequestReturnDialog()
                returnDialog.setRequestReturnListener(this)
                returnDialog.show(mActivity.supportFragmentManager, "Return Dialog")
            }
        }
    }

    override fun onGetReason(reason: String) {
        fragmentReturnBinding.edtReasonforReturn.setText(reason)
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessResponse(orderHistory: List<OrderHistory>) {
    }

    override fun onSuccessCancelReturn(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = msg
        successToast.show()
        navController.popBackStack(R.id.orderFrag, false)
    }

    override fun onFailureResponse(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailureResponse(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInvalidCredential() {
        myOrderVM.deleteUser()
        myOrderVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onInternetNotAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }
}