package com.notebook.android.ui.orderSummary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson

import com.notebook.android.R
import com.notebook.android.adapter.order.ApplyCouponAdapter
import com.notebook.android.data.db.entities.CouponApply
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentApplyCouponBinding
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.CouponAlertDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ApplyCoupon : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val viewModelFactory : DetailProductVMFactory by instance<DetailProductVMFactory>()
    private val detailVM: DetailProductVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(DetailProductVM::class.java)
    }
    private lateinit var fragApplyCouponBinding:FragmentApplyCouponBinding
    private lateinit var navController:NavController

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity

    private var productID:String ?= null
    private var emailID:String ?= null
    private var totalAmount:Float ?= null
    private var userType:Int ?= null
    private var registerFor:Int ?= null

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        val couponArgs = ApplyCouponArgs.fromBundle(requireArguments())
        Log.e("PassingCouponValue", " :: ${couponArgs.emailID} :: " +
                "${couponArgs.totalPayableAmount} :: ${couponArgs.userType} " +
                ":: ${couponArgs.registerFor} :: ${couponArgs.productID}")
        emailID = couponArgs.emailID
        totalAmount = couponArgs.totalPayableAmount
        userType = couponArgs.userType
        registerFor = couponArgs.registerFor
        productID = couponArgs.productID
    }

    private val notebookPrefs: NotebookPrefs by lazy{
        NotebookPrefs(mContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragApplyCouponBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_apply_coupon, container, false)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragApplyCouponBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragApplyCouponBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        notebookPrefs.orderSummaryCoupon = true
        return fragApplyCouponBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        detailVM.getAllCouponDataFromDB().observe(viewLifecycleOwner, Observer {
            setupRecyclerView(it)
        })
    }

    private fun setupRecyclerView(list:List<CouponApply>){
        val layoutManagerApplyCoupon = LinearLayoutManager(mContext)
        val couponAdapter = ApplyCouponAdapter(mContext, productID!!,  emailID!!,
            registerFor?:0, userType!!, totalAmount!!, list,
            object : ApplyCouponAdapter.ApplyCouponListener {
            override fun onApplyCoupon(couponData: CouponApply) {
                navController.previousBackStackEntry?.savedStateHandle?.set("applyCoupon", Gson().toJson(couponData))
                navController.popBackStack()
            }

            override fun errorMessage(msg: String) {

                val userLoginRequestPopup = CouponAlertDialog()
                userLoginRequestPopup.isCancelable = true
                val bundle = Bundle()
                bundle.putString("displayTitle", msg)
                userLoginRequestPopup.arguments = bundle
                userLoginRequestPopup.show(mActivity.supportFragmentManager, "Coupon error dialog !!")
//                errorToastTextView.text = msg
//                errorToast.show()
            }

            })
        fragApplyCouponBinding.recViewApplyCoupon.apply {
            layoutManager = layoutManagerApplyCoupon
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
            adapter = couponAdapter
        }
    }
}
