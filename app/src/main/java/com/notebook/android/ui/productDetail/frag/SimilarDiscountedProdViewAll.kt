package com.notebook.android.ui.productDetail.frag

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.gson.Gson

import com.notebook.android.R
import com.notebook.android.adapter.home.productAdapter.FilterCommonProductAdapter
import com.notebook.android.adapter.home.productAdapter.SimilarDiscountedProdAdapter
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.FilterProduct
import com.notebook.android.data.db.entities.Product
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentSimilarDiscountedProdViewAllBinding
import com.notebook.android.decoration.GridItemDecoration
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.model.filter.PaginationData
import com.notebook.android.ui.category.CategoryDetailProductFragDirections
import com.notebook.android.ui.category.FilterCommonProductListener
import com.notebook.android.ui.category.FilterCommonProductVM
import com.notebook.android.ui.category.FilterCommonProductVMFactory
import com.notebook.android.ui.category.subCategory.SubCategoryViewProductFragArgs
import com.notebook.android.ui.category.subCategory.SubCategoryViewProductFragDirections
import com.notebook.android.ui.dashboard.cart.CartResponseListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.SortByDialogFrag
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.base.BaseFragment
import com.notebook.android.ui.productDetail.listener.SimilarDiscListener
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SimilarDiscountedProdViewAll : BaseFragment(), KodeinAware,
    SwipeRefreshLayout.OnRefreshListener, FilterCommonProductListener, View.OnClickListener,
    SortByDialogFrag.SortSelectedValueListener, UserLogoutDialog.UserLoginPopupListener {

    override val kodein by kodein()
    private val viewModelFactory : FilterCommonProductVMFactory by instance()
    private val filterCommonProductVM: FilterCommonProductVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(FilterCommonProductVM::class.java)
    }
    private lateinit var fragSimilarDiscProdBinding:FragmentSimilarDiscountedProdViewAllBinding
    private lateinit var navController: NavController
    private var userData: User?= null

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private var brandIDArray:List<Int> ?=null
    private var colorIDArray:List<Int> ?=null
    private var discValueArray:List<Int> ?=null
    private var rateValueArray:List<Int> ?=null
    private var couponIDArray:List<Int> ?= null
    private var price1 = 0
    private var price2 = 100000
    private var filterRawData: FilterRequestData?= null
    private var pageData = PaginationData()

    private lateinit var myToast: Toast
    private lateinit var errorToast:Toast
    private lateinit var successToast:Toast
    private lateinit var errorToastTextView:TextView
    private lateinit var successToastTextView:TextView

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        val  args = SimilarDiscountedProdViewAllArgs.fromBundle(requireArguments())
        if (args.discountFilter != 0) {
            discValueArray = listOf(args.discountFilter)
        }
        filterRawData = FilterRequestData(brandIDArray?:ArrayList(), price1, price2,
            discValueArray?:ArrayList(), colorIDArray?:ArrayList(),
            rateValueArray?:ArrayList(), couponIDArray?:ArrayList(),
            Constant.FILTER_DISCOUNTED_PRODUCT_TYPE, 0, notebookPrefs.sortedValue)
        onRefresh()
    }

    override fun onResume() {
        super.onResume()
        filterCommonProductVM.getProductFilterByWise(filterRawData!!,0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragSimilarDiscProdBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_similar_discounted_prod_view_all, container, false)
        fragSimilarDiscProdBinding.lifecycleOwner = this
        filterCommonProductVM.filterProdListener = this
        setupRecyclerView()

        //custom toast initialize view here....
        val layouttoast = inflater.inflate(R.layout.custom_toast_layout,
            fragSimilarDiscProdBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).setText("Item added successfully !!")

//        layouttoast.findViewById(R.id.imagetoast)).setBackgroundResource(R.drawable.icon);
        val GRAVITY_BOTTOM = 80
        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_CENTER, 0, 0)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragSimilarDiscProdBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragSimilarDiscProdBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        fragSimilarDiscProdBinding.srlSimilarDiscProducts.setOnRefreshListener(this)
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.
        setColorSchemeColors(
            ContextCompat.getColor(mContext, android.R.color.holo_green_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_red_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_blue_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_orange_dark))

        return fragSimilarDiscProdBinding.root
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_similar_discounted_prod_view_all
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        filterCommonProductVM.getUserData().observe(viewLifecycleOwner, Observer {
            if(it != null){
                userData = it
            }else{
                userData = null
            }
        })

        if(notebookPrefs.FilterCommonImageUrl?.isEmpty() == true){
            fragSimilarDiscProdBinding.imgSimilarCategBanner.visibility = View.GONE
        }else {
            fragSimilarDiscProdBinding.imgSimilarCategBanner.visibility = View.VISIBLE
            Glide.with(mContext).apply {
                load("${Constant.BASE_IMAGE_PATH}${notebookPrefs.FilterCommonImageUrl}")
                    .into(fragSimilarDiscProdBinding.imgSimilarCategBanner)
            }
        }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("filterData")?.observe(
            viewLifecycleOwner, Observer {
                filterRawData = Gson().fromJson(it, FilterRequestData::class.java)
                filterRawData!!.para = 0
                filterRawData!!.filter = Constant.FILTER_DISCOUNTED_PRODUCT_TYPE
              onRefresh()
                Log.e("rawDataSubCategory", " :: ${Gson().fromJson(it, FilterRequestData::class.java)}")
            })

        filterCommonProductVM.getPageData.observe(viewLifecycleOwner,{
            pageData = it
        })


        val filterProductList = ArrayList<FilterProduct>()
        filterCommonProductVM.getFilterCommonProdDataFromDB.observe(viewLifecycleOwner, Observer {
            if (isRefreshing){
                filterProductList.clear()
            }

            filterProductList.addAll(it)

            val bsProdAdapter = FilterCommonProductAdapter(mContext, filterProductList,
                object : FilterCommonProductAdapter.FilterCommonProductListener,
                    UserLogoutDialog.UserLoginPopupListener {

                    override fun fcProductCallback(fcProd: FilterProduct, imgProduct: ImageView) {
                        val prod = Product(fcProd.id.toString(), fcProd.keyfeature,
                            fcProd.material,
                            fcProd.title, fcProd.alias, fcProd.image,
                            fcProd.status, fcProd.short_description,
                            fcProd.description, fcProd.data_sheet,
                            fcProd.quantity, fcProd.price, fcProd.offer_price,
                            fcProd.product_code, fcProd.product_condition,
                            fcProd.discount, fcProd.latest,
                            fcProd.best, fcProd.brandtitle, fcProd.colortitle)

                        val extras = FragmentNavigatorExtras(imgProduct to "transitionHome")
                        val similarProductFragDirections: SimilarDiscountedProdViewAllDirections.ActionSimilarDiscountedProdViewAllToDetailViewProductFrag =
                            SimilarDiscountedProdViewAllDirections.actionSimilarDiscountedProdViewAllToDetailViewProductFrag(prod)
//                        navController.navigate(similarProductFragDirections, extras)

                        notebookPrefs.similarDiscountedProductClicked = true
                        navController.previousBackStackEntry?.savedStateHandle?.set("prodData", Gson().toJson(prod))
                        navController.popBackStack()
//                        popBackStackWithResult(prod)
                    }

                    override fun fcAddToCart(prodID: Int, cartQty: Int) {
                        if(userData != null){
                            filterCommonProductVM.addItemsToCart(userData!!.id!!, userData!!.token!!, prodID, cartQty, 0)
                        }else{
                            val userLoginRequestPopup = UserLogoutDialog()
                            userLoginRequestPopup.isCancelable = false
                            userLoginRequestPopup.setUserLoginRequestListener(this)
                            userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                        }
                    }

                    override fun fcCartEmptyError(msg: String) {
                        errorToastTextView.text = msg
                        errorToast.show()
                    }

                    override fun onUserAccepted(isAccept: Boolean) {
                        navController.navigate(R.id.loginFrag)
                    }
                })

            fragSimilarDiscProdBinding.recViewSimilarProds.adapter = bsProdAdapter
        })

        fragSimilarDiscProdBinding.tvFilterByProducts.setOnClickListener(this)
        fragSimilarDiscProdBinding.tvSortByProducts.setOnClickListener(this)
    }

    private fun setupRecyclerView(){
        val layoutManagerSubCateg = GridLayoutManager(mContext, 2, RecyclerView.VERTICAL, false)
        fragSimilarDiscProdBinding.recViewSimilarProds.apply {
            layoutManager = layoutManagerSubCateg
            addItemDecoration(GridItemDecoration(10,2))
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }

        fragSimilarDiscProdBinding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight)) &&
                    scrollY > oldScrollY
                ) {
                    Log.d(TAG, "NOW LOAD MORE")
                    if (!isLoading)
                        loadPaginatedData()
                }
            }
        })
    }

    private val TAG = "SimilarDiscountedProdVi"

    private var isLoading = false
    private var isRefreshing = false
    private fun loadPaginatedData() {

        isRefreshing = false

        pageData.next_page_url?.let {
            val nextPage = it.substringAfterLast("=").toInt()
            filterCommonProductVM.getProductFilterByWise(filterRawData!!, nextPage)
            isLoading = true
        }

    }

    override fun onApiCallStarted() {
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = true
        isLoading = true
    }

    override fun onApiCartCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Loading dialog show")
    }

    override fun onSuccess(isListSizeGreater:Boolean) {
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = false
        isLoading = false

        if(isListSizeGreater){
            fragSimilarDiscProdBinding.imgNoProdFound.visibility = View.GONE
        }
    }

    override fun onCartItemAdded(cartMsg: String) {
        loadingDialog.dismissAllowingStateLoss()
        myToast.show()
    }

    override fun onFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
        isLoading = false
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
        isLoading = false
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        filterCommonProductVM.deleteUser()
        filterCommonProductVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
        isLoading = false
    }

    override fun onUserAccepted(isAccept: Boolean) {
        filterCommonProductVM.deleteUser()
        filterCommonProductVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
        isLoading = false
    }

    override fun onGetBannerImageData(imgUrl: String) {
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = false
        isLoading = false
        if(imgUrl.isEmpty()){
            fragSimilarDiscProdBinding.imgSimilarCategBanner.visibility = View.GONE
        }else{
            fragSimilarDiscProdBinding.imgSimilarCategBanner.visibility = View.VISIBLE
            notebookPrefs.FilterCommonImageUrl = imgUrl
            Glide.with(mContext).apply {
                load("${Constant.BASE_IMAGE_PATH}${imgUrl}")
                    .into(fragSimilarDiscProdBinding.imgSimilarCategBanner)
            }
        }
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        fragSimilarDiscProdBinding.srlSimilarDiscProducts.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
        isLoading = false
    }

    override fun onRefresh() {
        isRefreshing = true
        filterCommonProductVM.getProductFilterByWise(filterRawData!!,0)
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragSimilarDiscProdBinding.tvFilterByProducts -> {
                navController.navigate(R.id.filterByProductFrag)
            }

            fragSimilarDiscProdBinding.tvSortByProducts -> {
                val sortingDialog = SortByDialogFrag()
                sortingDialog.setSortingListener(this)
                sortingDialog.show(mActivity.supportFragmentManager, "Sorting Dialog")
            }
        }
    }

    override fun sortSelectedValue(value: Int) {
        Log.e("sorting value", " :: $value")
        filterRawData!!.para = 0
        filterRawData!!.filter = Constant.FILTER_DISCOUNTED_PRODUCT_TYPE
        filterRawData!!.filterType = value
        onRefresh()
    }
}
