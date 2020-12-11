package com.notebook.android.ui.dashboard.cart

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cart.CartData
import com.notebook.android.model.cart.CartDelete
import com.notebook.android.model.cart.CartResponseData

class CartRepo(val db : NotebookDatabase,
               val notebookApi: NotebookApi) : SafeApiRequest() {

    suspend fun addProductToCart(userID: Int, token: String, prodID: String?,  prodQty: Int?, updateProd:Int) : CartData {
        return apiRequest { notebookApi.addProductToCart(prodID!!, userID, token, prodQty!!, updateProd) }
    }

    suspend fun getCartData(userID:Int, token: String) : CartResponseData {
        return apiRequest { notebookApi.getCartData(userID, token) }
    }

    suspend fun deleteCartItemFromServer(userID:Int, token:String, prodID: String) : CartDelete {
        return apiRequest { notebookApi.cartItemDelete(userID, token, prodID) }
    }


    suspend fun insertCartList(cartList:List<Cart>) = db.getDetailProdDao().insertAllCartProduct(cartList)
    suspend fun deleteCartItem(cartID:Int) = db.getDetailProdDao().deleteCartItem(cartID)
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()

    fun getUser() = db.getUserDao().getUser()
    suspend fun deleteUser() = db.getUserDao().deleteUser()
    fun getCartDataFromDB() = db.getDetailProdDao().getAllCartProduct()


    //favourites item CRUD
    fun getAllFavouriteItems() = db.getWishlistDao().getAllWishlistProducts()
    suspend fun deleteFavItem(favID:Int) = db.getWishlistDao().deleteFavById(favID)
}