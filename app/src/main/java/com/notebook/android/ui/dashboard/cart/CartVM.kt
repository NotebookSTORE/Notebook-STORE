package com.notebook.android.ui.dashboard.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartVM(
    val cartRepo:CartRepo
) : ViewModel() {

    lateinit var cartRespListener:CartResponseListener
    fun getCartData() = cartRepo.getCartDataFromDB()
    fun getUserData() = cartRepo.getUser()
    fun getAllFavouritesData() = cartRepo.getAllFavouriteItems()

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            cartRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            cartRepo.clearCartTable()
            cartRepo.clearAddressTable()
            cartRepo.clearOrderTable()
            cartRepo.clearFavouriteTable()
        }
    }

    fun deleteCartItem(userID:Int, token:String, prodID:String){
        Coroutines.main{
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.deleteCartItemFromServer(userID, token, prodID)
                prodResponse.let {
                    if(it.status == 1){
                        cartRespListener.onCartItemDeleted("Item deleted successfully")
                        cartRepo.deleteCartItem(prodID.toInt())
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        cartRespListener.onInvalidCredential()
                    }else{
                        cartRespListener.onFailureUpdateORDeleteCart(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                cartRespListener.onApiFailureUpdateORDeleteCart(e.message!!)
            }catch (e: NoInternetException){
                cartRespListener.onNoInternetAvailableUpdateORDeleteCart(e.message!!)
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: String?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        cartRespListener.onCartProductItemAdded(it.msg!!)
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        cartRespListener.onInvalidCredential()
                    }else{
                        cartRespListener.onFailure(it.msg?:"", true)
                    }
                }
            }catch (e: ApiException){
                cartRespListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                cartRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun updateCartItem(userID: Int, token: String, prodID: String,  prodQty: Int, updateProd:Int) {
        Coroutines.main{
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.addProductToCart(userID, token, prodID, prodQty,updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        cartRespListener.onCartProductItemAdded("Cart updated successfully")
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        cartRespListener.onInvalidCredential()
                    }else{
                        cartRespListener.onFailureUpdateORDeleteCart(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                cartRespListener.onApiFailureUpdateORDeleteCart(e.message!!)
            }catch (e: NoInternetException){
                cartRespListener.onNoInternetAvailableUpdateORDeleteCart(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {
                cartRespListener.onApiCallStarted()
                val prodResponse = cartRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
                        if(it.cartdata != null){
                            cartRespListener.onCartEmpty(false)
                            cartRepo.insertCartList(it.cartdata!!)
                        }else{
                            cartRespListener.onCartEmpty(true)
                        }
                    }else if(it.status == 2) {
                        cartRespListener.onInvalidCredential()
                    }else {
                        cartRespListener.onFailure(it.msg?:"", false)
                    }
                }
            }catch (e: ApiException){
                cartRespListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                cartRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun deleteFavItemFromDB(favID:Int){
        Coroutines.main {
            cartRepo.deleteFavItem(favID)
        }
    }
}