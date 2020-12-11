package com.notebook.android.ui.myOrder

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.OrderHistory
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.myOrder.CancelOrderData
import com.notebook.android.model.myOrder.MyOrderData
import com.notebook.android.model.myOrder.ReturnOrderData

class MyOrderRepo(
    val db: NotebookDatabase,
    val apiService: NotebookApi
) : SafeApiRequest() {

    suspend fun getOrderHistoryFromServer(userID: Int, token: String): MyOrderData {
        return apiRequest { apiService.orderHistoryFromServer(userID, token) }
    }

    fun getAllOrderHistory() = db.getOrderHistoryDao().getAllOrderHistory()

    suspend fun cancelOrderPolicy(
        userID: Int,
        token: String,
        orderID: String,
        prodID: Int,
        reason: String
    ): CancelOrderData {
        return apiRequest { apiService.orderCancelPolicy(userID, token, prodID, orderID, reason) }
    }

    suspend fun returnOrderPolicy(
        userID: Int,
        token: String,
        orderID: String,
        prodID: Int,
        reason: String,
        deliveredDate: String
    ): ReturnOrderData {
        return apiRequest {
            apiService.orderReturnPolicy(
                userID,
                token,
                prodID,
                orderID,
                reason,
                deliveredDate
            )
        }
    }

    fun getUser() = db.getUserDao().getUser()

    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()

    suspend fun insertOrderHistory(orderHistory: List<OrderHistory>) =
        db.getOrderHistoryDao().insertAllOrderHistory(orderHistory)
    suspend fun clearOrderHistory()  = db.getOrderHistoryDao().clearOrderHistoryTable()

}