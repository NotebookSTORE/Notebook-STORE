package com.notebook.android.ui.dashboard.searchProduct

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.SearchProduct
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartResponseData
import com.notebook.android.model.home.SearchProductData

class SearchProdRepo(
    val db:NotebookDatabase,
    val notebookApi: NotebookApi
) : SafeApiRequest() {

    suspend fun getSearchProductResult(title:String) : SearchProductData{
        return apiRequest { notebookApi.productSearchData(title) }
    }

    suspend fun addProductToCart(userID: Int, token: String, prodID: Int?,
                                 prodQty: Int?, updateProd:Int) : CartData {
        return apiRequest { notebookApi.addProductToCart(prodID!!.toString(), userID, token, prodQty!!, updateProd) }
    }

    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }

    fun getUser() = db.getUserDao().getUser()
    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)

    suspend fun insertAllSearchProduct(searchProd:List<SearchProduct>) = db.getCategoryDao().insertAllSearchProduct(searchProd)
    fun getAllSearchProduct() = db.getCategoryDao().getAllSearchProduct()
    suspend fun clearSearchProductTable() = db.getCategoryDao().clearSearchProductTable()

    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()
}