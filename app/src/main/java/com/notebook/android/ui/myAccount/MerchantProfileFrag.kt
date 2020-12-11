package com.notebook.android.ui.myAccount

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation

import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.databinding.FragmentMerchantProfileBinding
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.merchant.MerchantViewModel
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MerchantProfileFrag : Fragment(), View.OnClickListener, KodeinAware,
    UserLogoutDialog.UserLoginPopupListener {

    private lateinit var fragMerchantProfileBinding:FragmentMerchantProfileBinding
    private lateinit var navController: NavController

    override val kodein by kodein()
    private val viewModelFactory: MerchantVMFactory by instance()
    private val merchantVM: MerchantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MerchantViewModel::class.java)
    }
    private var userData: User?= null

    private lateinit var mActivity: FragmentActivity
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
        fragMerchantProfileBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_merchant_profile, container, false)

        return fragMerchantProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        merchantVM.getUserData().observe(viewLifecycleOwner, Observer {
            if(it != null){
                userData = it
                if(userData!!.name.isNullOrEmpty()){
                    fragMerchantProfileBinding.tvUserNameView.text = userData!!.username?:"User Name"
                }else{
                    fragMerchantProfileBinding.tvUserNameView.text = userData!!.name?:"User Name"
                }
                fragMerchantProfileBinding.tvSponserDataView.text = "${userData!!.referralcode}"
                fragMerchantProfileBinding.tvMerchantDataView.text = userData!!.merchant_id
                if(it.wallet_amounts?.isNotEmpty() == true){
                    fragMerchantProfileBinding.tvWalletAmountValueView.text = "₹ ${userData!!.wallet_amounts}/-"
                    fragMerchantProfileBinding.tvCommissionAmountView.text = "₹ ${userData!!.wallet_amounts}/-"
                }else{
                    fragMerchantProfileBinding.tvWalletAmountValueView.text = "₹ 0/-"
                    fragMerchantProfileBinding.tvCommissionAmountView.text = "₹ 0/-"
                }
            }else{
                userData = null
            }
        })

        fragMerchantProfileBinding.btnViewSummary.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){

            fragMerchantProfileBinding.btnViewSummary -> {
                if (userData != null){
                    Log.e("userID", " :: ${userData!!.id}")
                    val merchantProfileDirections:MerchantProfileFragDirections.ActionMerchantProfileFragToMerchantViewSummaryWebViewPage =
                        MerchantProfileFragDirections.actionMerchantProfileFragToMerchantViewSummaryWebViewPage(userData!!.id!!)
                    navController.navigate(merchantProfileDirections)
                }else{
                    val userLoginRequestPopup = UserLogoutDialog()
                    userLoginRequestPopup.isCancelable = false
                    userLoginRequestPopup.setUserLoginRequestListener(this)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }
            }
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

}
