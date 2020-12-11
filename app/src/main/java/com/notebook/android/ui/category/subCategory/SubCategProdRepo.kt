package com.notebook.android.ui.category.subCategory

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.category.HomeCategoryProduct
import com.notebook.android.model.filter.FilterByData
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.model.home.SubCategoryProductData

class SubCategProdRepo(
    val db : NotebookDatabase,
    val notebookApi: NotebookApi
) : SafeApiRequest() {

    suspend fun subCategoryProductAccToID(subCategID:Int) : SubCategoryProductData{
        return apiRequest { notebookApi.productSubCategorySearch(subCategID) }
    }

    suspend fun sortSubCategoryProductAccToID(subCategID:Int, sortValue:Int) : SubCategoryProductData{
        return apiRequest { notebookApi.sortSubCategoryProduct(subCategID, sortValue) }
    }

    suspend fun getProductCategoryWise(categID:Int) : HomeCategoryProduct{
        return apiRequest { notebookApi.categoryWiseProduct(categID)}
    }

    /*suspend fun getProductAccToFilterByData(filterData: FilterRequestData) : SubCategoryProductData {
        return apiRequest { notebookApi.productFilterWise(filterData) }
    }*/

    suspend fun getFilter(filterDataFromPage:String, parameter:Int) : FilterByData {
        return apiRequest { notebookApi.filterByData(filterDataFromPage, parameter) }
    }

    suspend fun addProductToCart(userID: Int, token: String, prodID: Int?,
                                 prodQty: Int?, updateProd:Int) : CartData {
        return apiRequest { notebookApi.addProductToCart(prodID!!.toString(), userID, token, prodQty!!, updateProd) }
    }

    //get cart data function...
    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }


    suspend fun insertCategoryProductIntoDB(categProd:List<CategoryProduct>)
            = db.getProductDao().insertAllHomeCategoryProducts(categProd)
    suspend fun clearCategoryTableData() = db.getProductDao().clearCategoryTable()

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


    suspend fun insertSubCategoryProdcutIntoDB(subCategProdList:List<SubCategoryProduct>) = db.getCategoryDao().insertAllSubCategoryProduct(subCategProdList)

    fun getAllSubCategoryProduct() = db.getCategoryDao().getAllSubCategoryProduct()
    fun getAllCategoryProduct() = db.getProductDao().getAllHomeCategoryProducts()

    fun getUser() = db.getUserDao().getUser()
    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)
    suspend fun clearSubCategTable() = db.getCategoryDao().clearSubCategoryProductTable()
}