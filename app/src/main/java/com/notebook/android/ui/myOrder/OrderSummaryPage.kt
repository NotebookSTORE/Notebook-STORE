package com.notebook.android.ui.myOrder

import android.content.Context
import android.os.Bundle
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
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.OrderHistory
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentOrderSummaryPageBinding
import com.notebook.android.ui.myOrder.listener.OrderHistoryListener
import com.notebook.android.ui.popupDialogFrag.CancelOrderDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.Constant.ORDER_STATUS_CONFIRM
import com.notebook.android.utility.orderExpectedDate
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class OrderSummaryPage : Fragment(), KodeinAware, OrderHistoryListener, View.OnClickListener,
    CancelOrderDialog.CancelOrderReasonListener {
    private lateinit var fragOrderSummaryBinding: FragmentOrderSummaryPageBinding
    private lateinit var orderHistoryData: OrderHistory
    private lateinit var navController: NavController

    companion object {
        val GRAVITY_BOTTOM = 80
        val GRAVITY_CENTER = 17
    }

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    override val kodein by kodein()
    private val viewModelFactory: MyOrderVMFactory by instance()
    private val myOrderVM: MyOrderVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(MyOrderVM::class.java)
    }
    private val loadingDialog: LoadingDialog by lazy {
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragOrderSummaryBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_order_summary_page, container, false
        )
        fragOrderSummaryBinding.lifecycleOwner = this
        myOrderVM.orderHistoryListener = this

        //success toast layout initialization here....
        val successToastLayout: View = inflater.inflate(
            R.layout.custom_toast_layout,
            fragOrderSummaryBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        successToastTextView =
            (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout: View = inflater.inflate(
            R.layout.error_custom_toast_layout,
            fragOrderSummaryBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView =
            (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(GRAVITY_BOTTOM, 0, 80)

        val orderArgs = OrderSummaryPageArgs.fromBundle(requireArguments())
        orderHistoryData = orderArgs.orderHistoryData
        fragOrderSummaryBinding.setVariable(BR.orderSummaryModel, orderHistoryData)
        fragOrderSummaryBinding.executePendingBindings()
        return fragOrderSummaryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        myOrderVM.getUserData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                userData = it
            }
        })

        if (orderHistoryData.cancel_status == 1 && orderHistoryData.return_status == 0) {
            fragOrderSummaryBinding.clOrderDataFlowLayout.visibility = View.GONE
            fragOrderSummaryBinding.clOrderAdminMessage.visibility = View.VISIBLE
            fragOrderSummaryBinding.clRequestReturnLayout.visibility = View.VISIBLE
            fragOrderSummaryBinding.tvRequestReturnCancel.text = "Cancelled"
            fragOrderSummaryBinding.tvAdminMessage.text =
                "Reason  :  ${orderHistoryData.can_reason}"
        } else if (orderHistoryData.cancel_status == 0 && orderHistoryData.return_status == 1) {
            fragOrderSummaryBinding.clOrderDataFlowLayout.visibility = View.GONE
            fragOrderSummaryBinding.clOrderAdminMessage.visibility = View.VISIBLE
            fragOrderSummaryBinding.clRequestReturnLayout.visibility = View.VISIBLE
            fragOrderSummaryBinding.tvRequestReturnCancel.text = "Returned"
            fragOrderSummaryBinding.tvAdminMessage.text =
                "Reason  :  ${orderHistoryData.ret_reason}"
        } else {
            fragOrderSummaryBinding.clOrderDataFlowLayout.visibility = View.VISIBLE
            fragOrderSummaryBinding.clOrderAdminMessage.visibility = View.GONE

            if (!orderHistoryData.expected_date.isNullOrBlank()) {
                fragOrderSummaryBinding.clRequestReturnLayout.visibility = View.VISIBLE
                fragOrderSummaryBinding.tvRequestReturnCancel.text = "Cancel"

                fragOrderSummaryBinding.tvDeliveredText.text = "Expected delivery on"
                orderExpectedDate(fragOrderSummaryBinding.tvDeliveredDate, orderHistoryData.expected_date)
            }
            if (!orderHistoryData.delivered_date.isNullOrBlank()) {

                fragOrderSummaryBinding.tvDeliveredSuccessText.visibility = View.VISIBLE
                fragOrderSummaryBinding.tvDeliveredSuccessDate.visibility = View.VISIBLE
                fragOrderSummaryBinding.progressView2.visibility = View.VISIBLE

                fragOrderSummaryBinding.clRequestReturnLayout.visibility = View.VISIBLE
                fragOrderSummaryBinding.tvRequestReturnCancel.text = "Return"

                orderExpectedDate(fragOrderSummaryBinding.tvDeliveredSuccessDate, orderHistoryData.delivered_date)
            }
        }

        if (!orderHistoryData.return_date.isNullOrEmpty()) {
            try {
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val str1 = orderHistoryData.return_date
                val date1: Date = formatter.parse(str1!!)!!
                val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                val date2 = formatter.parse(currentDate)
                fragOrderSummaryBinding.tvRequestReturnCancel.isEnabled = date1 >= date2
            } catch (e1: ParseException) {
                e1.printStackTrace()
            }
        }
        fragOrderSummaryBinding.tvRequestReturnCancel.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            fragOrderSummaryBinding.tvRequestReturnCancel -> {
                val returnText = fragOrderSummaryBinding.tvRequestReturnCancel.text.toString()
                if (returnText.equals("cancel", true)) {
                    val cancelOrderDialog = CancelOrderDialog()
                    cancelOrderDialog.isCancelable = false
                    cancelOrderDialog.setCancelOrderListener(this@OrderSummaryPage)
                    cancelOrderDialog.show(
                        mActivity.supportFragmentManager,
                        "Cancel Order Dialog Show"
                    )
                } else if (returnText.equals("return", true)) {
                    val myOrderDetailDirections: OrderSummaryPageDirections.ActionOrderSummaryPageToRequestReturn =
                        OrderSummaryPageDirections.actionOrderSummaryPageToRequestReturn(
                            orderHistoryData
                        )
                    navController.navigate(myOrderDetailDirections)
                }
            }
        }
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
        navController.popBackStack()
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

    override fun onInternetNotAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }


    override fun onInvalidCredential() {
        notebookPrefs.clearPreference()
        myOrderVM.deleteUser()
        myOrderVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onGetReason(reason: String) {
        myOrderVM.cancelOrderPolicy(
            userData.id, userData.token!!,
            orderHistoryData.orderId, orderHistoryData.cartproduct_id!!, reason
        )
    }
}