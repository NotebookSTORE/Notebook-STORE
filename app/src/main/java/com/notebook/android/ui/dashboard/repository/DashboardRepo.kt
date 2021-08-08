package com.notebook.android.ui.dashboard.repository

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.AboutUsResponse
import com.notebook.android.model.auth.ChangePass
import com.notebook.android.model.auth.LogoutUserData
import com.notebook.android.model.auth.UserData
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.drawer.DrawerCategroyData
import com.notebook.android.model.drawer.SubSubCategoryData
import com.notebook.android.model.home.*
import com.notebook.android.model.home.PolicyData
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.model.wallet.WalletAmountResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DashboardRepo(
    val db: NotebookDatabase,
    val notebookApi: NotebookApi
):SafeApiRequest() {

    suspend fun logoutUserFromServer(userID:Int, token: String) : LogoutUserData {
        return apiRequest { notebookApi.logoutUser(userID, token) }
    }

    suspend fun getUserDataFromServer(userID:Int, token: String) : UserData {
        return apiRequest { notebookApi.userDetailFromServer(userID, token) }
    }

    suspend fun getBannerData(bannerType:Int): BannerData {
        return apiRequest { notebookApi.bannerSliderData(bannerType) }
    }

    suspend fun getPolicyData(policyID:Int): PolicyData {
        return apiRequest { notebookApi.policyDataDynamic(policyID) }
    }

    suspend fun getAllCategory(): AllCategory {
        return apiRequest { notebookApi.allCategoryData() }
    }

    suspend fun getAllSubCategory(): SubCategoryData {
        return apiRequest { notebookApi.subCategoryData() }
    }

    suspend fun getHomeLatestORBestProducts(): HomeLatestORBestProducts {
        return apiRequest { notebookApi.homeLatestORBestSellerProduct() }
    }

    suspend fun getBrandData(): BrandData {
        return apiRequest { notebookApi.brandData() }
    }

    suspend fun getLatestOfferData(): LatestOfferData {
        return apiRequest { notebookApi.latestOfferData()}
    }

    suspend fun getMerchantBannerData(): MerchantBannerData {
        return apiRequest { notebookApi.merchantBannerHome()}
    }

    suspend fun getSubSubCategAccToSubCategory(categID:Int, subCategID:Int): SubSubCategoryData {
        return apiRequest { notebookApi.getSubSubCategory(categID, subCategID)}
    }

    suspend fun addProductToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) : CartData {
        return apiRequest { notebookApi.addProductToCart(prodID!!.toString(), userID, token, prodQty!!, updateProd) }
    }

    //get cart data function...
    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }

    // Drawer operation function here....
    suspend fun getDrawerCategoryData(): DrawerCategroyData {
        return apiRequest { notebookApi.subCategoryDisplayDrawer()}
    }
    suspend fun insertAllDrawerCategData(list:List<DrawerCategory>) = db.getDrawerDao().insertAllDrawerCategoryData(list)
    fun getAllDataFromDrawerDB() = db.getDrawerDao().getDrawerCategoryData()


    //Get data from db api funcion here...
    fun getAllBannerFromDB() = db.getHomeDao().getAllBanner()
    fun getAllBestSellerProductHome() = db.getHomeDao().getBestSellelHome()
    fun getAllLatestProductHome() = db.getHomeDao().getAllLatestProductHome()
    fun getAllBrands() = db.getHomeDao().getAllBrands()
    fun getAllLatestOffers() = db.getHomeDao().getAllLatestOffers()
    fun getAllMerchantBanner() = db.getHomeDao().getAllMerchantBanner()

    fun getAllCategoryFromDB() = db.getCategoryDao().getAllCategory()
    fun getAllSubCategoryFromDB() = db.getCategoryDao().getAllSubCategory()
    fun getSubSubCategoryFromDB(categID:Int, subCategID:Int) = db.getCategoryDao().getAllSubSubCategoryAccToID(categID, subCategID)

    //insert all banner or category here...
    suspend fun insertAllCategoryIntoDB(categList:List<Category>) = db.getCategoryDao().insertAllCategory(categList)
    suspend fun insertAllSubCategoryIntoDB(subCategList:List<SubCategory>) = db.getCategoryDao().insertAllSubCategory(subCategList)
    suspend fun insertSubSubCategoryIntoDB(subSubCategList:List<SubSubCategory>) = db.getCategoryDao().insertAllSubSubCategory(subSubCategList)


    suspend fun insertAllBannerIntoDB(bannerList:List<Banner>) = db.getHomeDao().insertAllBanner(bannerList)
    suspend fun insertAllBestSellerIntoDB(bestSellerList:List<BestSellerHome>) = db.getHomeDao().insertAllBestSellerHome(bestSellerList)
    suspend fun insertAllLatestProductIntoDB(latestProductList:List<LatestProductHome>) = db.getHomeDao().insertAllLatestProductHome(latestProductList)

    suspend fun insertAllBrandsIntoDB(brandList:List<Brand>) = db.getHomeDao().insertAllBrands(brandList)
    suspend fun insertAllLatestOfferIntoDB(latestOfferList:List<LatestOffer>) = db.getHomeDao().insertAllLatestOffers(latestOfferList)
    suspend fun clearLatestOfferDB() = db.getHomeDao().clearLatestOfferHome()

    suspend fun insertMerchantBannerIntoDB(merchantBannerList:List<MerchantBanner>) = db.getHomeDao().insertAllMerchantBanner(merchantBannerList)


    //cart db function here..
    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)
    fun getCartData() = db.getDetailProdDao().getAllCartProduct()
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()
    suspend fun clearBrandTable() = db.getHomeDao().clearBrandData()
    suspend fun clearMerchantBannerTable() = db.getHomeDao().clearMerchantBannerHome()

    fun getUser() = db.getUserDao().getUser()
    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun insertUser(user:User) = db.getUserDao().insertUser(user)
    suspend fun updateUser(user: User) = db.getUserDao().updateUser(user)
    suspend fun deleteBanner() = db.getHomeDao().clearBannerTable()

    suspend fun clearBestSellerHomeProduct() = db.getHomeDao().clearBestSellerHome()
    suspend fun clearLatestProdrHomeProduct() = db.getHomeDao().clearLatestProductHome()

    suspend fun getAboutUs(): AboutUsResponse {
        return apiRequest { notebookApi.getAboutUs() }
    }
}