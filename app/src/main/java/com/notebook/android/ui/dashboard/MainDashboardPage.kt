package com.notebook.android.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.max.ecomaxgo.maxpe.view.flight.utility.getUserImageFullPath
import com.notebook.android.BuildConfig
import com.notebook.android.R
import com.notebook.android.adapter.drawer.DrawerCategoryAdapter
import com.notebook.android.adapter.drawer.PolicyDrawerAdapter
import com.notebook.android.adapter.drawer.SocialDrawerAdapter
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.PolicyData
import com.notebook.android.data.db.entities.SocialData
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.ActivityMainDashboardPageBinding
import com.notebook.android.ui.dashboard.factory.DashboardViewModelFactory
import com.notebook.android.ui.dashboard.listener.DashboardApiListener
import com.notebook.android.ui.dashboard.listener.LogoutListener
import com.notebook.android.ui.dashboard.viewmodel.DashboardViewModel
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.LogoutDialogFrag
import com.notebook.android.utility.Constant
import com.notebook.android.utility.Constant.FACEBOOK_LOGIN
import com.notebook.android.utility.Constant.GOOGLE_LOGIN
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainDashboardPage : AppCompatActivity(), View.OnClickListener,
    LogoutListener, KodeinAware, DashboardApiListener {

    override val kodein by kodein()
    private val viewModelFactory:DashboardViewModelFactory by instance()
    private val dashboardVM:DashboardViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)
    }
    private var userData: User?= null
    private val notebookPrefs by lazy {
        NotebookPrefs(this)
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private var cartCount:MutableLiveData<Int> = MutableLiveData()
    private lateinit var mainDashboardBinding:ActivityMainDashboardPageBinding
    private val navController:NavController by lazy {
        Navigation.findNavController(this, R.id.homeNavHostFragment)
    }
    private lateinit var prodIDArgs:NavArgument

    private var mGoogleSigninClient: GoogleSignInClient?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainDashboardBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_dashboard_page)
        NavigationUI.setupWithNavController(mainDashboardBinding.homeBottomNavView, navController)
        mainDashboardBinding.lifecycleOwner = this
        dashboardVM.dashboardApiListener = this

        Log.e("refferalID", " :: ${notebookPrefs.merchantRefferalID}")

        //send deep linking product id to detail page using graph value....
        val prodID = intent.getIntExtra("prodID", -1)
        Log.e("productValue", " :: product ID -> $prodID")
        prodIDArgs = NavArgument.Builder().setDefaultValue(prodID).build()
        val navGraph = navController.navInflater.inflate(R.navigation.home_nav_graph)
        navGraph.addArgument("prodID", prodIDArgs)
        navController.graph = navGraph

        setupHeaderView()
        setupRecyclerView()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSigninClient = GoogleSignIn.getClient(this@MainDashboardPage, gso)

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.e("instance token", " :: $it")
            notebookPrefs.firebaseDeviceID = it
        }

        /*Get method call here..*/
        mainDashboardBinding.imgMenuDrawer.setOnClickListener {
            mainDashboardBinding.homeDrawerLayout.openDrawer(GravityCompat.START)
        }

        dashboardVM.getUserData().observe(this, Observer {user ->
            if(user != null){
                userData = user
                mainDashboardBinding.clCustomNavViewLayout.llLogout.visibility = View.VISIBLE
                if(user.name.isNullOrEmpty()){
                    mainDashboardBinding.clCustomNavViewLayout.tvUserName.text = user.username?:"User Name"
                }else{
                    mainDashboardBinding.clCustomNavViewLayout.tvUserName.text = user.name?:"User Name"
                }

                if(user.phone.isNullOrEmpty()){
                    mainDashboardBinding.clCustomNavViewLayout.tvUserMobile.visibility = View.GONE
                }else{
                    mainDashboardBinding.clCustomNavViewLayout.tvUserMobile.visibility = View.VISIBLE
                    mainDashboardBinding.clCustomNavViewLayout.tvUserMobile.text = "+91 ${user.phone}"
                }

                if(userData!!.usertype == Constant.REGULAR_MERCHANT_TYPE){
                    mainDashboardBinding.clCustomNavViewLayout.imgUserBadge.visibility = View.VISIBLE
                    Glide.with(this).load(R.drawable.regular_badge)
                        .into( mainDashboardBinding.clCustomNavViewLayout.imgUserBadge)
                }else if(userData!!.usertype == Constant.PRIME_MERCHANT_TYPE){
                    mainDashboardBinding.clCustomNavViewLayout.imgUserBadge.visibility = View.VISIBLE
                    Glide.with(this).load(R.drawable.prime_badge)
                        .into( mainDashboardBinding.clCustomNavViewLayout.imgUserBadge)
                }else{
                    mainDashboardBinding.clCustomNavViewLayout.imgUserBadge.visibility = View.GONE
                }

                //cart data check...
//                dashboardVM.getCartData(user.id!!, user.token!!)


                if(notebookPrefs.loginType.equals(GOOGLE_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.user_profile)
                        .into(mainDashboardBinding.clCustomNavViewLayout.imgUserProfile)
                }else if(notebookPrefs.loginType.equals(FACEBOOK_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.user_profile)
                        .into(mainDashboardBinding.clCustomNavViewLayout.imgUserProfile)
                }else{
                    if(!user.profile_image.isNullOrEmpty()){
                        Glide.with(this)
                            .load(getUserImageFullPath(user.profile_image?:""))
                            .placeholder(R.drawable.user_profile)
                            .into(mainDashboardBinding.clCustomNavViewLayout.imgUserProfile)
                    }
                }
            }else{
                userData = null
                mainDashboardBinding.clCustomNavViewLayout.imgUserBadge.visibility = View.GONE
                mainDashboardBinding.clCustomNavViewLayout.tvUserName.text = "User Name"
                mainDashboardBinding.clCustomNavViewLayout.tvUserMobile.text = "+91 97xxxxxx89"
                mainDashboardBinding.clCustomNavViewLayout.imgUserProfile.setImageResource(R.drawable.user_profile)
                mainDashboardBinding.clCustomNavViewLayout.llLogout.visibility = View.GONE
            }
        })


        //Get cart item count...
        dashboardVM.getCartData().observe(this, Observer {
            if(it.isNotEmpty()){
                cartCount.value = it.size
                var totalCartItem=0
                for(cartItem in it){
                    totalCartItem = totalCartItem.plus(cartItem.cartquantity.toInt())
                }
                mainDashboardBinding.homeBottomNavView.getOrCreateBadge(R.id.cartFrag).apply {
                    backgroundColor = Color.RED
                    badgeTextColor = Color.WHITE
                    maxCharacterCount = 3
                    number = totalCartItem
                    isVisible = true
                }
            }else if(it?.isEmpty() == true){
                cartCount.value = 0
                mainDashboardBinding.homeBottomNavView.getOrCreateBadge(R.id.cartFrag).apply {
                    backgroundColor = Color.RED
                    badgeTextColor = Color.WHITE
                    maxCharacterCount = 3
                    number = 0
                    isVisible = false
                }
            }

        })

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val title = if (destination.label.isNullOrEmpty()) {
                "Home"
            } else {
                destination.label
            }
            Log.e("Title Of Fragment", " :: $title")
            setupToolbarTitle(title.toString())
        }

        dashboardVM.getDrawerDataFromDB().observe(this, Observer {
            Log.e("drawer size", " :: ${it.size}")

            val drawerCategoryAdapter = DrawerCategoryAdapter(this, it,
                object : DrawerCategoryAdapter.CategoryDataListener{
                override fun getSubSubCategoryData(subCategID: Int, subCategTitle:String) {
                    Log.e("Sub sub category ID", " :: $subCategID :: $subCategTitle")
                    val bundle = Bundle()
                    bundle.putInt("SubCategID", subCategID)
                    bundle.putString("SubCategTitle", subCategTitle)
                    navController.navigate(R.id.subSubCategoryWiseProductFrag, bundle)
                    mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                }
            })
            mainDashboardBinding.clCustomNavViewLayout.recViewDrawerSubCategory.adapter = drawerCategoryAdapter
        })

        mainDashboardBinding.homeBottomNavView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.myAccountFrag -> {
                    if(userData == null){
                        navController.navigate(R.id.loginFrag)
                    }else{
                        navController.navigate(R.id.myAccountFrag)
                    }

                }

                R.id.homeFrag -> {
                    if(navController.getBackStackEntry(R.id.homeFrag).destination.id == R.id.homeFrag){
                        navController.popBackStack(R.id.homeFrag, false)
                    }else{
                        navController.navigate(R.id.homeFrag)
                    }
                }

                R.id.orderFrag -> {
                    navController.navigate(R.id.orderFrag)
                }

                R.id.cartFrag -> {
                    navController.navigate(R.id.cartFrag)
                }

                R.id.wishlistFrag -> {
                    navController.navigate(R.id.wishlistFrag)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if(navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        notebookPrefs.FaqsDynamicLink = faqsLink
    }

    private fun setupRecyclerView(){
        val layoutManagerMenu = LinearLayoutManager(this)
        mainDashboardBinding.clCustomNavViewLayout.recViewDrawerSubCategory.layoutManager = layoutManagerMenu
        mainDashboardBinding.clCustomNavViewLayout.recViewDrawerSubCategory.itemAnimator = DefaultItemAnimator()
        mainDashboardBinding.clCustomNavViewLayout.recViewDrawerSubCategory.hasFixedSize()

        val layoutManagerSocialLink = LinearLayoutManager(this)
        mainDashboardBinding.clCustomNavViewLayout.recViewSocialLinks.apply {
            layoutManager = layoutManagerSocialLink
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }

        //policy recycler view initialize here....
        val layoutManagerPolicy = LinearLayoutManager(this)
        mainDashboardBinding.clCustomNavViewLayout.recViewPolicyPart.apply {
            layoutManager = layoutManagerPolicy
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    private fun setupToolbarTitle(title:String){

        if (title.equals("home",true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.VISIBLE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        }else if (title.equals("Cart", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
            mainDashboardBinding.tvCustomTitle.text = title

            mainDashboardBinding.homeBottomNavView.getOrCreateBadge(R.id.cartFrag).apply {
                backgroundColor = Color.RED
                badgeTextColor = Color.WHITE
                maxCharacterCount = 3
                number = 0
                isVisible = false
            }
        }else if (title.equals("My order", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
            mainDashboardBinding.tvCustomTitle.text = title
        }else if (title.equals("My Account", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
            mainDashboardBinding.tvCustomTitle.text = title
        }else if (title.equals("Merchant Profile", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Merchant Analytics", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Add Details", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("My Wallet", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Redeem History", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Select Bank", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Payment Method", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Help & Support", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Saved Address", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Payment", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("My Wishlist", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
            mainDashboardBinding.tvCustomTitle.text = title
        }else if (title.equals("Apply Coupon", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Add Card", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Offer Web link", true)){
            lightToolbarWithTitle("Offers")
        }else if (title.equals("Add Address", true)){
            subCategTitle.observe(this, Observer {
                lightToolbarWithTitle(it?:"")
            })
        }else if (title.equals("Merchant Prime Details", true)){
            darkToolbarWithTitle()
        }else if (title.equals("Merchant Regular Details", true)){
            darkToolbarWithTitle()
        }else if (title.equals("Prime Merchant", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Regular Merchant", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Merchant Details", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Identity Proof Upload", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else if (title.equals("Pan Card Upload", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else if (title.equals("Merchant Benefits", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Contact Us", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Order Summary", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Request Return", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("About Us", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("FAQS", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Policy", true)){
            subCategTitle.observe(this, Observer {
                lightToolbarWithTitle(it?:"")
            })
        }else if (title.equals("Feedback", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Report a Problem", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Search Product", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        }else if (title.equals("MerchantViewSummaryWebViewPage", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        }else if (title.equals("Sub Category Product", true)){
//            lightToolbarWithTitle(title?:"")
            subCategTitle.observe(this, Observer {
                lightToolbarWithTitle(it?:"")
            })
        }else if (title.equals("Category Product", true)){
//            lightToolbarWithTitle(title?:"")
            subCategTitle.observe(this, Observer {
                lightToolbarWithTitle(it?:"")
            })
        }else if (title.equals("SubSubCategory Product", true)){
//            lightToolbarWithTitle(title?:"")
            subCategTitle.observe(this, Observer {
                lightToolbarWithTitle(it?:"")
            })
        }else if (title.equals("Latest Products", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("Best Seller", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Product Details", true)){
            lightToolbarWithTitle(title)
        } else  if (title.equals("Zoomable View", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else  if (title.equals("Similar Discount Product", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Order Summary", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Review Product", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Reviews", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Bulk Query", true)){
            lightToolbarWithTitle(title)
        }else  if (title.equals("Filter By", true)){
            lightToolbarWithTitle(title)
        }else if (title.equals("login", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else if (title.equals("PaymentCODSuccessScreen", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else if (title.equals("sign up", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        } else if (title.equals("SocialLoginVerify", true)){
            mainDashboardBinding.cvMainNavigationAppBar.visibility = View.GONE
            mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
            mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        }else  if (title.equals("Reset Password", true)){
            lightToolbarWithTitle(title)
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        }else  if (title.equals("Reset New Password", true)){
            lightToolbarWithTitle("Reset Password")
            mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        }
    }

    private fun setupHeaderView(){
       mainDashboardBinding.clCustomNavViewLayout.llNavHome.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llShareApp.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llFaqs.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llAboutUs.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llContactUs.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llPolicies.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.llLogout.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.clUserProfile.setOnClickListener(this)

        /*mainDashboardBinding.clCustomNavViewLayout.tvTermsCondition.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.tvReturnPolicy.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.tvRefundPolicy.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.tvCancelPolicy.setOnClickListener(this)
        mainDashboardBinding.clCustomNavViewLayout.tvPrivacyPolicy.setOnClickListener(this)*/
        mainDashboardBinding.clCustomNavViewLayout.llSocialLinks.setOnClickListener(this)
        mainDashboardBinding.imgBackToPrevious.setOnClickListener(this)
    }

    private var isPolicyClick = false
    private var isSocialLinkClick= false
    override fun onClick(v: View?) {
        when(v){

            mainDashboardBinding.imgBackToPrevious ->{
                navController.navigateUp()
            }

            mainDashboardBinding.clCustomNavViewLayout.clUserProfile -> {
                if(userData == null){
                    navController.navigate(R.id.loginFrag)
                }else{
                    navController.navigate(R.id.myAccountFrag)
                }
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }

            mainDashboardBinding.clCustomNavViewLayout.llNavHome -> {
//                showErrorToast(this, "Your Error Message")
//                navController.popBackStack()
//                navController.navigate(R.id.homeFrag)
                navController.popBackStack(R.id.homeFrag, false)
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }

            mainDashboardBinding.clCustomNavViewLayout.llShareApp -> {
                shareApp()
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }

            mainDashboardBinding.clCustomNavViewLayout.llFaqs -> {
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                val bundle = Bundle()
                bundle.putString("faqLink", faqsLink)
                notebookPrefs.FaqsDynamicLink = faqsLink
                navController.navigate(R.id.faqsFrag, bundle)
            }

            mainDashboardBinding.clCustomNavViewLayout.llAboutUs -> {
                val bundleAboutUs = Bundle()
                bundleAboutUs.putString("aboutUsLink", aboutsUsLink)
                navController.navigate(R.id.aboutUsFrag, bundleAboutUs)
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }

            mainDashboardBinding.clCustomNavViewLayout.llPolicies -> {

                if(isPolicyClick){
                    isPolicyClick = false
                    mainDashboardBinding.clCustomNavViewLayout.imgPolicyArrow.setImageResource(R.drawable.ic_categoy_right_arrow)
                    mainDashboardBinding.clCustomNavViewLayout.clPolicyContainer.visibility = View.GONE
                }else{
                    isPolicyClick = true
                    mainDashboardBinding.clCustomNavViewLayout.imgPolicyArrow.setImageResource(R.drawable.ic_category_down_arrow)
                    mainDashboardBinding.clCustomNavViewLayout.clPolicyContainer.visibility = View.VISIBLE
                }
            }

            mainDashboardBinding.clCustomNavViewLayout.llContactUs -> {
                navController.navigate(R.id.contactUsFrag)
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }

            mainDashboardBinding.clCustomNavViewLayout.llSocialLinks -> {
                if(isSocialLinkClick){
                    isSocialLinkClick = false
                    mainDashboardBinding.clCustomNavViewLayout.imgSocialArrow.setImageResource(R.drawable.ic_categoy_right_arrow)
                    mainDashboardBinding.clCustomNavViewLayout.recViewSocialLinks.visibility = View.GONE
                }else{
                    isSocialLinkClick = true
                    mainDashboardBinding.clCustomNavViewLayout.imgSocialArrow.setImageResource(R.drawable.ic_category_down_arrow)
                    mainDashboardBinding.clCustomNavViewLayout.recViewSocialLinks.visibility = View.VISIBLE
                }
            }

            mainDashboardBinding.clCustomNavViewLayout.llLogout -> {
                val logoutDialog = LogoutDialogFrag()
                logoutDialog.isCancelable = false
                logoutDialog.setLogoutListener(this)
                logoutDialog.show(supportFragmentManager, "Show Logout Dialog")
                mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun shareApp(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Notebook Store")
            var shareMessage = "Notebook STORE Presenting One of the Best E-Commerce Platform " +
                    "of India where you can get Best Quality Products of Stationary, Books & " +
                    "Educational Toys. As well as have a Bright Opportunity to Make Your Career " +
                    "With Notebook STORE  By Generating Active & Passive Income with its Unique " +
                    "Referral Model.\n" +
                    "\n" +
                    "Click for More Detail \n" +
                    "http://bit.ly/2VfxSMi\n" +
                    "\n" +
                    "Download The App Now\n\n"
            shareMessage = "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share with..."))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    override fun logoutListener() {
        dashboardVM.userLogoutFromServer(userData!!.id, userData!!.token!!)
    }

    private fun darkToolbarWithTitle(){
        mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
        mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
        mainDashboardBinding.homeBottomNavView.visibility = View.GONE
        mainDashboardBinding.tvCustomTitle.text = "Merchant Details"
        mainDashboardBinding.clCustomHomeToolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        mainDashboardBinding.tvCustomTitle.setTextColor(resources.getColor(R.color.colorWhite))
        mainDashboardBinding.imgBackToPrevious.setColorFilter(
            ContextCompat.getColor(this, R.color.colorWhite))
    }

    private fun lightToolbarWithTitle(title:String){
        mainDashboardBinding.cvMainNavigationAppBar.visibility = View.VISIBLE
        mainDashboardBinding.clMainDrawerLayout.visibility = View.GONE
        mainDashboardBinding.homeBottomNavView.visibility = View.VISIBLE
        mainDashboardBinding.tvCustomTitle.text = title
        mainDashboardBinding.clCustomHomeToolbar.setBackgroundColor(resources.getColor(R.color.colorWhite))
        mainDashboardBinding.clCustomHomeToolbar
        mainDashboardBinding.tvCustomTitle.setTextColor(resources.getColor(R.color.colorLightBlack))
        mainDashboardBinding.imgBackToPrevious.setColorFilter(
            ContextCompat.getColor(this, R.color.colorLightBlack))
    }

    val subCategTitle = MutableLiveData<String>()
    fun setSubCategoryTitle(title:String){
        Log.e("Sub category title", " :: $title")
        subCategTitle.value = title
    }

    override fun onApiCallStarted() {
        loadingDialog.show(supportFragmentManager, "Show loading dialog")
    }

    override fun onApiCartAddCallStarted() {

    }

    override fun onSuccess(apiResponse: String) {

    }

    override fun onSuccessPolicy(apiResponse: String) {

    }

    override fun onCartItemAdded(isAdded: Boolean) {
    }


    override fun onSuccessBulkOrderData(bannerResponse: List<Banner>) {

    }

    override fun onFailure(msg: String) {
        loadingDialog.show(supportFragmentManager, "Show loading dialog")
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.show(supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessLogout() {
        loadingDialog.dismissAllowingStateLoss()
        dashboardVM.deleteUser()
        dashboardVM.clearCartTableFromDB()
        notebookPrefs.defaultAddr = ""
        notebookPrefs.defaultAddrModal = ""
        notebookPrefs.clearPreference()

        Log.e("loginType", " :: ${notebookPrefs.loginType}")
        if(notebookPrefs.loginType.equals(GOOGLE_LOGIN, true)){
            logoutGoogleLogin()
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginType.equals(FACEBOOK_LOGIN, true)){
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
//            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginTypeOnImageUpdated.equals(Constant.GOOGLE_LOGIN, true)){
            logoutGoogleLogin()
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else if(notebookPrefs.loginTypeOnImageUpdated.equals(FACEBOOK_LOGIN, true)){
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
//            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
            Log.e("loginType", " :: ${notebookPrefs.loginType}")
        }else{
//            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
        }
    }

    private fun logoutGoogleLogin() {
        val fAuth = FirebaseAuth.getInstance()
        fAuth.signOut()
//        mGoogleSigninClient?.revokeAccess()?.addOnCompleteListener(mActivity) {}
        mGoogleSigninClient?.signOut()?.addOnCompleteListener(this@MainDashboardPage) {
//            navController.popBackStack()
            navController.navigate(R.id.loginFrag)
        }
    }

    override fun onGettingUpgradeCheck(isUpgradeAvail: Int?) {

    }

    override fun onInternetNotAvailable(msg: String) {
        loadingDialog.show(supportFragmentManager, "Show loading dialog")
    }

    override fun onSocialDrawerData(social: List<SocialData>) {
        val socialAdapter = SocialDrawerAdapter(this, social,
            object : SocialDrawerAdapter.SocialDataListener{
                override fun showSocialPage(url: String) {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                    mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                }
            })
        mainDashboardBinding.clCustomNavViewLayout.recViewSocialLinks.adapter = socialAdapter
    }

    override fun onPolicyDrawerData(policy: List<PolicyData>) {
        for(i in policy.indices){
            if (policy[i].title?.contains("terms & conditions", true) == true){
                Log.e("termsPolicyLink", " :: ${policy[i].url}")
                notebookPrefs.TermsConditionLink = policy[i].url
            }
        }

        val policyAdapter = PolicyDrawerAdapter(this, policy,
            object : PolicyDrawerAdapter.PolicyDataListener{
                override fun showPolicyPage(policyID: String, title: String) {
                    val bundle = Bundle()
                    bundle.putString("policyLink", policyID)
                    if (title.contains("terms & conditions", true)){
                        Log.e("termsPolicyTitle", " :: $policyID")
                        notebookPrefs.TermsConditionLink = policyID
                    }
                    navController.navigate(R.id.policyPart, bundle)
                    setSubCategoryTitle(title)
                    mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                }
            })
        mainDashboardBinding.clCustomNavViewLayout.recViewPolicyPart.adapter = policyAdapter
    }

    var aboutsUsLink:String ?= null
    var faqsLink:String ?= null
    override fun onDrawerFaqAboutUsData(faqLink: String, aboutUsLink: String) {
        faqsLink = faqLink
        aboutsUsLink = aboutUsLink
        notebookPrefs.FaqsDynamicLink = faqLink
        Log.e("faqDrawerData", " :: $faqLink :: $aboutUsLink")
    }

    override fun onInvalidCredential() {
        notebookPrefs.clearPreference()
        dashboardVM.deleteUser()
        dashboardVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onFailureCart(msg: String, isAddCart:Boolean) {

    }

    override fun onApiFailureCart(msg: String) {

    }

    override fun onInternetNotAvailableCart(msg: String) {

    }

    override fun onBackPressed() {
        if(mainDashboardBinding.homeDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainDashboardBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
        }else if(navController.currentBackStackEntry!!.destination.id.equals(R.id.myAccountFrag)){
            navController.popBackStack(R.id.homeFrag, false)
        }else if(navController.currentBackStackEntry!!.destination.id.equals(R.id.wishlistFrag)){
            navController.popBackStack(R.id.homeFrag, false)
        }else if(navController.currentBackStackEntry!!.destination.id.equals(R.id.cartFrag)){
            navController.popBackStack(R.id.homeFrag, false)
        }else if(navController.currentBackStackEntry!!.destination.id.equals(R.id.orderFrag)){
            navController.popBackStack(R.id.homeFrag, false)
        }else{
            super.onBackPressed()
        }
    }
}