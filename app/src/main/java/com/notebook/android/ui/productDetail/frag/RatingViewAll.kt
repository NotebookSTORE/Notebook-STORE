package com.notebook.android.ui.productDetail.frag

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.notebook.android.R
import com.notebook.android.adapter.DetailProduct.ReviewAllAdapter
import com.notebook.android.databinding.FragmentRatingViewAllBinding
import com.notebook.android.model.productDetail.RatingReviewData
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.listener.RatingViewAllListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class RatingViewAll : Fragment(), KodeinAware, RatingViewAllListener,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var fragRatingViewAllBinding:FragmentRatingViewAllBinding
    override val kodein by kodein()
    private val viewModelFactory : DetailProductVMFactory by instance()
    private val detailVM: DetailProductVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(DetailProductVM::class.java)
    }
    private var prodID:Int ?= null
    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        if(arguments != null){
            val ratingArgs = RatingViewAllArgs.fromBundle(requireArguments())
            prodID = ratingArgs.productID
            Log.e("prod id", " :: ${ratingArgs.productID}")
            detailVM.getRatingReviewsData(ratingArgs.productID.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragRatingViewAllBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_rating_view_all, container, false)
        fragRatingViewAllBinding.lifecycleOwner = this
        detailVM.ratingViewListener = this
        setupRecyclerView()

        fragRatingViewAllBinding.splRatingViewAll.
        setColorSchemeColors(
            ContextCompat.getColor(mContext, android.R.color.holo_green_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_red_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_blue_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_orange_dark))
        fragRatingViewAllBinding.splRatingViewAll.setOnRefreshListener(this)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragRatingViewAllBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragRatingViewAllBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragRatingViewAllBinding.root
    }

    override fun onApiCallStarted() {
        fragRatingViewAllBinding.splRatingViewAll.isRefreshing = true
    }

    private fun setupRecyclerView(){
        val layoutManagerRating = LinearLayoutManager(mContext)
        fragRatingViewAllBinding.recViewRatingReview.apply {
            layoutManager = layoutManagerRating
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    override fun onSuccess(it: RatingReviewData) {
        fragRatingViewAllBinding.splRatingViewAll.isRefreshing = false

        fragRatingViewAllBinding.clOverallRating.visibility = View.VISIBLE
        fragRatingViewAllBinding.tvRatingStarsText.text = it.average
        fragRatingViewAllBinding.tvRatingCounts.text = "${it.average} Ratings and ${it.ratingcount} Reviews"

        if(it.productrating?.isEmpty() == true){
            fragRatingViewAllBinding.clLottieAnimationView.visibility = View.VISIBLE
            fragRatingViewAllBinding.tvErrorMsg.text = "No data available !!"
        }else{
            fragRatingViewAllBinding.clLottieAnimationView.visibility = View.GONE
            val ratingAdapter = ReviewAllAdapter(mContext, it.productrating?:ArrayList())
            fragRatingViewAllBinding.recViewRatingReview.adapter = ratingAdapter
        }
    }

    override fun onFailure(msg: String) {
        fragRatingViewAllBinding.splRatingViewAll.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()

        fragRatingViewAllBinding.clLottieAnimationView.visibility = View.VISIBLE
        fragRatingViewAllBinding.tvErrorMsg.text = msg
        fragRatingViewAllBinding.lavDataNotAvailable.setAnimation(R.raw.error_lottie)
        fragRatingViewAllBinding.lavDataNotAvailable.playAnimation()
        fragRatingViewAllBinding.lavDataNotAvailable.loop(true)
    }

    override fun onApiFailure(msg: String) {
        fragRatingViewAllBinding.splRatingViewAll.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
        fragRatingViewAllBinding.splRatingViewAll.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onRefresh() {
        detailVM.getRatingReviewsData(prodID!!.toString())
    }
}
