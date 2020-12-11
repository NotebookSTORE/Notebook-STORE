package com.notebook.android.ui.myOrder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.ui.myOrder.listener.OrderHistoryListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyOrderVM(
    val orderRepo:MyOrderRepo
) : ViewModel() {

    lateinit var orderHistoryListener: OrderHistoryListener

    //room db function callback....
    fun getUserData() = orderRepo.getUser()

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            orderRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            orderRepo.clearCartTable()
            orderRepo.clearAddressTable()
            orderRepo.clearOrderTable()
            orderRepo.clearFavouriteTable()
        }
    }

    //api callback...
    fun getOrderHistoryFromServer(userID:Int, token:String){
        Coroutines.main {
            try {
                orderHistoryListener.onApiCallStarted()
                orderRepo.clearOrderHistory()
                val orderResponse = orderRepo.getOrderHistoryFromServer(userID, token)
                orderResponse.let {
                    if (it.status == 1){
                        orderHistoryListener.onSuccessResponse(it.cartdata)
                        orderRepo.insertOrderHistory(it.cartdata)
                    }else if(it.status == 2){
                        orderHistoryListener.onInvalidCredential()
                    }else{
                        orderHistoryListener.onFailureResponse(it.msg?:"")
                    }
                }

            }catch (e:ApiException){
                orderHistoryListener.onFailureResponse(e.message!!)
            }catch (e:NoInternetException){
                orderHistoryListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun cancelOrderPolicy(userID: Int,
                          token: String,
                          orderID: String,
                          prodID: Int,
                          reason: String){
        Coroutines.main{
            try {
                orderHistoryListener.onApiCallStarted()

                val orderHistoryData = orderRepo.cancelOrderPolicy(userID, token, orderID,prodID, reason)
                orderHistoryData.let {
                    if(it.status == 1){
                        orderHistoryListener.onSuccessCancelReturn(it.msg?:"")
                    }else if(it.status == 2){
                        orderHistoryListener.onInvalidCredential()
                    }else{
                        orderHistoryListener.onApiFailureResponse(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                orderHistoryListener.onFailureResponse(e.message!!)
            }catch (e: NoInternetException){
                orderHistoryListener.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun returnOrderPolicy( userID: Int,
                           token: String,
                           orderID: String,
                           prodID: Int,
                           reason: String,
                           deliveredDate: String){
        Coroutines.main{
            try {
                orderHistoryListener.onApiCallStarted()
                val orderHistoryData = orderRepo.returnOrderPolicy(userID, token, orderID, prodID, reason, deliveredDate)
                orderHistoryData.let {
                    if(it.status == 1){
                        orderHistoryListener.onSuccessCancelReturn(it.msg?:"")
                    }else if(it.status == 2){
                        orderHistoryListener.onInvalidCredential()
                    }else{
                        orderHistoryListener.onFailureResponse(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                orderHistoryListener.onFailureResponse(e.message!!)
            }catch (e: NoInternetException){
                orderHistoryListener.onFailureResponse(e.message!!)
            }
        }
    }

    fun getAllOrderHistory() = orderRepo.getAllOrderHistory()
}
