package com.notebook.android.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.data.db.entities.User
import com.notebook.android.ui.dashboard.listener.DashboardApiListener
import com.notebook.android.ui.dashboard.listener.PolicyDataListener
import com.notebook.android.ui.dashboard.repository.DashboardRepo
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.Constant
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel(
    val dashboardRepo:DashboardRepo
) : ViewModel(){

    lateinit var dashboardApiListener:DashboardApiListener
//    lateinit var policyDataListener : PolicyDataListener

    //get all data from cart db....
    fun getCartData() = dashboardRepo.getCartData()
    fun getUserData() = dashboardRepo.getUser()
    fun getAllBannerData() = dashboardRepo.getAllBannerFromDB()
    fun getAllCategoryDataFromDB() = dashboardRepo.getAllCategoryFromDB()
    fun getAllSubCategoryFromDB() = dashboardRepo.getAllSubCategoryFromDB()

    fun getLatestProductHomeFromDB() = dashboardRepo.getAllLatestProductHome()
    fun getBestSellerProductFromDB() = dashboardRepo.getAllBestSellerProductHome()
    fun getBrandsFromDB() = dashboardRepo.getAllBrands()
    fun getLatestOfferFromDB() = dashboardRepo.getAllLatestOffers()
    fun getMerchantBannerFromDB() = dashboardRepo.getAllMerchantBanner()

    // get Drawer data from db..
    fun getDrawerDataFromDB() = dashboardRepo.getAllDataFromDrawerDB()

    init {
        getDrawerCategoryData()
    }

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            dashboardRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            dashboardRepo.clearCartTable()
            dashboardRepo.clearAddressTable()
            dashboardRepo.clearOrderTable()
            dashboardRepo.clearFavouriteTable()
        }
    }

    fun insertUser(user: User){
        viewModelScope.launch(Dispatchers.IO){
            dashboardRepo.insertUser(user)
        }
    }

    fun updateUser(user: User){
        viewModelScope.launch(Dispatchers.IO){
            dashboardRepo.updateUser(user)
        }
    }

    fun getUserDataFromServer(userID:Int, token: String){
        Coroutines.main{
            try {
//                dashboardApiListener.onApiCallStarted()
                val userLogoutResponse = dashboardRepo.getUserDataFromServer(userID, token)
                userLogoutResponse.let {
                    if(it.status == 1){
                        dashboardApiListener.onGettingUpgradeCheck(it.upgrade_require?.toInt()?:0)
                        dashboardRepo.updateUser(it.user!!)
                    }else if(it.status == 2){
                        dashboardApiListener.onInvalidCredential()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun userLogoutFromServer(userID:Int, token: String){
        Coroutines.main{
            try {
                dashboardApiListener.onApiCallStarted()
                val userLogoutResponse = dashboardRepo.logoutUserFromServer(userID, token)
                userLogoutResponse.let {
                    if(it.status == 1){
                        dashboardApiListener.onSuccessLogout()
                    }else if(it.status == 2){
                        dashboardApiListener.onInvalidCredential()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun getBannerData(bannerType:Int){
        Coroutines.main{
            try {
                dashboardApiListener.onApiCallStarted()
                val bannerResp = dashboardRepo.getBannerData(bannerType)
                bannerResp.let {
                    if(it.status == 1){
                        dashboardRepo.deleteBanner()
                        dashboardRepo.insertAllBannerIntoDB(it.bannerresponse?:ArrayList())
                        getAllCategoryData()
//                        getBulkQueryData(Constant.BANNER_TYPE_BULK_QUERY)
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun getPolicyData(policyID:Int){
        Coroutines.main{
            try {
                dashboardApiListener.onApiCallStarted()
                val bannerResp = dashboardRepo.getPolicyData(policyID)
                bannerResp.let {
                    if(it.status == 1){
                        dashboardApiListener.onSuccessPolicy(it.Description?:"")
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun getBulkQueryData(bannerType:Int){
        Coroutines.main{
            try {
                dashboardApiListener.onApiCallStarted()
                val bannerResp = dashboardRepo.getBannerData(bannerType)
                bannerResp.let {
                    if(it.status == 1){
                        dashboardApiListener.onSuccessBulkOrderData(it.bannerresponse?:ArrayList())
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    //getDrawerData from api...
    fun getDrawerCategoryData(){
        Coroutines.main{
            try {
                val drawerResp = dashboardRepo.getDrawerCategoryData()
                drawerResp.let {
                    if(it.status == 1){
                        dashboardRepo.insertAllDrawerCategData(it.catsub?:ArrayList())
                        dashboardApiListener.onSocialDrawerData(it.social?:ArrayList())
                        dashboardApiListener.onPolicyDrawerData(it.policy?:ArrayList())
                        dashboardApiListener.onDrawerFaqAboutUsData(it.faq?:"", it.aboutUs?:"")
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllCategoryData(){
        Coroutines.main{
            try {
                val categResp = dashboardRepo.getAllCategory()
                categResp.let {
                    if(it.status == 1){
                        dashboardRepo.insertAllCategoryIntoDB(it.category?: ArrayList())
                        getAllSubCategoryData()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllSubCategoryData(){
        Coroutines.main{
            try {
                val categResp = dashboardRepo.getAllSubCategory()
                categResp.let {
                    if(it.status == 1){
                        dashboardRepo.insertAllSubCategoryIntoDB(it.subcategory?: ArrayList())
                        getAllHomeProducts()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllHomeProducts(){
        Coroutines.main{
            try {
                val categResp = dashboardRepo.getHomeLatestORBestProducts()
                categResp.let {
                    if(it.status == 1){
                        dashboardRepo.clearBestSellerHomeProduct()
                        dashboardRepo.clearLatestProdrHomeProduct()
                        dashboardRepo.insertAllBestSellerIntoDB(it.bestseller?: ArrayList())
                        dashboardRepo.insertAllLatestProductIntoDB(it.latestproduct?: ArrayList())
                        getAllBrands()
//                        dashboardApiListener?.onSuccess(it.msg!!)
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllBrands(){
        Coroutines.main{
            try {
                val brandResp = dashboardRepo.getBrandData()
                brandResp.let {
                    if(it.status == 1){
                        dashboardRepo.clearBrandTable()
                        dashboardRepo.insertAllBrandsIntoDB(it.brand?: ArrayList())
                        getAllLatestOffer()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllLatestOffer(){
        Coroutines.main{
            try {
                val brandResp = dashboardRepo.getLatestOfferData()
                brandResp.let {
                    if(it.status == 1){
                        dashboardRepo.clearLatestOfferDB()
                        dashboardRepo.insertAllLatestOfferIntoDB(it.latestoffer?: ArrayList())
                        getAllMerchantBanner()
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    suspend fun getAllMerchantBanner(){
        Coroutines.main{
            try {
                val brandResp = dashboardRepo.getMerchantBannerData()
                brandResp.let {
                    if(it.status == 1){
                        dashboardRepo.clearMerchantBannerTable()
                        dashboardRepo.insertMerchantBannerIntoDB(it.Merchantbanner?: ArrayList())
                    }else{
                        dashboardApiListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {
                dashboardApiListener.onApiCartAddCallStarted()
                val prodResponse = dashboardRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        dashboardApiListener.onCartItemAdded(true)
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        dashboardApiListener.onInvalidCredential()
                    }else{
                        dashboardApiListener.onFailureCart(it.msg?:"", true)
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailureCart(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailableCart(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {
                dashboardApiListener.onApiCartAddCallStarted()
                dashboardRepo.clearCartTable()
                val prodResponse = dashboardRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
                        if(it.cartdata != null){
                            dashboardRepo.insertCartList(it.cartdata!!)
                        }
                        dashboardApiListener.onSuccessLogout()
                    }else if(it.status == 2){
                        dashboardApiListener.onInvalidCredential()
                    }else{
                        dashboardApiListener.onFailureCart(it.msg?:"", false)
                    }
                }
            }catch (e: ApiException){
                dashboardApiListener.onApiFailureCart(e.message!!)
            }catch (e: NoInternetException){
                dashboardApiListener.onInternetNotAvailableCart(e.message!!)
            }
        }
    }
}