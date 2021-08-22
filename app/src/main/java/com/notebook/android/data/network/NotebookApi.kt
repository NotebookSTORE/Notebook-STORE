package com.notebook.android.data.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.notebook.android.BuildConfig
import com.notebook.android.model.AboutUsResponse
import com.notebook.android.model.address.AddAddress
import com.notebook.android.model.address.CountryData
import com.notebook.android.model.address.FetchAddresses
import com.notebook.android.model.address.MakeDefaultAddress
import com.notebook.android.model.autdh.ProfileUpdate
import com.notebook.android.model.auth.*
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartDelete
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.cashfree.CFTokenResponse
import com.notebook.android.model.category.DrawerSubSubCategoryProduct
import com.notebook.android.model.category.HomeCategoryProduct
import com.notebook.android.model.category.SimilarDiscountedProduct
import com.notebook.android.model.coupon.CouponData
import com.notebook.android.model.drawer.DrawerCategroyData
import com.notebook.android.model.drawer.SubSubCategoryData
import com.notebook.android.model.drawerParts.ContactUs
import com.notebook.android.model.drawerParts.Faqs
import com.notebook.android.model.filter.FilterByData
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.model.helpSupport.FeedbackData
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.model.helpSupport.ReportProblem
import com.notebook.android.model.home.*
import com.notebook.android.model.merchant.MerchantBenefitData
import com.notebook.android.model.merchant.PrimeMerchantResponse
import com.notebook.android.model.merchant.RegularMerchantResponse
import com.notebook.android.model.myOrder.CancelOrderData
import com.notebook.android.model.myOrder.MyOrderData
import com.notebook.android.model.myOrder.ReturnOrderData
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.model.orderSummary.PaymentSuccesData
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.productDetail.PincodeData
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.model.productDetail.RatingData
import com.notebook.android.model.productDetail.RatingReviewData
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.model.wallet.WalletAmountResponse
import com.notebook.android.model.wallet.redeem.WalletRedeemHistoryResponse
import com.notebook.android.model.wallet.redeem.WalletRedeemResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface NotebookApi {

    /*Auth part apis here....*/

    @FormUrlEncoded
    @POST("registration")
    suspend fun userRegistration(
        @Field("name") username: String,
        @Field("email") email: String,
        @Field("mobile_number") mobileNumber: String,
        @Field("password") password: String,
        @Field("device_id") deviceID: String,
        @Field("usertype") usertype: Int,
        @Field("referralcode") referralCode: String,
    ): Response<RegistrationResponse>

    /*typeofuser*/

    @FormUrlEncoded
    @POST("otpverify")
    suspend fun otpVerification(
        @Field("mobile_number") mobileNumber: String,
        @Field("otp") otpValue: String
    ): Response<UserData>

    @FormUrlEncoded
    @POST("redeemOnBankDetails")
    suspend fun redeemWalletPoints(
        @Field("user_id") userId: Int,
    ): Response<WalletRedeemResponse>

    @FormUrlEncoded
    @POST("redeemHistory")
    suspend fun fetchRedeemHistory(
        @Field("user_id") userId: Int,
    ): Response<WalletRedeemHistoryResponse>

    @FormUrlEncoded
    @POST("otpverify")
    suspend fun otpVerificationWithEmail(
        @Field("email") email: String,
        @Field("otp") otpValue: String
    ): Response<UserData>

    @FormUrlEncoded
    @POST("postLogin")
    suspend fun postLogin(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String
    ): Response<UserData>

    @FormUrlEncoded
    @POST("updateDeviceToken")
    suspend fun updateDeviceToken(
        @Field("token") password: String,
        @Field("device_id") deviceId: String
    ): Response<Any>

    @FormUrlEncoded
    @POST("mobileloginexist")
    suspend fun mobileloginexist(
        @Field("email") email: String,
        @Field("typeofuser") usertype: Int
    ): Response<UserData>

    @FormUrlEncoded
    @POST("mobilelogin")
    suspend fun mobileSocialLogin(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("device_id") deviceID: String,
        @Field("typeofuser") usertype: Int,
        @Field("mobile_number") mobileNumber: String,
        @Field("profile_image") profileImage: String
    ): Response<RegistrationResponse>


    @FormUrlEncoded
    @POST("mobilelogin")
    suspend fun socialMobileLogin(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("device_id") deviceID: String,
        @Field("profile_image") profileImage: String,
        @Field("typeofuser") usertype: Int,
        @Field("socialid") socialid: String
    ): Response<UserData>

    @FormUrlEncoded
    @POST("forgotpass")
    suspend fun forgotPasswordWithOptions(
        @Field("parameter") parameterValue: String
    ): Response<ForgotPass>

    @FormUrlEncoded
    @POST("changepass")
    suspend fun changePassword(
        @Field("mobile_number") mobNumber: String,
        @Field("password") password: String
    ): Response<ChangePass>

    @FormUrlEncoded
    @POST("changepass")
    suspend fun changePasswordWithEmail(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ChangePass>

    @Multipart
    @POST("profileupdate")
    suspend fun userProfileUpdate(
        @Part("userid") userID: RequestBody,
        @Part("email") email: RequestBody,
        @Part("name") fullname: RequestBody,
        @Part profileImage: MultipartBody.Part,/*("profile_image")*/
        @Part("dob") dob: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("phone") phone: RequestBody
    ): Response<ProfileUpdate>

    @FormUrlEncoded
    @POST("profileupdate")
    suspend fun userProfileStringUpdate(
        @Field("userid") userid: Int,
        @Field("email") email: String,
        @Field("name") fullname: String,
        @Field("profile_image") profileImage: String,
        @Field("dob") dob: String,
        @Field("gender") gender: String,
        @Field("phone") phone: String
    ): Response<ProfileUpdate>

    @FormUrlEncoded
    @POST("profileimageempty")
    suspend fun profileUpdateRemoveImage(
        @Field("email") email: String
    ): Response<ChangePass>

    @FormUrlEncoded
    @POST("logout")
    suspend fun logoutUser(
        @Field("user_id") userID: Int,
        @Field("token") token: String
    ): Response<LogoutUserData>

    @FormUrlEncoded
    @POST("userdetails")
    suspend fun userDetailFromServer(
        @Field("userid") userID: Int,
        @Field("token") token: String
    ): Response<UserData>


    //Dashboard Apis here...

    @GET("subcategorydisplay")
    suspend fun subCategoryDisplayDrawer(): Response<DrawerCategroyData>

    @GET("allsubcategory")
    suspend fun subCategoryData(): Response<SubCategoryData>

    @GET("allcategory")
    suspend fun allCategoryData(): Response<AllCategory>

    @GET("bannerrecord")
    suspend fun bannerSliderData(
        @Query("bannertype") bannertype: Int
    ): Response<BannerData>

    @GET("policies")
    suspend fun policyDataDynamic(
        @Query("policyId") policyID: Int
    ): Response<PolicyData>

    @GET("brand")
    suspend fun brandData(): Response<BrandData>

    @GET("productdetailbenefitinfo")
    suspend fun getProductDetailBenefitInfo(): Response<BenefitProductData>

    @GET("freedelivery")
    suspend fun getFreeDelivery(): Response<FreeDeliveryData>

    @GET("latestoffer")
    suspend fun latestOfferData(): Response<LatestOfferData>

    @GET("homeproduct")
    suspend fun homeLatestORBestSellerProduct(): Response<HomeLatestORBestProducts>

    @GET("filterdata")
    suspend fun filterByData(
        @Query("filter") filterFromPage: String,
        @Query("para") parameter: Int
    ): Response<FilterByData>

    @FormUrlEncoded
    @POST("subsubcategory")
    suspend fun getSubSubCategory(
        @Field("category_id") categID: Int,
        @Field("subcategory_id") subCategID: Int
    ): Response<SubSubCategoryData>

    @FormUrlEncoded
    @POST("cartdelete")
    suspend fun cartItemDelete(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("product_id") productID: String
    ): Response<CartDelete>

    @GET("merchantbanner")
    suspend fun merchantBannerHome(): Response<MerchantBannerData>

    @FormUrlEncoded
    @POST("productsearch")
    suspend fun productSearchData(
        @Field("title") prodTitle: String
    ): Response<SearchProductData>

    @FormUrlEncoded
    @POST("productsubcategorysearch")
    suspend fun productSubCategorySearch(
        @Field("subcategory_id") subCatgoryID: Int
    ): Response<SubCategoryProductData>

    @FormUrlEncoded
    @POST("productcoupon")
    suspend fun productCouponCode(
        @Field("product_id") prodID: String
    ): Response<ChangePass>

    @FormUrlEncoded
    @POST("productdetail")
    suspend fun productDetailData(
        @Field("product_id") prodID: String
    ): Response<ProductDetailData>

    @FormUrlEncoded
    @POST("productdiscount_v2")
    suspend fun productDiscountData(
        @Field("discount") discountValue: Int,
//        @Query("page") pageNumber: Int
    ): Response<DiscountedProdData>

    @GET("pinAvailable")
    suspend fun checkDeliveryAccordingToPincode(
        @Query("delivery_postcode") pincodeValue: String
    ): Response<PincodeData>

    @FormUrlEncoded
    @POST("specifyproduct")
    suspend fun specifyProductAccToBestSeller(
        @Field("best") best: Int
    ): Response<BestSellerProductData>

    @FormUrlEncoded
    @POST("specifyproduct")
    suspend fun specifyProductAccLatest(
        @Field("latest") latestProd: Int
    ): Response<LatestProductData>

    @FormUrlEncoded
    @POST("productcart")
    suspend fun addProductToCart(
        @Field("product_id") prodID: String,
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("quantity") qty: Int,
        @Field("update_product") updateProd: Int
    ): Response<CartData>

    @FormUrlEncoded
    @POST("productcartupdate")
    suspend fun updateCartProduct(
        @Field("product_id") prodID: Int,
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("quantity") qty: Int
    ): Response<CartResponseData>

    @FormUrlEncoded
    @POST("cartdata")
    suspend fun getCartData(
        @Field("user_id") userID: Int,
        @Field("token") token: String
    ): Response<CartResponseData>

    @Headers("Content-Type: application/json")
    @POST("productfilterby_v2")
    suspend fun productFilterWise(
        @Body filterData: FilterRequestData,
        @Query("page") pageNo: Int
    ): Response<FilterProductData>

    @FormUrlEncoded
    @POST("sortproduct")
    suspend fun sortSubCategoryProduct(
        @Field("subcategory_id") subCategoryID: Int,
        @Field("type") sortValue: Int
    ): Response<SubCategoryProductData>

    @GET("getdiscountproduct")
    suspend fun getDiscountedProduct(): Response<SimilarDiscountedProduct>

    @GET("helpsupport")
    suspend fun getHelpSupportData(): Response<HelpSupportData>

    @GET("coupon")
    suspend fun couponDataFromServer(
        @Query("userID") userID: Int,
        @Query("product_id") prodID: String,
        @Query("total_amount") totalAmount: String
    ): Response<CouponData>

    @FormUrlEncoded
    @POST("productcoupon")
    suspend fun productCouponAccToID(
        @Field("product_id") product_id: String
    ): Response<ProductCoupon>

    @Multipart
    @POST("ratingofproduct")
    suspend fun ratingAndReviewProduct(
        @Part("user_id") userID: RequestBody,
        @Part("token") token: RequestBody,
        @Part("product_id") productID: RequestBody,
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("message") message: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ContactUs>

    @FormUrlEncoded
    @POST("ratingofproduct")
    suspend fun ratingAndReviewProductWithoutImage(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("product_id") productID: Int,
        @Field("name") fullName: String,
        @Field("email") email: String,
        @Field("message") message: String,
        @Field("rating") rating: Float,
        @Field("image") image: String
    ): Response<ContactUs>

    @FormUrlEncoded
    @POST("ratingdata")
    suspend fun productRatingData(
        @Field("product_id") product_id: String
    ): Response<RatingData>

    @FormUrlEncoded
    @POST("productrating")
    suspend fun productRatingAllListing(
        @Field("product_id") product_id: String
    ): Response<RatingReviewData>

    @FormUrlEncoded
    @POST("getbulkenquiry")
    suspend fun bulkEnquiryAccToQuantity(
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("productname") prodName: String,
        @Field("email") email: String,
        @Field("quantity") quantity: Int
    ): Response<FeedbackData>


    //home fragment category or subSubCategory Product list apis here.....
    @FormUrlEncoded
    @POST("categorywiseproduct")
    suspend fun categoryWiseProduct(
        @Field("category_id") categID: Int
    ): Response<HomeCategoryProduct>

    @FormUrlEncoded
    @POST("subsubcategorywiseproduct")
    suspend fun subSubCategoryWiseProduct(
        @Field("subsubcategory_id") subSubCategID: Int
    ): Response<DrawerSubSubCategoryProduct>


    // Merchant registration apis here.....
    @Multipart
    @POST("primeregistermerchant")
    suspend fun registerPrimeMerchant(
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("locality") locality: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("identity_detail") aadharDetail: RequestBody,
        @Part("pancardno") panCardNumber: RequestBody,
        @Part identity_image: MultipartBody.Part?,
        @Part pancardimage: MultipartBody.Part?,
        @Part identity_image2: MultipartBody.Part?,
        @Part cancel_cheque: MultipartBody.Part?,
        @Part("accountno") accountno: RequestBody,
        @Part("bankname") bankname: RequestBody,
        @Part("ifsccode") ifsccode: RequestBody,
        @Part("banklocation") banklocation: RequestBody,
        @Part("upi") upiAddress: RequestBody,
        @Part("referralcode") referralcode: RequestBody,
        @Part("device_id") deviceID: RequestBody,
        @Part("registerfor") registerfor: RequestBody,
        @Part("institute_name") instituteName: RequestBody
    ): Response<PrimeMerchantResponse>

    @Multipart
    @POST("primeregistermerchant")
    suspend fun     registerPrimeMerchantUpdate(
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("locality") locality: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("identity_detail") aadharDetail: RequestBody,
        @Part("pancardno") panCardNumber: RequestBody,
        @Part("identity_image") identity_image: RequestBody,
        @Part("pancardimage") identity_image2: RequestBody,
        @Part("identity_image2") pancardimage: RequestBody,
        @Part("cancled_cheque_image") cancel_cheque: RequestBody,
        @Part("accountno") accountno: RequestBody,
        @Part("bankname") bankname: RequestBody,
        @Part("ifsccode") ifsccode: RequestBody,
        @Part("banklocation") banklocation: RequestBody,
        @Part("upi") upiAddress: RequestBody,
        @Part("referralcode") referralcode: RequestBody,
        @Part("device_id") deviceID: RequestBody,
        @Part("registerfor") registerfor: RequestBody,
        @Part("institute_name") instituteName: RequestBody
    ): Response<PrimeMerchantResponse>


    @Multipart
    @POST("primeregistermerchant")
    suspend fun registerPrimeMerchantUpdateOnlyCheque(
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("locality") locality: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("identity_detail") aadharDetail: RequestBody,
        @Part("pancardno") panCardNumber: RequestBody,
        @Part("identity_image") identity_image: RequestBody,
        @Part("pancardimage") identity_image2: RequestBody,
        @Part("identity_image2") pancardimage: RequestBody,
        @Part cancel_cheque: MultipartBody.Part?,
        @Part("accountno") accountno: RequestBody,
        @Part("bankname") bankname: RequestBody,
        @Part("ifsccode") ifsccode: RequestBody,
        @Part("banklocation") banklocation: RequestBody,
        @Part("upi") upiAddress: RequestBody,
        @Part("referralcode") referralcode: RequestBody,
        @Part("device_id") deviceID: RequestBody,
        @Part("registerfor") registerfor: RequestBody,
        @Part("institute_name") instituteName: RequestBody
    ): Response<PrimeMerchantResponse>

    @Multipart
    @POST("registermerchant")
    suspend fun registerRegularMerchant(
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("locality") locality: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("identity_detail") aadharDetail: RequestBody,
        @Part("pancardno") panCardNumber: RequestBody,
        @Part identity_image: MultipartBody.Part?,
        @Part pancardimage: MultipartBody.Part?,
        @Part identity_image2: MultipartBody.Part?,
        @Part("referralcode") referralcode: RequestBody,
        @Part("device_id") deviceID: RequestBody,
        @Part("registerfor") registerfor: RequestBody,
        @Part("institute_name") instituteName: RequestBody
    ): Response<RegularMerchantResponse>

    @Multipart
    @POST("registermerchant")
    suspend fun registerRegularMerchantUpdate(
        @Part("name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("locality") locality: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("identity_detail") aadharDetail: RequestBody,
        @Part("pancardno") panCardNumber: RequestBody,
        @Part("identity_image") identity_image: RequestBody,
        @Part("pancardimage") identity_image2: RequestBody,
        @Part("identity_image2") pancardimage: RequestBody,
        @Part("referralcode") referralcode: RequestBody,
        @Part("device_id") deviceID: RequestBody,
        @Part("registerfor") registerfor: RequestBody,
        @Part("institute_name") instituteName: RequestBody
    ): Response<RegularMerchantResponse>

    @GET("merchentbenifit")
    suspend fun merchantBenifitData(): Response<MerchantBenefitData>

    /*usertype ->
    *
    * null -> normal user
    * 0 -> Regular Merchant
    * 1 -> Prime Merchant
    *
    *
    * typeofUser
    * 0 -> Normal User
    * 1 -> Google User
    * 2 -> Facebook User*/


    /*Drawer part apis here....*/
    @GET("faq")
    suspend fun drawerFaqData(): Response<Faqs>

    @FormUrlEncoded
    @POST("contactus")
    suspend fun contactUsPostToServer(
        @Field("name") fullName: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("yourmessage") yourMsg: String
    ): Response<ContactUs>

    //help and support function here....
    @Multipart
    @POST("reportaproblem")
    suspend fun reportProblemWithImageUpload(
        @Part("user_id") userID: RequestBody,
        @Part("token") token: RequestBody,
        @Part("email") email: RequestBody,
        @Part("name") name: RequestBody,
        @Part imgReport: MultipartBody.Part,
        @Part("message") reportMsg: RequestBody
    ): Response<ReportProblem>

    @FormUrlEncoded
    @POST("app_rating")
    suspend fun appRatingORFeedback(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("email") email: String,
        @Field("name") name: String,
        @Field("rating") ratingValue: Float,
        @Field("message") feedbackMsg: String
    ): Response<FeedbackData>


    //address api here....
    @GET("countries")
    suspend fun getCountryData(): Response<CountryData>

    @FormUrlEncoded
    @POST("fetchaddress")
    suspend fun fetchAddressFromServer(
        @Field("user_id") userID: Int,
        @Field("token") token: String
    ): Response<FetchAddresses>

    @FormUrlEncoded
    @POST("multiaddress")
    suspend fun addMultiAddressToServer(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("street") street: String,
        @Field("locality") locality: String,
        @Field("state") state: String,
        @Field("pincode") pincode: String,
        @Field("country") country: String,
        @Field("city") city: String
    ): Response<AddAddress>

    @FormUrlEncoded
    @POST("multiaddressupdate")
    suspend fun updateMultiAddress(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("street") street: String,
        @Field("locality") locality: String,
        @Field("state") state: String,
        @Field("pincode") pincode: String,
        @Field("country") country: String,
        @Field("city") city: String,
        @Field("multiaddress_id") multiAddressID: Int
    ): Response<FetchAddresses>

    @FormUrlEncoded
    @POST("multiaddressdelete")
    suspend fun deleteMultiAddress(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("multiaddress_id") multiAddressID: Int
    ): Response<FetchAddresses>

    @FormUrlEncoded
    @POST("defaultaddress")
    suspend fun makeDefaultAddress(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("multiaddress_id") multiAddressID: Int
    ): Response<MakeDefaultAddress>

    // payment method api ....

    @Headers("Content-Type: application/json")
    @POST("confirmcart")
    suspend fun orderPlacedWithCOD(
        @Body orderDetails: OrderPaymentDetail
    ): Response<CFTokenResponse>

    @Headers("Content-Type: application/json")
    @POST("addtowallet")
    suspend fun addWalletAmount(
        @Body addWalletData: AddWallet
    ): Response<AddWalletResponse>

    @Headers("Content-Type: application/json")
    @POST("walletamount")
    suspend fun walletAmountGet(
        @Body walletAmountRaw: WalletAmountRaw
    ): Response<WalletAmountResponse>

    @Headers("Content-Type: application/json")
    @POST("paymentsuccess")
    suspend fun paymentSaveToDBAfterPayment(
        @Body paymentRawData: AfterPaymentRawData
    ): Response<PaymentSuccesData>

    @Headers("Content-Type: application/json")
    @POST("walletsuccess")
    suspend fun afterPaymentWalletSuccess(@Body walletSuccess: WalletSuccess): Response<PaymentSuccesData>

    //My order api here...
    @FormUrlEncoded
    @POST("orderhistory")
    suspend fun orderHistoryFromServer(
        @Field("user_id") userID: Int,
        @Field("token") token: String
    ): Response<MyOrderData>

    @FormUrlEncoded
    @POST("returnpolicy")
    suspend fun orderReturnPolicy(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("product_id") productID: Int,
        @Field("orderId") orderID: String,
        @Field("reason") reason: String,
        @Field("delivered_date") deliveredDate: String
    ): Response<ReturnOrderData>

    @FormUrlEncoded
    @POST("cancelpolicy")
    suspend fun orderCancelPolicy(
        @Field("user_id") userID: Int,
        @Field("token") token: String,
        @Field("product_id") productID: Int,
        @Field("orderId") orderID: String,
        @Field("reason") reason: String
    ): Response<CancelOrderData>

    @GET("aboutus")
    suspend fun getAboutUs(): Response<AboutUsResponse>

    companion object {
        const val BASE_URL = "${BuildConfig.SERVER_URL}api/"

        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor,
            context: Context
        ): NotebookApi {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpclient = OkHttpClient.Builder()
            httpclient.addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                val request1 = request.build()
                chain.proceed(request1)
            }
            httpclient.connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            httpclient.readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            httpclient.addInterceptor(logging)
//            httpclient.addInterceptor(ChuckInterceptor(context))
            httpclient.addInterceptor(networkConnectionInterceptor)

            val gsonBuilder = GsonBuilder()
            gsonBuilder.setLenient()
            val gson = gsonBuilder.create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpclient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(NotebookApi::class.java)
        }
    }
}