package com.notebook.android.ui.productDetail

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.coupon.CouponData
import com.notebook.android.model.drawerParts.ContactUs
import com.notebook.android.model.helpSupport.FeedbackData
import com.notebook.android.model.home.BenefitProductData
import com.notebook.android.model.home.DiscountedProdData
import com.notebook.android.model.home.FreeDeliveryData
import com.notebook.android.model.home.ProductCoupon
import com.notebook.android.model.productDetail.PincodeData
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.model.productDetail.RatingData
import com.notebook.android.model.productDetail.RatingReviewData
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProdDetailRepo(
    val db : NotebookDatabase,
     val notebookApi: NotebookApi
) : SafeApiRequest() {

    suspend fun getSimilarDiscountedProducts(discountValue: Int, pageNumber: Int) : DiscountedProdData{
        return apiRequest { notebookApi.productDiscountData(discountValue) }
    }

    suspend fun checkPincodeAvailabilit(pincode:String) : PincodeData{
        return apiRequest { notebookApi.checkDeliveryAccordingToPincode(pincode) }
    }

    suspend fun addProductToCart(userID: Int, token: String, prodID: String?,  prodQty: Int?, updateProd:Int) : CartData{
        return apiRequest { notebookApi.addProductToCart(prodID!!, userID, token, prodQty!!, updateProd) }
    }

    suspend fun ratingProductCallToServerWithoutImage(userID:Int, token:String, prodID: Int, name:String, email:String, rating:Float, image:String, message:String) : ContactUs {
        return apiRequest { notebookApi.ratingAndReviewProductWithoutImage(userID, token, prodID, name, email, message, rating, image) }
    }

    suspend fun ratingProductCallToServer(userID: RequestBody, token:RequestBody, prodID: RequestBody, name:RequestBody, email:RequestBody, rating:RequestBody, image:MultipartBody.Part, message:RequestBody) : ContactUs {
        return apiRequest { notebookApi.ratingAndReviewProduct(userID, token, prodID, name, email, message, rating, image) }
    }

    suspend fun productBulkEnquiry(name:String, phone:String, prodName:String, email:String, quantity:Int) : FeedbackData {
        return apiRequest { notebookApi.bulkEnquiryAccToQuantity(name, phone, prodName, email, quantity) }
    }

    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }

    suspend fun getProductCouponData(prodID:String) : ProductCoupon {
        return apiRequest { notebookApi.productCouponAccToID(prodID) }
    }

    suspend fun productDetailDataFromServer(prodID:String) : ProductDetailData {
        return apiRequest { notebookApi.productDetailData(prodID) }
    }

    suspend fun getApplyCouponData(userID:Int, prodID: String,totalAmount:String) : CouponData {
        return apiRequest { notebookApi.couponDataFromServer(userID, prodID,totalAmount) }
    }

    suspend fun getProductBenefitData() : BenefitProductData {
        return apiRequest { notebookApi.getProductDetailBenefitInfo() }
    }

    suspend fun getFreeDeliveryData() : FreeDeliveryData {
        return apiRequest { notebookApi.getFreeDelivery() }
    }

    suspend fun getProductReviewData(prodID:String) : RatingData {
        return apiRequest { notebookApi.productRatingData(prodID) }
    }

    suspend fun getRatingProdListingData(prodID:String) : RatingReviewData {
        return apiRequest { notebookApi.productRatingAllListing(prodID) }
    }

    suspend fun updateCartProduct(userID: Int, token: String, prodID: Int,  prodQty: Int) : CartResponseData {
        return apiRequest { notebookApi.updateCartProduct(prodID, userID, token, prodQty) }
    }

    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)
    suspend fun insertAllDiscProducts(prodList:List<DiscountedProduct>) = db.getDetailProdDao().insertAllDiscProducts(prodList)
    fun getAllDiscountProducts() = db.getDetailProdDao().getAllDiscountedProducts()
    suspend fun clearDiscountedProductTable() = db.getDetailProdDao().clearDiscountedProductTable()

    suspend fun deleteCartItem(cartID:Int) = db.getDetailProdDao().deleteCartItem(cartID)
    fun getUserData() = db.getUserDao().getUser()
    fun getCartData() = db.getDetailProdDao().getAllCartProduct()

    suspend fun insertAllRatingReviewsData(list:List<RatingReviews>) = db.getDetailProdDao().insertAllRatingReviewsData(list)
    suspend fun clearRatingReviewsTable() = db.getDetailProdDao().clearRatingReviewsTable()
    fun getAllRatingReviewsData() = db.getDetailProdDao().getAllRatingReviewsData()


    //favourites item CRUD
    suspend fun insertFavourites(favItems:Wishlist) = db.getWishlistDao().insertFavProduct(favItems)
    fun getAllFavouriteItems() = db.getWishlistDao().getAllWishlistProducts()
    suspend fun deleteFavItem(favID:Int) = db.getWishlistDao().deleteFavById(favID)

    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun insertCouponDat(couponList:List<CouponApply>) = db.getDetailProdDao().insertAllCouponData(couponList)
    fun getAllCouponDataFromDB() = db.getDetailProdDao().getAllCouponData()
    suspend fun clearCouponTable() = db.getDetailProdDao().clearCouponTable()

    //product detail data function.....
    suspend fun insertProductDataIntoDB(prodDetailData:ProductDetailEntity) =
        db.getProductDao().insertProductDetailData(prodDetailData)
    suspend fun clearProductDetailTable() = db.getProductDao().clearProductDetailData()
    fun getProductDetailData() = db.getProductDao().getProductDetailData()

    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()
}