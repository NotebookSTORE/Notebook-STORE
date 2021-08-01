package com.notebook.android.ui.dashboard.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.notebook.android.R
import com.notebook.android.adapter.home.*
import com.notebook.android.adapter.home.PagerAdapter.HomeTopSliderAdapter
import com.notebook.android.adapter.home.PagerAdapter.LatestOfferSliderAdapter
import com.notebook.android.adapter.home.PagerAdapter.MerchantBenefitSliderAdapter
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.data.preferences.RefferalPreferance
import com.notebook.android.databinding.FragmentHomeBinding
import com.notebook.android.decoration.GridItemDecoration
import com.notebook.android.receiver.NetworkReceiver
import com.notebook.android.ui.dashboard.MainDashboardPage
import com.notebook.android.ui.dashboard.factory.DashboardViewModelFactory
import com.notebook.android.ui.dashboard.listener.DashboardApiListener
import com.notebook.android.ui.dashboard.viewmodel.DashboardViewModel
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.CouponAlertDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.utility.Constant.BANNER_TYPE_BULK_QUERY
import com.notebook.android.utility.Constant.BANNER_TYPE_HOME
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList

class HomeFrag : Fragment(), KodeinAware, View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener, DashboardApiListener,
    UserLogoutDialog.UserLoginPopupListener {

    //initialize binding or viewmodel views initialize here......
    private lateinit var homeFragBinding: FragmentHomeBinding
    private lateinit var navController: NavController
    override val kodein by kodein()
    private val viewModelFactory: DashboardViewModelFactory by instance()
    private val dashboardVM: DashboardViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)
    }

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog()
    }

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val refferalPrefs: RefferalPreferance by lazy {
        RefferalPreferance(mContext)
    }

    private var mNetworkReceiver: BroadcastReceiver? = null
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val message = intent.getStringExtra("message")
            val isNetworkAvailable = intent.getBooleanExtra("checkNetwork", false)
            Log.e("broadcast check network", " :: ${message} :: ${isNetworkAvailable}")

            if (isNetworkAvailable) {
                successToastTextView.text = "Internet is available"
                successToast.show()
            } else {
                errorToastTextView.text = "No internet available"
                errorToast.show()
            }
        }
    }

    //initialize adapter views here.....
    private lateinit var subCategoryAdapter: HomeSubCategoryAdapter
    private lateinit var brandAdapter: BrandDataAdapter
    private var brandDataList: ArrayList<Brand>? = null
    private var categroyDataList: ArrayList<SubCategory>? = null
    private var scrollCount = 0
    private var scrollCountForCategory = 0

    private var userData: User? = null
    private lateinit var timer: Timer
    private lateinit var timer1: Timer
    private lateinit var timer2: Timer
    private lateinit var timer3: Timer
    private val DELAY_MS: Long = 4000 //delay in milliseconds before task is to be executed
    private val PERIOD_MS: Long = 6000 // time in milliseconds between successive task executions.

    private var prodID: Int = -1
    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        if (arguments != null) {
            prodID = requireArguments().getInt("prodID")
            Log.e("productID", " :: $prodID")
        }
    }

    private lateinit var myToast: Toast
    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dashboardVM.getBannerData(BANNER_TYPE_HOME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mActivity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        homeFragBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home, container, false
        )
        homeFragBinding.lifecycleOwner = this
        dashboardVM.dashboardApiListener = this

        dashboardVM.getBulkQueryData(BANNER_TYPE_BULK_QUERY)
        dashboardVM.getDrawerCategoryData()
        //custom toast initialize view here....
        val layouttoast = inflater.inflate(
            R.layout.custom_toast_layout,
            homeFragBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).text =
            "Item added successfully !!"
        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_CENTER, 0, 0)

        Log.e("refferalData", " :: ${refferalPrefs.refferCode}")

        //success toast layout initialization here....
        val successToastLayout: View = inflater.inflate(
            R.layout.custom_toast_layout,
            homeFragBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        successToastTextView =
            (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout: View = inflater.inflate(
            R.layout.error_custom_toast_layout,
            homeFragBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView =
            (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        homeFragBinding.srlHomeFrag.setColorSchemeColors(
            ContextCompat.getColor(mContext, android.R.color.holo_green_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_red_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_blue_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_orange_dark)
        )
        homeFragBinding.srlHomeFrag.setOnRefreshListener(this)

        setUpRecyclerView()
        timer = Timer()
        timer1 = Timer()
        timer2 = Timer()
        timer3 = Timer()
        timer.scheduleAtFixedRate(SliderTimer(), DELAY_MS, PERIOD_MS)
        timer1.scheduleAtFixedRate(SliderTimer1(), DELAY_MS, PERIOD_MS)
        timer2.scheduleAtFixedRate(SliderTimer2(), DELAY_MS, PERIOD_MS)
        timer3.scheduleAtFixedRate(SliderTimerForBulkOrder(), DELAY_MS, PERIOD_MS)

        //dynamic check network call using braodcast receiver
        mNetworkReceiver = NetworkReceiver()
        registerNetworkBroadcastForNougat()

        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(mMessageReceiver, IntentFilter("network_state_check"))

        return homeFragBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        if (prodID > 0) {
            val prod = Product(
                prodID.toString(), ArrayList(),
                "", "", "", "",
                0, "", "", "",
                0, 0f, 0,
                "", "", 0,
                0, 0, "", "", 0f, 0, 0f
            )
            val homeToDetailViewFrag: HomeFragDirections.ActionHomeFragToDetailViewProductFrag =
                HomeFragDirections.actionHomeFragToDetailViewProductFrag(prod)
            navController.navigate(homeToDetailViewFrag)
            prodID = -1
        }

        dashboardVM.getUserData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                userData = it
            } else {
                userData = null
            }
        })


        if (!notebookPrefs.userToken.isNullOrEmpty()) {
            dashboardVM.getUserDataFromServer(notebookPrefs.userID, notebookPrefs.userToken!!)
            dashboardVM.getCartData(notebookPrefs.userID, notebookPrefs.userToken!!)
        }

        setBannerOrCategoryData()
        homeFragBinding.clBulkOrderQuery.setOnClickListener(this)
        homeFragBinding.llSearchProductLayout.setOnClickListener(this)
        homeFragBinding.tvBestSellersViewAll.setOnClickListener(this)
        homeFragBinding.tvLatestProductsViewAll.setOnClickListener(this)
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mActivity.registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mActivity.registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            mActivity.unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setUpRecyclerView() {
        //sub category recycler view layout manager...
        val layoutManagerSubCategory =
            object : LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false) {
                override fun smoothScrollToPosition(
                    recyclerView: RecyclerView?,
                    state: RecyclerView.State?,
                    position: Int
                ) {
                    val smoothScroller = object : LinearSmoothScroller(mContext) {
                        val SPEED = 6600f
                        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                            return SPEED / displayMetrics!!.densityDpi
                        }
                    }
                    smoothScroller.targetPosition = position
                    startSmoothScroll(smoothScroller)
                }

                override fun canScrollHorizontally(): Boolean {
                    return true
                }
            }

        homeFragBinding.recViewSubCategory.apply {
            layoutManager = layoutManagerSubCategory
            hasFixedSize()
            setItemViewCacheSize(1024)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }

        //Recycler view
        val layoutManagerMainCategory = GridLayoutManager(
            mContext,
            3, RecyclerView.VERTICAL, false
        )
        homeFragBinding.recViewCategory.apply {
            layoutManager = layoutManagerMainCategory
            addItemDecoration(GridItemDecoration(5, 3))
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }

        //Recycler view Best Seller Product
        val layoutManagerBestSellerProduct =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        homeFragBinding.recViewBestSellers.layoutManager = layoutManagerBestSellerProduct
        homeFragBinding.recViewBestSellers.hasFixedSize()
        homeFragBinding.recViewBestSellers.itemAnimator = DefaultItemAnimator()

        //Recycler view Latest Product
        val layoutManagerLatestProduct =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        homeFragBinding.recViewLatestProducts.layoutManager = layoutManagerLatestProduct
        homeFragBinding.recViewLatestProducts.hasFixedSize()
        homeFragBinding.recViewLatestProducts.itemAnimator = DefaultItemAnimator()

        //brand recycler view...
        val layoutManagerBrands = object : LinearLayoutManager(mContext) {
            override fun smoothScrollToPosition(
                recyclerView: RecyclerView?,
                state: RecyclerView.State?,
                position: Int
            ) {

                val smoothScroller = object : LinearSmoothScroller(mContext) {
                    val SPEED = 6600f
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                        return SPEED / displayMetrics!!.densityDpi
                    }
                }

                smoothScroller.targetPosition = position
                startSmoothScroll(smoothScroller)
            }
        }

        layoutManagerBrands.orientation = LinearLayoutManager.HORIZONTAL
        homeFragBinding.recViewBrands.apply {
            layoutManager = layoutManagerBrands
            hasFixedSize()
            setItemViewCacheSize(1024)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }
    }

    private fun autoScrollAnother() {
//        brandDataList = ArrayList()
        scrollCount = 0
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                homeFragBinding.recViewBrands.smoothScrollToPosition((scrollCount++))
                if (scrollCount == brandAdapter.itemCount - 4) {
                    brandDataList!!.addAll(brandDataList!!)
                    brandAdapter.notifyDataSetChanged()
                }
                handler.postDelayed(this, 2000)
            }
        };
        handler.postDelayed(runnable, 2000)
    }

    //    private lateinit var handler:Handler
    private fun autoScrollForCategory() {
        scrollCountForCategory = 0
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                homeFragBinding.recViewSubCategory.smoothScrollToPosition((scrollCountForCategory++))
                if (scrollCountForCategory == subCategoryAdapter.itemCount - 4) {
                    categroyDataList!!.addAll(categroyDataList!!)
                    subCategoryAdapter.notifyDataSetChanged()
                }
                handler.postDelayed(this, 2000)
//                disableTouch()
            }
        }
        handler.postDelayed(runnable, 2000)
    }

    private var speedScroll = 0
    private val mHandler = Handler()
    private val runnable = object : Runnable {
        var count = 0
        override fun run() {
            if (count == homeFragBinding.recViewSubCategory.adapter?.itemCount) count = 0
            if (count < homeFragBinding.recViewSubCategory.adapter?.itemCount ?: -1) {
                homeFragBinding.recViewSubCategory.smoothScrollToPosition(++count)
                mHandler.postDelayed(this, speedScroll.toLong())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mHandler.postDelayed(runnable, speedScroll.toLong())
    }

    private lateinit var bannerList: ArrayList<Banner>
    private lateinit var latestOfferList: ArrayList<LatestOffer>
    private lateinit var merchantBannerList: ArrayList<MerchantBanner>
    private lateinit var bulkOrderBannerList: ArrayList<Banner>
    private fun setBannerOrCategoryData() {
        bannerList = ArrayList()
        latestOfferList = ArrayList()
        merchantBannerList = ArrayList()
        bulkOrderBannerList = ArrayList()
        dashboardVM.getAllCategoryDataFromDB.observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {
                homeFragBinding.srlHomeFrag.isRefreshing = false
                homeFragBinding.clHomeAllViews.visibility = View.VISIBLE
            } else {
                homeFragBinding.srlHomeFrag.isRefreshing = true
                homeFragBinding.clHomeAllViews.visibility = View.GONE
            }
            val prodCategoryAdapter = HomeProductSectionAdapter(mContext, it as ArrayList<Category>,
                object : HomeProductSectionAdapter.CategoryProductListener {
                    override fun getCategProdID(categID: Int, title: String) {
                        val homeFragDirections: HomeFragDirections.ActionHomeFragToCategoryDetailProductFrag =
                            HomeFragDirections.actionHomeFragToCategoryDetailProductFrag(categID)
                        navController.navigate(homeFragDirections)
                        (mActivity as MainDashboardPage).setSubCategoryTitle(title)
                    }
                })
            homeFragBinding.recViewCategory.adapter = prodCategoryAdapter
        })

        dashboardVM.getAllBannerData.observe(viewLifecycleOwner, Observer {
            bannerList = ArrayList()
            val sliderAdapter =
                HomeTopSliderAdapter(
                    mContext,
                    it as ArrayList<Banner>,
                    object : HomeTopSliderAdapter.BannerSliderListener {
                        override fun onSliderClick(bannerData: Banner) {
                            Log.d(
                                "Home top banner",
                                "onSliderClick() called with: bannerData = $bannerData"
                            )

                            bannerData.brand_id?.let {
                                val action =
                                    HomeFragDirections.actionHomeFragToSubCategoryViewProductFrag()
                                action.brandId = it
                                action.subCategoryID = 0
                                action.subCategTitle = ""
                                navController.navigate(action)
                                (mActivity as MainDashboardPage).setSubCategoryTitle("")
                            }

                            val homeDirections: HomeFragDirections.ActionHomeFragToOfferViewProdLink =
                                HomeFragDirections.actionHomeFragToOfferViewProdLink(
                                    bannerData.url ?: ""
                                )

                            if (bannerData.banner_use_for == 1) {
                                Log.e("offerProductID", " :: ${bannerData.product_id}")
                                if (!bannerData.product_id.equals("0", true)) {
                                    val prod = Product(
                                        bannerData.product_id, ArrayList(),
                                        "", "", "", "",
                                        0, "", "", "",
                                        0, 0f, 0,
                                        "", "", 0,
                                        0, 0, "", "", 0f,
                                        0, 0f
                                    )
                                    val homeToDetailViewFrag: HomeFragDirections.ActionHomeFragToDetailViewProductFrag =
                                        HomeFragDirections.actionHomeFragToDetailViewProductFrag(
                                            prod
                                        )
                                    navController.navigate(homeToDetailViewFrag)
                                }

                            } else if (bannerData.banner_use_for == 2) {
                                navController.navigate(R.id.merchantMainFrag)
                            } else if (bannerData.url?.isNotEmpty() == true) {
                                navController.navigate(homeDirections)
                            }
                            Log.e("offer web link", " :: ${bannerData.url}")

                        }
                    })

            bannerList = it
            homeFragBinding.vpImageSlider.adapter = sliderAdapter
            homeFragBinding.tlImageSliderIndicator.setupWithViewPager(homeFragBinding.vpImageSlider)
        })

//        setViewPagerTimer(bannerList.size)
        val llManager: LinearLayoutManager =
            object : LinearLayoutManager(mContext, HORIZONTAL, false) {
                override fun smoothScrollToPosition(
                    recyclerView: RecyclerView,
                    state: RecyclerView.State,
                    position: Int
                ) {
                    val smoothScroller: LinearSmoothScroller =
                        object : LinearSmoothScroller(mContext) {
                            private val SPEED = 4000f // Change this value (default=25f)
                            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                                return SPEED
                            }
                        }
                    smoothScroller.targetPosition = position
                    startSmoothScroll(smoothScroller)
                }
            }

        brandDataList = ArrayList()
        dashboardVM.getBrandsFromDB.observe(viewLifecycleOwner, Observer {
            brandDataList = it as ArrayList<Brand>
            brandAdapter = BrandDataAdapter(mContext, it, object : BrandDataAdapter.BrandListener {
                override fun BrandId(brandID: Int, title: String) {
                    val action = HomeFragDirections.actionHomeFragToSubCategoryViewProductFrag()
                    action.brandId = brandID
                    action.subCategoryID = 0
                    action.subCategTitle = title
                    navController.navigate(action)
                    (mActivity as MainDashboardPage).setSubCategoryTitle(title)
                }
            })
            homeFragBinding.recViewBrands.adapter = brandAdapter
            autoScrollAnother()
        })

        categroyDataList = ArrayList()
        dashboardVM.getAllSubCategoryFromDB.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                homeFragBinding.srlHomeFrag.isRefreshing = false
                homeFragBinding.clHomeAllViews.visibility = View.VISIBLE
            } else {
                homeFragBinding.srlHomeFrag.isRefreshing = true
                homeFragBinding.clHomeAllViews.visibility = View.GONE
            }
            categroyDataList = it as ArrayList<SubCategory>
            autoScrollForCategory()
            subCategoryAdapter = HomeSubCategoryAdapter(mContext, categroyDataList!!,
                object : HomeSubCategoryAdapter.SubCategoryListener {
                    override fun subCategoryId(subCategID: Int, title: String) {
                        val action = HomeFragDirections.actionHomeFragToSubCategoryViewProductFrag()
                        action.subCategoryID = subCategID
                        action.subCategTitle = title
                        navController.navigate(action)
                        (mActivity as MainDashboardPage).setSubCategoryTitle(title)
                    }
                })
            homeFragBinding.recViewSubCategory.adapter = subCategoryAdapter


            homeFragBinding.recViewSubCategory.apply {
                adapter = subCategoryAdapter
            }
            autoScrollForCategory()
        })

        dashboardVM.getBestSellerProductFromDB.observe(viewLifecycleOwner, Observer {
            val bestSellerAdapter =
                BestSellerHomeProductAdapter(mContext, it as ArrayList<BestSellerHome>,
                    object : BestSellerHomeProductAdapter.BestSellerProductListener,
                        UserLogoutDialog.UserLoginPopupListener {
                        override fun bestProductObj(bestSellerProd: BestSellerHome) {

                            val prod = Product(
                                bestSellerProd.id.toString(),
                                bestSellerProd.keyfeature,
                                bestSellerProd.material,
                                bestSellerProd.title,
                                bestSellerProd.alias,
                                bestSellerProd.image,
                                bestSellerProd.status,
                                bestSellerProd.short_description,
                                bestSellerProd.description,
                                bestSellerProd.data_sheet,
                                bestSellerProd.quantity,
                                bestSellerProd.price,
                                bestSellerProd.offer_price,
                                bestSellerProd.product_code,
                                bestSellerProd.product_condition,
                                bestSellerProd.discount,
                                bestSellerProd.latest,
                                bestSellerProd.best,
                                bestSellerProd.brandtitle,
                                bestSellerProd.colortitle,
                                bestSellerProd.customerRating,
                                bestSellerProd.reviewCount
                            )
                            val homeToDetailViewFrag: HomeFragDirections.ActionHomeFragToDetailViewProductFrag =
                                HomeFragDirections.actionHomeFragToDetailViewProductFrag(prod)
                            navController.navigate(homeToDetailViewFrag)
                        }

                        override fun bestAddToCart(prodID: Int, cartQty: Int) {
                            if (userData != null) {
                                dashboardVM.addItemsToCart(
                                    userData!!.id,
                                    userData!!.token!!,
                                    prodID,
                                    cartQty,
                                    0
                                )
                            } else {
                                val userLoginRequestPopup = UserLogoutDialog()
                                userLoginRequestPopup.isCancelable = false
                                userLoginRequestPopup.setUserLoginRequestListener(this)
                                userLoginRequestPopup.show(
                                    mActivity.supportFragmentManager,
                                    "User login request popup !!"
                                )
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
            homeFragBinding.recViewBestSellers.adapter = bestSellerAdapter
        })

        dashboardVM.getLatestProductHomeFromDB.observe(viewLifecycleOwner, Observer {
            val latestProductAdapter =
                LatestProductHomeAdapter(mContext, it as ArrayList<LatestProductHome>,
                    object : LatestProductHomeAdapter.latestProductListener,
                        UserLogoutDialog.UserLoginPopupListener {
                        override fun latestProductObj(latestProd: LatestProductHome) {
                            val prod = Product(
                                latestProd.id.toString(),
                                latestProd.keyfeature,
                                latestProd.material,
                                latestProd.title,
                                latestProd.alias,
                                latestProd.image,
                                latestProd.status,
                                latestProd.short_description,
                                latestProd.description,
                                latestProd.data_sheet,
                                latestProd.quantity,
                                latestProd.price,
                                latestProd.offer_price,
                                latestProd.product_code,
                                latestProd.product_condition,
                                latestProd.discount,
                                latestProd.latest,
                                latestProd.best,
                                latestProd.brandtitle,
                                latestProd.colortitle,
                                latestProd.customerRating,
                                latestProd.reviewCount
                            )
                            val homeToDetailViewFrag: HomeFragDirections.ActionHomeFragToDetailViewProductFrag =
                                HomeFragDirections.actionHomeFragToDetailViewProductFrag(prod)
                            navController.navigate(homeToDetailViewFrag)
                        }

                        override fun fcCartEmptyError(msg: String) {
                            errorToastTextView.text = msg
                            errorToast.show()
                        }

                        override fun latestAddToCart(prodID: Int, cartQty: Int) {
                            if (userData != null) {
                                dashboardVM.addItemsToCart(
                                    userData!!.id,
                                    userData!!.token!!,
                                    prodID,
                                    cartQty,
                                    0
                                )
                            } else {
                                val userLoginRequestPopup = UserLogoutDialog()
                                userLoginRequestPopup.isCancelable = false
                                userLoginRequestPopup.setUserLoginRequestListener(this)
                                userLoginRequestPopup.show(
                                    mActivity.supportFragmentManager,
                                    "User login request popup !!"
                                )
                            }
                        }

                        override fun onUserAccepted(isAccept: Boolean) {
                            navController.navigate(R.id.loginFrag)
                        }
                    })
            homeFragBinding.recViewLatestProducts.adapter = latestProductAdapter
        })

        brandDataList = ArrayList()
        dashboardVM.getBrandsFromDB.observe(viewLifecycleOwner, Observer {
            brandDataList = it as ArrayList<Brand>
            brandAdapter = BrandDataAdapter(mContext, it, object : BrandDataAdapter.BrandListener {
                override fun BrandId(brandID: Int, title: String) {
                    val action = HomeFragDirections.actionHomeFragToSubCategoryViewProductFrag()
                    action.brandId = brandID
                    action.subCategoryID = 0
                    action.subCategTitle = title
                    navController.navigate(action)
                    (mActivity as MainDashboardPage).setSubCategoryTitle(title)
                }

            })
            homeFragBinding.recViewBrands.adapter = brandAdapter
            autoScrollAnother()
        })

        dashboardVM.getLatestOfferFromDB.observe(viewLifecycleOwner, Observer {
            val sliderAdapter =
                LatestOfferSliderAdapter(mContext, it as ArrayList<LatestOffer>,
                    object : LatestOfferSliderAdapter.LatestOfferSliderListener {
                      /*  override fun onOfferSliderClick(offerUrl: String, offerType: Int) {

                            *//*  val homeDirections:HomeFragDirections.ActionHomeFragToOfferViewProdLink =
                                  HomeFragDirections.actionHomeFragToOfferViewProdLink(offerUrl)*//*
                            if (offerUrl.isNotEmpty()) {
                                Log.e("latestOfferUrl", " :: $offerUrl")
                                val prod = Product(
                                    offerUrl, ArrayList(),
                                    "", "", "", "",
                                    0, "", "", "",
                                    0, 0f, 0,
                                    "", "", 0,
                                    0, 0, "", "", 0f,
                                    0, 0f
                                )
                                val homeToDetailViewFrag: HomeFragDirections.ActionHomeFragToDetailViewProductFrag =
                                    HomeFragDirections.actionHomeFragToDetailViewProductFrag(prod)
                                navController.navigate(homeToDetailViewFrag)
                            } else {
                                if (offerType == 1) {
                                    navController.navigate(R.id.action_homeFrag_to_latestProductPage)
                                } else {
                                    navController.navigate(R.id.action_homeFrag_to_bestSellerProductPage)
                                }
                            }
                        }*/

                        override fun onOfferSliderClick(latestOffer: LatestOffer) {
                            Log.d("Home latest offer", "onOfferSliderClick() called with: latestOffer = $latestOffer")
                            latestOffer.brand_id?.let {
                                val action =
                                    HomeFragDirections.actionHomeFragToSubCategoryViewProductFrag()
                                action.brandId = it
                                action.subCategoryID = latestOffer.category_id?:0
                                action.subCategTitle = ""
                                navController.navigate(action)
                                (mActivity as MainDashboardPage).setSubCategoryTitle("")
                            }

                        }
                    })
            latestOfferList = it
            homeFragBinding.vpImageOffersSlider.adapter = sliderAdapter
            homeFragBinding.tlImageOfferSliderIndicator.setupWithViewPager(homeFragBinding.vpImageOffersSlider)
        })

        dashboardVM.getMerchantBannerFromDB.observe(viewLifecycleOwner, Observer {
            val sliderAdapter =
                MerchantBenefitSliderAdapter(
                    mContext,
                    it as ArrayList<MerchantBanner>,
                    object : MerchantBenefitSliderAdapter.MerchantSectionListener {
                        override fun onClickMerchantSection() {
                            if (userData != null) {
                                if (userData!!.usertype == 1) {
                                    if (userData!!.status == 0) {
                                        if (notebookPrefs.primeUserUpgradeAvail == 1) {
                                            navController.navigate(R.id.primeMerchantFormFrag)
                                        } else {
                                            val userLoginRequestPopup = CouponAlertDialog()
                                            userLoginRequestPopup.isCancelable = true
                                            val bundle = Bundle()
                                            bundle.putString(
                                                "displayTitle",
                                                "You are already a Prime Merchant"
                                            )
                                            userLoginRequestPopup.arguments = bundle
//                                            userLoginRequestPopup.setUserLoginRequestListener(this)
                                            userLoginRequestPopup.show(
                                                mActivity.supportFragmentManager,
                                                "User login request popup !!"
                                            )
                                        }
                                    } else {
                                        navController.navigate(R.id.merchantMainFrag)
                                    }
                                } else if (userData!!.usertype == 0) {
                                    navController.navigate(R.id.merchantMainFrag)
                                } else {
                                    navController.navigate(R.id.merchantMainFrag)
                                }
                            } else {
                                navController.navigate(R.id.merchantMainFrag)
                            }
                        }
                    }
                )
            merchantBannerList = it
            homeFragBinding.vpMerchantBenefitsSlider.adapter = sliderAdapter
            homeFragBinding.tlImageMerchantBenefitsIndicator.setupWithViewPager(homeFragBinding.vpMerchantBenefitsSlider)
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            homeFragBinding.clBulkOrderQuery -> {
                navController.navigate(R.id.action_homeFrag_to_bulkOrderQuery)
            }

            homeFragBinding.llSearchProductLayout -> {
                navController.navigate(R.id.action_homeFrag_to_searchProductFrag)
            }

            homeFragBinding.tvBestSellersViewAll -> {
                navController.navigate(R.id.action_homeFrag_to_bestSellerProductPage)
            }

            homeFragBinding.tvLatestProductsViewAll -> {
                navController.navigate(R.id.action_homeFrag_to_latestProductPage)
            }

            homeFragBinding.btnRetryInternet -> {
                if (isNetworkAvailable()) {
                    dashboardVM.getBannerData(BANNER_TYPE_HOME)
                } else {
                    homeFragBinding.clInternetNotAvailable.visibility = View.VISIBLE
                    homeFragBinding.imgFileNotFound.setImageResource(R.drawable.ic_error)
                    homeFragBinding.tvInternetNotAvailText.text = "No internet available"
                }
            }
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            mContext.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also {
            return it != null && it.isConnected
        }
    }

    override fun onRefresh() {
        dashboardVM.getBannerData(BANNER_TYPE_HOME)
    }

    override fun onApiCallStarted() {
        homeFragBinding.srlHomeFrag.isRefreshing = true
        homeFragBinding.clHomeAllViews.visibility = View.GONE
    }

    override fun onApiCartAddCallStarted() {
        if (mActivity.supportFragmentManager.findFragmentByTag("Show loading dialog") == null) {
            loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
        } else {
            loadingDialog.requireDialog().show()
        }
    }

    override fun onSuccess(apiResponse: String) {
        homeFragBinding.srlHomeFrag.isRefreshing = false
        homeFragBinding.clHomeAllViews.visibility = View.VISIBLE
    }

    override fun onSuccessPolicy(apiResponse: String) {

    }

    override fun onSocialDrawerData(social: List<SocialData>) {

    }

    override fun onPolicyDrawerData(policy: List<PolicyData>) {
        for (i in policy.indices) {
            if (policy[i].title?.contains("terms & conditions", true) == true) {
                Log.e("termsPolicyLink", " :: ${policy[i].url}")
                notebookPrefs.TermsConditionLink = policy[i].url
            }
        }
    }

    override fun onCartItemAdded(isAdded: Boolean) {
        loadingDialog.dismissAllowingStateLoss()
        myToast.show()
    }

    override fun onSuccessBulkOrderData(bannerResponse: List<Banner>) {
        val sliderAdapter =
            HomeTopSliderAdapter(
                mContext,
                bannerResponse as ArrayList<Banner>,
                object : HomeTopSliderAdapter.BannerSliderListener {
                    override fun onSliderClick(bannerData: Banner) {
                        navController.navigate(R.id.action_homeFrag_to_bulkOrderQuery)
                    }
                })

        bannerList = bannerResponse
        bulkOrderBannerList = bannerResponse
        homeFragBinding.vpBulkOrderQuerySlider.adapter = sliderAdapter
        homeFragBinding.tlImageBulkOrderQueryIndicator.setupWithViewPager(homeFragBinding.vpBulkOrderQuerySlider)
    }

    override fun onFailure(msg: String) {
        homeFragBinding.srlHomeFrag.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
        homeFragBinding.srlHomeFrag.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInternetNotAvailable(msg: String) {
        homeFragBinding.srlHomeFrag.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onUserAccepted(isAccept: Boolean) {
        dashboardVM.deleteUser()
        dashboardVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onSuccessLogout() {
        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onDrawerFaqAboutUsData(faqLink: String, aboutUsLink: String) {
        notebookPrefs.FaqsDynamicLink = faqLink
        Log.e("faqDrawerData", " :: $faqLink :: $aboutUsLink")
    }

    private var upgradeRequire: Int = 0
    override fun onGettingUpgradeCheck(isUpgradeAvail: Int?) {
        Log.e("upgradeValue", " :: $isUpgradeAvail")
        upgradeRequire = isUpgradeAvail ?: 0
        notebookPrefs.primeUserUpgradeAvail = upgradeRequire
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        dashboardVM.deleteUser()
        dashboardVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onFailureCart(msg: String, isAddCart: Boolean) {
        if (isAddCart) {
            errorToastTextView.text = msg
            errorToast.show()
        }

        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onApiFailureCart(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInternetNotAvailableCart(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    internal inner class SliderTimer : TimerTask() {
        override fun run() {
            activity?.let {
                it.runOnUiThread {
                    if (homeFragBinding.vpImageSlider.currentItem < bannerList.size - 1) {
                        homeFragBinding.vpImageSlider.currentItem =
                            homeFragBinding.vpImageSlider.currentItem + 1
                    } else {
                        homeFragBinding.vpImageSlider.currentItem = 0
                    }
                }
            }
        }
    }

    internal inner class SliderTimer1() : TimerTask() {
        override fun run() {
            activity?.let {
                it.runOnUiThread {
                    if (homeFragBinding.vpImageOffersSlider.currentItem < latestOfferList.size - 1) {
                        homeFragBinding.vpImageOffersSlider.currentItem =
                            homeFragBinding.vpImageOffersSlider.currentItem + 1
                    } else {
                        homeFragBinding.vpImageOffersSlider.currentItem = 0
                    }
                }
            }
        }
    }

    internal inner class SliderTimer2() : TimerTask() {
        override fun run() {
            activity?.let {
                it.runOnUiThread {
                    if (homeFragBinding.vpMerchantBenefitsSlider.currentItem < merchantBannerList.size - 1) {
                        homeFragBinding.vpMerchantBenefitsSlider.currentItem =
                            homeFragBinding.vpMerchantBenefitsSlider.currentItem + 1
                    } else {
                        homeFragBinding.vpMerchantBenefitsSlider.currentItem = 0
                    }
                }
            }
        }
    }

    internal inner class SliderTimerForBulkOrder() : TimerTask() {
        override fun run() {
            activity?.let {
                it.runOnUiThread {
                    if (homeFragBinding.vpBulkOrderQuerySlider.currentItem < bulkOrderBannerList.size - 1) {
                        homeFragBinding.vpBulkOrderQuerySlider.currentItem =
                            homeFragBinding.vpBulkOrderQuerySlider.currentItem + 1
                    } else {
                        homeFragBinding.vpBulkOrderQuerySlider.currentItem = 0
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mHandler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(runnable)
        unregisterNetworkChanges()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
        timer1.cancel()
        timer2.cancel()
        timer3.cancel()
    }
}
