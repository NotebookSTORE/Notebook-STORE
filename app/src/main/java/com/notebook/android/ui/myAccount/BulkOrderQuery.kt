package com.notebook.android.ui.myAccount

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail

import com.notebook.android.R
import com.notebook.android.databinding.FragmentBulkOrderQueryBinding
import com.notebook.android.ui.popupDialogFrag.ConfirmationDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.listener.RateProdListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class BulkOrderQuery : Fragment(), KodeinAware, View.OnClickListener, RateProdListener,
    ConfirmationDialog.ConfirmDialogDismiss {

    private lateinit var bulkOrderBinding:FragmentBulkOrderQueryBinding
    override val kodein by kodein()
    private val viewModelFactory : DetailProductVMFactory by instance<DetailProductVMFactory>()
    private val detailVM: DetailProductVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(DetailProductVM::class.java)
    }
    private lateinit var navController: NavController

    private lateinit var mActivity:FragmentActivity
    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bulkOrderBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_bulk_order_query, container, false)
        bulkOrderBinding.lifecycleOwner = this
        detailVM.rateProdListener = this
        return bulkOrderBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        bulkOrderBinding.btnSubmitBulkQuery.setOnClickListener(this)
    }

    private fun showErrorView(msg:String){
        bulkOrderBinding.clErrorView.visibility = View.VISIBLE
        bulkOrderBinding.tvErrorText.text = msg
        bulkOrderBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            bulkOrderBinding.clErrorView.visibility = View.GONE
            bulkOrderBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onClick(p0: View?) {
        when(p0){

            bulkOrderBinding.btnSubmitBulkQuery -> {
                val name = bulkOrderBinding.edtName.text.toString()
                val email = bulkOrderBinding.edtEmail.text.toString()
                val prodName = bulkOrderBinding.edtProduct.text.toString()
                val phone = bulkOrderBinding.edtPhone.text.toString()
                val qty = bulkOrderBinding.edtQuantity.text.toString()

                if(TextUtils.isEmpty(prodName)){
                    showErrorView("Enter product name")
                }else if(TextUtils.isEmpty(qty)){
                    showErrorView("Enter product quantity")
                }else if(TextUtils.isEmpty(name)){
                    showErrorView("Enter your name")
                }else if(TextUtils.isEmpty(email)){
                    showErrorView("Enter your email address")
                }else if(!validateEmail(email)){
                    showErrorView("Enter valid email address")
                }else if(TextUtils.isEmpty(phone)){
                    showErrorView("Enter your mobile number")
                }else if(phone.length<10){
                    showErrorView("Enter valid mobile number")
                }else{
                    detailVM.bulkEnquiryProduct(name, phone, prodName, email, qty.toInt())
                }
            }
        }
    }

    override fun onApiCallStarted() {
        bulkOrderBinding.pbBulkEnquiry.visibility = View.VISIBLE
        bulkOrderBinding.btnSubmitBulkQuery.visibility = View.GONE
    }

    override fun onSuccess(successMsg: String) {
        bulkOrderBinding.pbBulkEnquiry.visibility = View.GONE
        bulkOrderBinding.btnSubmitBulkQuery.visibility = View.VISIBLE

        val confirmDialog = ConfirmationDialog()
        confirmDialog.isCancelable = false
        confirmDialog.setDialogListener(this)
        val bundle = Bundle()
        bundle.putString("toastMsg", successMsg)
        confirmDialog.arguments = bundle
        confirmDialog.show(mActivity.supportFragmentManager, "Custom Toast Popup !!")
    }

    override fun onFailure(msg: String) {
        showErrorView(msg)
        bulkOrderBinding.pbBulkEnquiry.visibility = View.GONE
        bulkOrderBinding.btnSubmitBulkQuery.visibility = View.VISIBLE
    }

    override fun onApiFailure(msg: String) {

    }

    override fun onInvalidCredential() {
//        loadingDialog.dismissAllowingStateLoss()
        detailVM.deleteUser()
        detailVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onNoInternetAvailable(msg: String) {
        TODO("Not yet implemented")
    }

    override fun ondismissed() {
        navController.popBackStack()
    }

}
