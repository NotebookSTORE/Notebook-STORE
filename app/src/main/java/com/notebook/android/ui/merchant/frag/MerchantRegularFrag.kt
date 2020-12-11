package com.notebook.android.ui.merchant.frag

import android.content.Context
import android.os.Bundle
import android.text.Html
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager

import com.notebook.android.R
import com.notebook.android.adapter.merchant.MerchantBenefitAdapter
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.MerchantBenefit
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentMerchantRegularBinding
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.merchant.MerchantViewModel
import com.notebook.android.ui.merchant.responseListener.MerchantBenefitListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MerchantRegularFrag : Fragment(), KodeinAware, MerchantBenefitListener {

    private lateinit var merchantRegularBinding:FragmentMerchantRegularBinding
    override val kodein by kodein()
    private val viewModelFactory: MerchantVMFactory by instance()
    private val merchantVM: MerchantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MerchantViewModel::class.java)
    }

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
        merchantVM.merchantBenefitData()
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        merchantRegularBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_merchant_regular, container, false)
        merchantRegularBinding.lifecycleOwner = this
        merchantVM.merchantBenefitListener = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            merchantRegularBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            merchantRegularBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)
        return merchantRegularBinding.root
    }

    private fun setupRecyclerView(merchantBenefitData:List<MerchantBenefit>){
        val layoutManagerMerchant = LinearLayoutManager(mContext)
        val merchantBenefitAdapter = MerchantBenefitAdapter(mContext, merchantBenefitData)
        merchantRegularBinding.recViewMerchantRegularBenefits.apply {
            layoutManager = layoutManagerMerchant
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
            adapter = merchantBenefitAdapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        merchantVM.getMerchantBenefitData(Constant.MERCHANT_REGULAR_BENEFIT)
            .observe(viewLifecycleOwner, Observer {
                if (it != null){
                    val merchBenefitFirst = it[0]
                    val merchBenefitLast = it[it.size-1]
                    merchantRegularBinding.tvBenefitRegularMerchant.text = Html.fromHtml(merchBenefitFirst.title).toString()
                    merchantRegularBinding.tvPrimeMerchantDesc.text = Html.fromHtml(merchBenefitFirst.description).toString()
                    merchantRegularBinding.tvWhySellOnNotebook.text = Html.fromHtml(merchBenefitLast.title).toString()
                    merchantRegularBinding.tvWhySellOnNotebookDesc.text = Html.fromHtml(merchBenefitLast.description).toString()
                    val merchPrimeBenefitList: ArrayList<MerchantBenefit> = it as ArrayList<MerchantBenefit>
                    merchPrimeBenefitList.removeAt(0)
                    merchPrimeBenefitList.removeAt(it.size-1)
                    setupRecyclerView(merchPrimeBenefitList)
                }
            })
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessResponse(successMsg: String, primeSubscriptionCharge: String) {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.primeSubscriptionCharge = primeSubscriptionCharge
    }

    override fun onSuccessBannerResponse(bannerresponse: List<Banner>?) {

    }

    override fun onSuccessDefaultAddress(defaultAddr: String) {

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
