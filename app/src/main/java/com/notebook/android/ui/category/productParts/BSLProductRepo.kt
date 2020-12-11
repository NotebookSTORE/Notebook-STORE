package com.notebook.android.ui.category.productParts

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.category.DrawerSubSubCategoryProduct
import com.notebook.android.model.category.HomeCategoryProduct
import com.notebook.android.model.filter.FilterByData
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.model.home.BestSellerProductData
import com.notebook.android.model.home.LatestProductData
import com.notebook.android.model.home.SubCategoryProductData

class BSLProductRepo(
    val notebookApi: NotebookApi,
    val db:NotebookDatabase
) : SafeApiRequest() {

    suspend fun bestSellerViewAllProducts(best:Int) : BestSellerProductData{
        return apiRequest { notebookApi.specifyProductAccToBestSeller(best) }
    }

    suspend fun latestViewAllProducts(latest:Int) : LatestProductData{
        return apiRequest { notebookApi.specifyProductAccLatest(latest) }
    }

  /*  suspend fun getProductAccToFilterByData(filterData: FilterRequestData) : SubCategoryProductData {
        return apiRequest { notebookApi.productFilterWise(filterData) }
    }
*/
    suspend fun getFilter(filterDataFromPage:String, parameter:Int) : FilterByData {
        return apiRequest { notebookApi.filterByData(filterDataFromPage, parameter) }
    }

    suspend fun addProductToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) : CartData {
        return apiRequest { notebookApi.addProductToCart(prodID!!.toString(), userID, token, prodQty!!, updateProd) }
    }
    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }

    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)

    suspend fun getProductSSCategoryWise(ssCategID:Int) : DrawerSubSubCategoryProduct{
        return apiRequest { notebookApi.subSubCategoryWiseProduct(ssCategID)}
    }

    suspend fun insertBSProductIntoDB(bestSellerProd:List<BestSeller>)
            = db.getProductDao().insertAllBestSellerProducts(bestSellerProd)

    suspend fun insertLatestProductIntoDB(latestProd:List<LatestProduct>)
            = db.getProductDao().insertAllLatestProducts(latestProd)

    //insert category or sub sub category into db..
    suspend fun insertSSCategoryProductIntoDB(ssCategProd:List<HomeSubSubCategoryProduct>)
            = db.getProductDao().insertAllHomeSSCategoryProducts(ssCategProd)


    fun getUser() = db.getUserDao().getUser()
    fun getAllLatestProducts() = db.getProductDao().getAllLatestProducts()
    fun getAllBestSellerProducts() = db.getProductDao().getAllBestSellerProducts()

    // get category or sub sub category into db..
    fun getAllSSCategoryProduct() = db.getProductDao().getAllHomeSSCategoryProducts()
    suspend fun clearHomeSSCategoryTableData() = db.getProductDao().clearHomeSSCategoryTable()


    // Data insert into filter by table
    suspend fun insertBrandFilter(brandFilter:List<BrandFilterBy>) = db.getFilterDao().insertAllBrandFilter(brandFilter)
    suspend fun insertColorFilter(colorFilter:List<ColorFilterBy>) = db.getFilterDao().insertColorFilter(colorFilter)
    suspend fun insertDiscountFilter(discountFilter:List<DiscountFilterBy>) = db.getFilterDao().insertAllDiscountFilter(discountFilter)
    suspend fun insertRatingFilter(ratingFilter:List<RatingFilterBy>) = db.getFilterDao().insertAllRatingFilter(ratingFilter)
    suspend fun insertPriceFilter(priceFilter:List<PriceFilterBy>) = db.getFilterDao().insertAllPriceFilter(priceFilter)
    suspend fun insertCouponFilter(couponFilter:List<CouponFilterBy>) = db.getFilterDao().insertAllCouponFilter(couponFilter)

    //clear filter by table
    suspend fun clearBrandFilterTable() = db.getFilterDao().clearBrandFilterByTable()
    suspend fun clearColorFilterTable() = db.getFilterDao().clearColorFilterByTable()
    suspend fun clearRatingFilterTable() = db.getFilterDao().clearRatingFilterByTable()
    suspend fun clearDiscountFilterTable() = db.getFilterDao().clearDiscountFilterByTable()
    suspend fun clearPriceFilterTable() = db.getFilterDao().clearPriceFilterByTable()
    suspend fun clearCouponFilterTable() = db.getFilterDao().clearCouponFilterByTable()
}