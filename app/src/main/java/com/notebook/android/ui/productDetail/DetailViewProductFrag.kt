package com.notebook.android.ui.dashboard.frag.fragHome

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.*
import androidx.transition.TransitionInflater
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.max.ecomaxgo.maxpe.view.flight.utility.formatStringDateToStandard
import com.max.ecomaxgo.maxpe.view.flight.utility.setProductRating
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.adapter.DetailProduct.CustomSpinnerAdpater
import com.notebook.android.adapter.DetailProduct.DetailProductAdapter
import com.notebook.android.adapter.DetailProduct.ProductBenefitAdapter
import com.notebook.android.adapter.DetailProduct.ProductCouponAdapter
import com.notebook.android.adapter.home.PagerAdapter.ProductImageSliderAdapter
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentDetailViewProductBinding
import com.notebook.android.model.filter.PaginationData
import com.notebook.android.model.home.FreeDeliveryData
import com.notebook.android.model.home.ProductCoupon
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.model.productDetail.RatingData
import com.notebook.android.ui.popupDialogFrag.ConfirmationDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.SharedVM
import com.notebook.android.ui.productDetail.listener.DiscountProdResponseListener
import com.notebook.android.utility.Constant
import kotlinx.android.synthetic.main.fragment_detail_view_product.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DetailViewProductFrag : Fragment(), KodeinAware,
    DiscountProdResponseListener,
    AdapterView.OnItemSelectedListener, View.OnClickListener,
    UserLogoutDialog.UserLoginPopupListener, ConfirmationDialog.ConfirmDialogDismiss {

    override val kodein by kodein()
    private val viewModelFactory: DetailProductVMFactory by instance()
    private lateinit var detailViewProductBinding: FragmentDetailViewProductBinding
    private val detailVM: DetailProductVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(DetailProductVM::class.java)
    }

    private lateinit var navController: NavController
    private lateinit var prodModel: ProductDetailEntity
    private var prodID: String? = null
    private var productDiscount: Int = 0
    private var prodQty: Int? = null
    private var qtyList: ArrayList<Int>? = null

    private var pageData = PaginationData()

    private val sharedVM: SharedVM by lazy {
        ViewModelProvider(mActivity).get(SharedVM::class.java)
    }

    /* private val loadingDialog: LoadingDialog by lazy{
         LoadingDialog()
     }*/

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var myToast: Toast
    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    //    private var isSimilarDiscountedViewAllClicked = false
    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        val args = DetailViewProductFragArgs.fromBundle(requireArguments())
        val product: Product = args.productHome
        prodID = args.productHome.id!!.toString()
        productDiscount = args.productHome.discount!!
        detailVM.clearProductTable()
        detailVM.getProductDetailData(prodID.toString())
        loadSimilarDiscountedPaginatedData(true)
        detailVM.getRatingSingleData(prodID!!)
        detailVM.getProductCouponData(prodID!!)
    }

    companion object {
        var productPrice: Float? = null
        var userData: User? = null
        private const val TAG = "DetailViewProductFrag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onRestoreInstanceState(savedInstanceState)
        // Inflate the layout for this fragment
        detailViewProductBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail_view_product, container, false
        )
        detailViewProductBinding.lifecycleOwner = this
        detailVM.discProdListener = this
        isFavouriteAdded = true

        checkDelivery(0, "")
        //custom toast initialize view here....
        val layouttoast = inflater.inflate(
            R.layout.custom_toast_layout,
            detailViewProductBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).setText("Item added successfully !!")

//        layouttoast.findViewById(R.id.imagetoast)).setBackgroundResource(R.drawable.icon);
        val GRAVITY_BOTTOM = 80
//        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_BOTTOM, 0, 0)

        //success toast layout initialization here....
        val successToastLayout: View = inflater.inflate(
            R.layout.custom_toast_layout,
            detailViewProductBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
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
            detailViewProductBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView =
            (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(GRAVITY_BOTTOM, 0, 80)

        setTextClickable()
        setupRecyclerView()

        sharedElementEnterTransition =
            TransitionInflater.from(mContext).inflateTransition(android.R.transition.move)
        Log.e("prodID", " :: $prodID")
        return detailViewProductBinding.root
    }

    private var notDeliverable = false
    private fun checkDelivery(i: Int, date: String) {
        notDeliverable = false
        if (i == 0) {
            detailViewProductBinding.tvDeliveryBy.text = "Please Check Pincode"
            detailViewProductBinding.tvDeliveryByDate.visibility = View.GONE
        } else if (i == 1) {
            detailViewProductBinding.tvDeliveryBy.text = getString(R.string.strDeliveryBy)
            detailViewProductBinding.tvDeliveryByDate.visibility = View.VISIBLE
            detailViewProductBinding.tvDeliveryByDate.text = date
        } else if (i == 2) {
            detailViewProductBinding.tvDeliveryBy.text = "Delivery Not Available"
            detailViewProductBinding.tvDeliveryByDate.visibility = View.GONE
            notDeliverable = true
        }
    }

    private fun setQuantityList(qty: Int): ArrayList<Int> {
        val qtyArray = ArrayList<Int>()
        for (i in 0 until qty) {
            qtyArray.add(i + 1)
        }
        return qtyArray
    }

    private val spanStartFrom = 31
    private fun setTextClickable() {
        val ssText = SpannableString(resources.getString(R.string.strBulkQueryText))
        ssText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
            spanStartFrom, ssText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssText.setSpan(
            StyleSpan(Typeface.BOLD),
            spanStartFrom,
            ssText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val spanRegNow = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                navController.navigate(R.id.action_detailViewProductFrag_to_bulkOrderQuery)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssText.setSpan(spanRegNow, spanStartFrom, ssText.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        detailViewProductBinding.tvQueryBulkOrder.movementMethod = LinkMovementMethod.getInstance()
        detailViewProductBinding.tvQueryBulkOrder.text = ssText

        //See more text underlines with clickable  tvSeeMore
        val ssMoreText = SpannableString(resources.getString(R.string.strSeeMore))
        ssMoreText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
            0, ssMoreText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssMoreText.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            ssMoreText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val spanSeeMore = object : ClickableSpan() {
            override fun onClick(widget: View) {
//                val navController = Navigation.findNavController(widget)
//                navController.navigate(R.id.action_detailViewProductFrag_to_bulkOrderQuery)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssMoreText.setSpan(spanSeeMore, 0, ssMoreText.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        detailViewProductBinding.tvSeeMore.movementMethod = LinkMovementMethod.getInstance()
        detailViewProductBinding.tvSeeMore.text = ssMoreText
    }

    private fun setupRecyclerView() {
        val layoutManagerSimilarProducts =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        detailViewProductBinding.recViewSimilarDiscntProds.apply {
            layoutManager = layoutManagerSimilarProducts
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()


            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    Log.d(
                        this@DetailViewProductFrag.tag,
                        "onScrolled() called with: recyclerView = $recyclerView, dx = $dx, dy = $dy"
                    )
                    Log.d(
                        TAG,
                        "onScrolled: last visible item: ${layoutManagerSimilarProducts.findLastVisibleItemPosition()}"
                    )
                    Log.d(TAG, "onScrolled: item count: ${layoutManagerSimilarProducts.itemCount}")

                    if (layoutManagerSimilarProducts.findLastVisibleItemPosition() == layoutManagerSimilarProducts.itemCount - 2) {
                        Log.d(TAG, "onScrolled: loading state:${isLoading}")
                        if (!isLoading) {
                            loadSimilarDiscountedPaginatedData()
                        }
                    }

                }
            })
        }

        //Setup recycler view for product coupons...
        val layoutManagerProdCoupon = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        detailViewProductBinding.recProdCoupon.apply {
            layoutManager = layoutManagerProdCoupon
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }

        val layoutManagerProdBenefits = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        detailViewProductBinding.recViewProdBenefits.apply {
            layoutManager = layoutManagerProdBenefits
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }


    private var isLoading: Boolean = false
    private fun loadSimilarDiscountedPaginatedData(refresh: Boolean = false) {
        Log.d(TAG, "loadSimilarDiscountedPaginatedData() called")

        isLoading = true
        if (refresh) {
            detailVM.getDiscProductUsingDiscontValue(productDiscount, 1)
        } else {
            pageData.next_page_url?.let {
                detailVM.getDiscProductUsingDiscontValue(
                    productDiscount,
                    it.substringAfterLast("=").toInt()
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        detailVM.getProductDetailLiveData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                prodModel = it
                detailViewProductBinding.setVariable(BR.productModel, it)
                detailViewProductBinding.executePendingBindings()

                val dataSheet = it.data_sheet
                if (dataSheet == null) {
                    clDataSheet.visibility = View.GONE
                    View2.visibility = View.GONE
                } else {
                    clDataSheet.visibility = View.VISIBLE
                    View2.visibility = View.VISIBLE
                }

                if (prodModel.reviewCount == 0) {
                    detailViewProductBinding.tvRatingStarText.text = "${4.0}"
                    detailViewProductBinding.tvRatingAmount.text = "(1 Review)"
                } else if (prodModel.reviewCount != null) {
                    detailViewProductBinding.tvRatingStarText.text = "${prodModel.customerRating}"
                    detailViewProductBinding.tvRatingAmount.text =
                        "(${prodModel.reviewCount} Reviews)"
                }

                if (prodModel.quantity <= 0) {
                    prodQty = 0
                    detailViewProductBinding.clProductQty.visibility = View.GONE
                    detailViewProductBinding.tvInStock.text = "Out of Stock"
                    detailViewProductBinding.tvInStock.setTextColor(mContext.resources.getColor(R.color.colorAccent))
                    val result = ((prodModel.price).times(prodModel.discount)).div(100f)
                    productPrice = (prodModel.price.minus(result)).times(prodQty!!)
                } else {
                    prodQty = 1
                    detailViewProductBinding.clProductQty.visibility = View.VISIBLE
                    detailViewProductBinding.tvInStock.text = "Stock Available"
                    detailViewProductBinding.tvInStock.setTextColor(Color.parseColor("#259D31"))
                    qtyList = setQuantityList(prodModel.quantity)
                    val qtyAdapter = CustomSpinnerAdpater(mContext, qtyList!!)
                    detailViewProductBinding.spProductQuantity.adapter = qtyAdapter
                    detailViewProductBinding.spProductQuantity.onItemSelectedListener =
                        this@DetailViewProductFrag
                    val result = ((prodModel.price).times(prodModel.discount)).div(100f)
                    productPrice = (prodModel.price.minus(result)).times(prodQty!!)
                }


                if (prodQty == 0) {
                    detailViewProductBinding.btnAddToCard.isEnabled = false
                    detailViewProductBinding.btnBuyNow.isEnabled = false
                    detailViewProductBinding.btnCheckPincodeNow.isEnabled = false
                } else {
                    detailViewProductBinding.btnAddToCard.isEnabled = true
                    detailViewProductBinding.btnBuyNow.isEnabled = true
                    detailViewProductBinding.btnCheckPincodeNow.isEnabled = true
                }
                prodID = prodModel.id!!.toString()
                /* detailVM.getRatingSingleData(prodID!!)
                 detailVM.getProductCouponData(prodID!!)
                 detailVM.getDiscProductUsingDiscontValue(prodModel.discount)
 */
                if (prodModel.can_cashon?.toInt() == 1) {
                    detailViewProductBinding.tvCOD.text = "Cash\non delivery"
                } else {
                    detailViewProductBinding.tvCOD.text = "COD\nnot available"
                }

                if (prodModel.can_free_delivery?.toInt() == 1) {
                    detailVM.getFreeDeliveryData()
                    detailViewProductBinding.recViewProdBenefits.visibility = View.VISIBLE
                } else {
                    sharedVM.setFreeDeliveryData(0)
                    prodModel.delivery_charges?.let {
                        showDeliverCharges(it)
                    }
                }

                if (prodModel.can_return?.toInt() == 1) {
                    detailViewProductBinding.tvReplacement.text =
                        "${prodModel.return_days ?: 0} days\nreplacement"
                } else {
                    detailViewProductBinding.tvReplacement.text = "Non\nreturnable"
                }

                val prodimageArray = stringToProductImageList(prodModel)

                Log.e("prodImageArray", " :: $prodimageArray")
                if (prodimageArray.isNullOrEmpty()) {
                    detailViewProductBinding.clImageSliderContainer.visibility = View.GONE
                    detailViewProductBinding.imgProduct.visibility = View.VISIBLE
                } else {
                    detailViewProductBinding.clImageSliderContainer.visibility = View.VISIBLE
                    detailViewProductBinding.imgProduct.visibility = View.GONE

                    val sliderAdapter = ProductImageSliderAdapter(mContext,
                        prodimageArray as ArrayList<ProductDetailData.ProductImageData>,
                        object : ProductImageSliderAdapter.ProductImageSliderListener {
                            override fun onSliderClick(offerUrl: Array<String>,position: Int) {
                                val detailViewProductFragDirections: DetailViewProductFragDirections.ActionDetailViewProductFragToZoomableViewFrag =
                                    DetailViewProductFragDirections.actionDetailViewProductFragToZoomableViewFrag(
                                        offerUrl,position
                                    )
                                Log.e("offer web link", " :: $offerUrl")
                                navController.navigate(detailViewProductFragDirections)
                            }
                        })
                    detailViewProductBinding.vpProductImageSlider.adapter = sliderAdapter
                    detailViewProductBinding.tlProductImageSliderIndicator.setupWithViewPager(
                        detailViewProductBinding.vpProductImageSlider
                    )
                }
                detailViewProductBinding.nsvProductDetail.scrollTo(0, 0)
            }
        })


        val discountedProductList = ArrayList<DiscountedProduct>()
        detailVM.getPageData.observe(viewLifecycleOwner, {
            pageData = it
        })

        detailVM.getAllDiscountProdFromDB().observe(viewLifecycleOwner, {

            if (!isLoading) {
                return@observe
            }
            isLoading = false

            if (it != null && it.isNotEmpty()) {

                discountedProductList.addAll(it)

                detailViewProductBinding.clSimilarProducts.visibility = View.VISIBLE
                val discProductAdapter = DetailProductAdapter(mContext, discountedProductList,
                    object : DetailProductAdapter.discountProductListener {
                        override fun buyDiscountedProducts(
                            discProd: DiscountedProduct,
                            prodQty: Int,
                            prodPrice: Float
                        ) {
                            if (userData == null) {
                                showLoginPopup()
                                return
                            }

                            val prodList = ArrayList<OrderSummaryProduct>()
                            prodList.add(
                                OrderSummaryProduct(
                                    discProd.id,
                                    prodQty,
                                    prodPrice,
                                    discProd.keyfeature,
                                    discProd.material,
                                    discProd.title,
                                    discProd.alias,
                                    discProd.image,
                                    discProd.status,
                                    discProd.short_description,
                                    discProd.description,
                                    discProd.data_sheet,
                                    discProd.quantity,
                                    discProd.price,
                                    discProd.offer_price,
                                    discProd.product_code,
                                    discProd.product_condition,
                                    discProd.discount,
                                    discProd.latest,
                                    discProd.best,
                                    discProd.brandtitle,
                                    discProd.colortitle,
                                    discProd.delivery_charges,
                                    1,
                                    discProd.can_free_delivery
                                )
                            )
                            sharedVM.setCodOptionForPayment(discProd.can_cashon?.toInt() ?: 0)
                            sharedVM.setDeliveryCharge(discProd.delivery_charges)
                            sharedVM.setProductOrderSummaryList(prodList)
                            navController.navigate(R.id.action_detailViewProductFrag_to_orderSummary)
                        }

                        override fun addToCartItem(prodID: String, prodQty: Int) {
                            if (userData != null) {
                                detailVM.addItemsToCart(
                                    userData!!.id, userData!!.token!!,
                                    prodID, prodQty, 0
                                )
                            } else {
                                showLoginPopup()
                            }
                        }

                        override fun showProdDetailOnClickDiscountedProd(discProd: DiscountedProduct) {
                            prodID = discProd.id!!.toString()
                            detailVM.clearProductTable()
                            detailVM.getProductDetailData(prodID!!)
                        }
                    })
                detailViewProductBinding.recViewSimilarDiscntProds.adapter = discProductAdapter

            } else {
                detailViewProductBinding.clSimilarProducts.visibility = View.GONE
            }
        })

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("prodData")
            ?.observe(
                viewLifecycleOwner, Observer {
                    prodModel = Gson().fromJson(it, ProductDetailEntity::class.java)
                    Log.e("prodTitle", " :: ${prodModel.title}")
                    prodID = prodModel.id!!.toString()
                    detailVM.clearProductTable()
                    detailVM.getProductDetailData(prodID!!)
                })

        detailVM.getUserData().observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                userData = user
            } else {
                userData = null
            }
        })

        detailViewProductBinding.tvSimilarDiscntProdsViewAll.setOnClickListener(this)
        detailViewProductBinding.tvSeeMore.setOnClickListener(this)
        detailViewProductBinding.btnAddToCard.setOnClickListener(this)
        detailViewProductBinding.tvRatingViewAll.setOnClickListener(this)
        detailViewProductBinding.tvRateProduct.setOnClickListener(this)
        detailViewProductBinding.cvAddToFav.setOnClickListener(this)
        detailViewProductBinding.btnBuyNow.setOnClickListener(this)
        detailViewProductBinding.btnCheckPincodeNow.setOnClickListener(this)
        detailViewProductBinding.imgShareProduct.setOnClickListener(this)
        detailViewProductBinding.wvDataSheet.setOnLongClickListener(View.OnLongClickListener {
            return@OnLongClickListener true
        })
        detailViewProductBinding.wvDataSheet.isLongClickable = false

        loadSimilarDiscountedPaginatedData(true)
    }

    private fun stringToProductImageList(product: ProductDetailEntity): List<ProductDetailData.ProductImageData> {
        val data = product.prodImageListString
        if ((data.isNullOrBlank() || data == "[]") && product.image.isNullOrEmpty()) {
            return Collections.emptyList()
        }

        if ((data.isNullOrBlank() || data == "[]")) {
            return mutableListOf(
                ProductDetailData.ProductImageData(
                    id = 0,
                    product_id = product.id ?: 0,
                    title = product.title ?: "",
                    image = product.image,
                    status = 0
                )
            )
        }

        val listType: Type = object : TypeToken<List<ProductDetailData.ProductImageData>>() {}.type
        return Gson().fromJson(data, listType)
    }

    override fun onApiCallStarted() {
        /*if(mActivity.supportFragmentManager.findFragmentByTag("Show loading dialog") == null){
            loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
        }else{
            loadingDialog.requireDialog().show()
        }*/
    }

    override fun onSuccessProductData(prodData: ProductDetailData) {
        detailViewProductBinding.nsvProductDetail.visibility = View.VISIBLE
//        loadingDialog.dialog?.dismiss()
    }

    override fun onSuccess(prod: List<DiscountedProduct>?) {
//        loadingDialog.dialog?.dismiss()
    }

    override fun onCouponDataSuccess(couponProd: List<ProductCoupon.ProdCoupon>) {
//        loadingDialog.dialog?.dismiss()
        activity?.let {
            val couponProdAdapter = ProductCouponAdapter(it,
                couponProd as java.util.ArrayList<ProductCoupon.ProdCoupon>,
                object : ProductCouponAdapter.ProductCouponListener {
                    override fun prodCouponObj(coupon: ProductCoupon.ProdCoupon) {
                        Log.e("couponData", " :: ${coupon.code} :: ${coupon.description}")
//                        sharedVM.setCouponData(coupon)
                    }

                    override fun upgradeToPrimeMerchant(msg: String) {
                        errorToastTextView.text = msg
                        errorToast.show()
                    }

                    override fun couponForLogin(msg: String) {
                        showLoginPopup()
                    }
                })
            detailViewProductBinding.recProdCoupon.adapter = couponProdAdapter
        }
    }

    override fun onSuccessFreeDeliveryData(freeDeliveryData: List<FreeDeliveryData.FreeDelivery>) {
        val benefitAdapter = ProductBenefitAdapter(
            mContext,
            freeDeliveryData as ArrayList<FreeDeliveryData.FreeDelivery>
        )
        detailViewProductBinding.recViewProdBenefits.adapter = benefitAdapter

        for (freeDelObj in freeDeliveryData.indices) {
            if (freeDeliveryData[freeDelObj].title.contains("free delivery", true)) {
                sharedVM.setFreeDeliveryData(freeDeliveryData[freeDelObj].price)
                Log.e("freeDeliveryAmount", " :: ${freeDeliveryData[freeDelObj].price}")
//                prodModel.delivery_charges
            }
        }
    }

    fun showDeliverCharges(delivery_charges: Float) {
        val deliveryCharges = FreeDeliveryData.FreeDelivery(
            "Applicable delivery charges",
            delivery_charges.roundToInt()
        )

        val deliveryChargesList = arrayListOf<FreeDeliveryData.FreeDelivery>()
        deliveryChargesList.add(deliveryCharges)

        val benefitAdapter = ProductBenefitAdapter(mContext, deliveryChargesList)
        detailViewProductBinding.recViewProdBenefits.adapter = benefitAdapter
    }

    override fun onInvalidCredential() {
//        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        detailVM.deleteUser()
        detailVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onApiFailure(msg: String) {
//        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onProductDetailFailure(msg: String) {
        detailViewProductBinding.nsvProductDetail.visibility = View.GONE
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onFailure(msg: String) {
//        loadingDialog.dialog?.dismiss()
//        errorToastTextView.text = msg
//        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
//        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onCartItemAdded(success: String) {
//        loadingDialog.dialog?.dismiss()
        myToast.show()
    }

    override fun onProductRatingData(it: RatingData) {
//        loadingDialog.dialog?.dismiss()

        if (it.productrating != null) {
            detailViewProductBinding.clNoReviewAvailable.visibility = View.GONE
            detailViewProductBinding.clReviewDataAvailable.visibility = View.VISIBLE

            detailViewProductBinding.tvRatingStarsText.text = it.average
            detailViewProductBinding.tvRatingUsername.text = it.productrating?.email
            detailViewProductBinding.tvRatingMsg.text = it.productrating?.message
            detailViewProductBinding.rbProductRating.rating = it.productrating?.rating!!

            setProductRating(detailViewProductBinding.tvRatingStarText, it.average.toFloat())
            setProductRating(detailViewProductBinding.tvRatingAmount, it.ratingcount)

            detailViewProductBinding.tvRatingCounts.text =
                "${it.average} Ratings and ${it.ratingcount} Reviews"
            formatStringDateToStandard(
                detailViewProductBinding.tvReviewDate,
                it.productrating!!.currentdate
            )
        } else {
            detailViewProductBinding.clNoReviewAvailable.visibility = View.VISIBLE
            detailViewProductBinding.clReviewDataAvailable.visibility = View.GONE
        }
    }

    private var prodQtyPosition = 0
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.e("selected qty", " :: ${parent?.getItemAtPosition(position)}")
        prodQty = qtyList!![position]
        prodQtyPosition = position

        val result = ((prodModel.price).times(prodModel.discount)).div(100f)
        productPrice = (prodModel.price.minus(result)).times(prodQty!!)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private var isSeeMoreClick = false
    private var isFavouriteAdded = true
    override fun onClick(v: View?) {
        when (v) {
            detailViewProductBinding.btnAddToCard -> {
                if (userData != null) {
                    if (!notDeliverable) {
                        detailVM.addItemsToCart(
                            userData!!.id, userData!!.token!!,
                            prodID, prodQty, 0
                        )
                    } else {
                        errorToastTextView.text =
                            "Delivery For selected Item Not Available in your Area"
                        errorToast.show()
                    }
                } else {
                    showLoginPopup()
                }
            }

            detailViewProductBinding.btnCheckPincodeNow -> {
                val edtPincodeValue =
                    detailViewProductBinding.edtPincodeDeliveryInfo.text.toString()
                if (TextUtils.isEmpty(edtPincodeValue)) {
                    // show error -> pincode is required
                    errorToastTextView.text = "Please enter pincode"
                    errorToast.show()
                } else if (edtPincodeValue.length < 6) {
                    // show error -> length should be 6
                    errorToastTextView.text = "Please enter 6 digit pincode"
                    errorToast.show()
                } else {
                    detailViewProductBinding.nsvProductDetail.scrollTo(120, 120)
                    try {
                        val imm: InputMethodManager =
                            mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(mActivity.currentFocus?.windowToken, 0)
                    } catch (e: java.lang.Exception) {
                        errorToastTextView.text = e.localizedMessage
                        errorToast.show()
                    }
                    detailVM.checkPincodeAvailability(edtPincodeValue)
                }
            }

            detailViewProductBinding.btnBuyNow -> {
                if (userData == null) {
                    showLoginPopup()
                    return
                }
                if (!notDeliverable) {
                    if (prodQty != 0) {
                        val prodList = ArrayList<OrderSummaryProduct>()
                        val result = prodModel.price.minus(Math.round((prodModel.price * prodModel.discount) / 100.0))
                        prodList.add(
                            OrderSummaryProduct(
                                prodModel.id.toString(),
                                prodQty ?: 0,
                                prodQty!!.times(result),
                                prodModel.keyfeature,
                                prodModel.material,
                                prodModel.title,
                                prodModel.alias,
                                prodModel.image,
                                prodModel.status,
                                prodModel.short_description,
                                prodModel.description,
                                prodModel.data_sheet,
                                prodModel.quantity,
                                prodModel.price,
                                prodModel.offer_price,
                                prodModel.product_code,
                                prodModel.product_condition,
                                prodModel.discount,
                                prodModel.latest,
                                prodModel.best,
                                prodModel.brandtitle,
                                prodModel.colortitle,
                                prodModel.delivery_charges,
                                1,
                                prodModel.can_free_delivery
                            )
                        )
                        sharedVM.setProductOrderSummaryList(prodList)
                        sharedVM.setDeliveryCharge(prodModel.delivery_charges ?: 0f)
                        sharedVM.setCodOptionForPayment(prodModel.can_cashon?.toInt() ?: 0)
                        Log.e("cashon", " :: ${prodModel.can_cashon?.toInt()}")
                        navController.navigate(R.id.action_detailViewProductFrag_to_orderSummary)
                    }
                } else {
                    errorToastTextView.text = "You Can't Buy For The Selected Pincode"
                    errorToast.show()
                }
            }

            detailViewProductBinding.tvSimilarDiscntProdsViewAll -> {
                val similarFragmentAction = DetailViewProductFragDirections
                    .actionDetailViewProductFragToSimilarDiscountedProdViewAll(prodModel.discount)

                navController.navigate(similarFragmentAction)
            }

            detailViewProductBinding.imgShareProduct -> {
                createProductShareDeepLink()
            }

            detailViewProductBinding.tvSeeMore -> {
                if (isSeeMoreClick) {
                    detailViewProductBinding.tvProductDescText.maxLines = 3
                    detailViewProductBinding.tvSeeMore.text = "See More"
                    isSeeMoreClick = false
                } else {
                    detailViewProductBinding.tvProductDescText.maxLines = 10
                    detailViewProductBinding.tvSeeMore.text = "See Less"
                    isSeeMoreClick = true
                }
            }

            detailViewProductBinding.tvRateProduct -> {
                val prodDetailDirections: DetailViewProductFragDirections.ActionDetailViewProductFragToReviewProduct =
                    DetailViewProductFragDirections.actionDetailViewProductFragToReviewProduct(
                        prodModel
                    )
                navController.navigate(prodDetailDirections)
            }

            detailViewProductBinding.tvRatingViewAll -> {
                val detailViewProductFragDirections: DetailViewProductFragDirections.ActionDetailViewProductFragToRatingViewAll =
                    DetailViewProductFragDirections.actionDetailViewProductFragToRatingViewAll()
                detailViewProductFragDirections.productID = prodID?.toInt() ?: 0
                navController.navigate(detailViewProductFragDirections)
            }

            detailViewProductBinding.cvAddToFav -> {
                if (isFavouriteAdded) {
                    isFavouriteAdded = false

                    if (prodModel.quantity <= 0) {
                        errorToastTextView.text = "Product quantity is not available"
                        errorToast.show()
                    } else {
                        detailViewProductBinding.lottieFavAddAnimation.visibility = View.VISIBLE
                        detailViewProductBinding.lottieFavAddAnimation.playAnimation()
                        detailViewProductBinding.imgAddToFavourites.setImageResource(R.drawable.ic_like)
                        val prod = Wishlist(
                            prodModel.id,
                            prodModel.keyfeature,
                            prodModel.material,
                            prodModel.title,
                            prodModel.alias,
                            prodModel.image,
                            prodModel.status,
                            prodModel.short_description,
                            prodModel.description,
                            prodModel.data_sheet,
                            prodModel.quantity,
                            prodModel.price,
                            prodModel.offer_price,
                            prodModel.product_code,
                            prodModel.product_condition,
                            prodModel.discount,
                            prodModel.latest,
                            prodModel.best,
                            prodModel.brandtitle,
                            prodModel.colortitle,
                            prodQtyPosition
                        )
                        detailVM.insertFavourites(prod)

                        Handler().postDelayed({
                            detailViewProductBinding.lottieFavAddAnimation.visibility = View.GONE
                        }, 1200)
                    }
                }
            }
        }
    }

    fun createProductShareDeepLink() {
        Log.e("main", "create link ")
        /*val dynamicLink: DynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://notebookstore.in/"))
            .setDynamicLinkDomain("notebookstore.page.link") // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build()) // Open links with com.example.ios on iOS
            //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
            .buildDynamicLink()*/
        //click -- link -- google play store -- inistalled/ or not  ----
        //val dynamicLinkUri: Uri = dynamicLink.getUri()
        //Log.e("main", "  Long refer " + dynamicLink.getUri())
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link

        // manual link
        val sharelinktext = "https://notebookstoreindia.page.link/?" +
                "link=https://notebookstore.in/myProductShare.php?productID=${prodModel.id}" +
                "&apn=" + mActivity.packageName +
                "&st=" + "${prodModel.title}" +
                "&sd=" + "${prodModel.short_description}" +
                "&si=" + "${Constant.PRODUCT_IMAGE_PATH}${prodModel.image}"

        Log.e("imageLink", " :: ${Constant.PRODUCT_IMAGE_PATH}${prodModel.image}")


        // shorten the link
        val shortLinkTask: Task<ShortDynamicLink> = FirebaseDynamicLinks.getInstance()
            .createDynamicLink() //.setLongLink(dynamicLink.getUri())
            .setLongLink(Uri.parse(sharelinktext)) // manually
            .buildShortDynamicLink()
            .addOnCompleteListener(
                mActivity
            ) { task ->
                if (task.isSuccessful) {
                    // Short link created
                    val shortLink: Uri = task.result?.shortLink ?: Uri.EMPTY
                    val flowchartLink: Uri = task.result?.previewLink ?: Uri.EMPTY
                    Log.e("main ", "short link $shortLink")
                    // share app dialog

                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shortLink.toString())
                        type = "text/plain"
                        startActivity(this)
                    }
                } else {
                    // Error
                    // ...
                    Log.e("main", " error " + task.exception)
                    errorToastTextView.text = task.exception?.localizedMessage
                    errorToast.show()
                }
            }
    }

    override fun onPause() {
        super.onPause()
        mActivity.currentFocus?.clearFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(
            "ARTICLE_SCROLL_POSITION",
            intArrayOf(
                detailViewProductBinding.nsvProductDetail.scrollX,
                detailViewProductBinding.nsvProductDetail.scrollY
            )
        )
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION")
            if (position != null)
                detailViewProductBinding.nsvProductDetail.post {
                    detailViewProductBinding.nsvProductDetail.scrollTo(
                        position[0],
                        position[1]
                    )
                }
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun ondismissed() {}

    override fun pinSuccessful(mesg: String, date: String) {
        checkDelivery(1, date)
    }

    override fun onDeliveryNotAvailable(mesg: String) {
        checkDelivery(2, mesg)
    }

    private fun showLoginPopup() {
        val userLoginRequestPopup = UserLogoutDialog()
        userLoginRequestPopup.isCancelable = false
        userLoginRequestPopup.setUserLoginRequestListener(this@DetailViewProductFrag)
        userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
    }
}