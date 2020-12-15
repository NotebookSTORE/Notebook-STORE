package com.notebook.android.ui.dashboard.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.ActivityState
import com.notebook.android.model.ErrorState
import com.notebook.android.model.ProgressState
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartVM(
        val cartRepo: CartRepo
) : ViewModel() {

    lateinit var cartRespListener: CartResponseListener
    fun getCartData() = cartRepo.getCartDataFromDB()
    fun getUserData() = cartRepo.getUser()
    fun getAllFavouritesData() = cartRepo.getAllFavouriteItems()
    private val _loadFreeDeliveryDataState: MutableLiveData<ActivityState> = MutableLiveData()
    val loadFreeDeliveryDataState: LiveData<ActivityState> = _loadFreeDeliveryDataState

    fun deleteUser() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepo.clearCartTable()
            cartRepo.clearAddressTable()
            cartRepo.clearOrderTable()
            cartRepo.clearFavouriteTable()
        }
    }

    fun deleteCartItem(userID: Int, token: String, prodID: String) {
        Coroutines.main {
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.deleteCartItemFromServer(userID, token, prodID)
                prodResponse.let {
                    if (it.status == 1) {
                        cartRespListener.onCartItemDeleted("Item deleted successfully")
                        cartRepo.deleteCartItem(prodID.toInt())
                        getCartData(userID, token)
                    } else if (it.status == 2) {
                        cartRespListener.onInvalidCredential()
                    } else {
                        cartRespListener.onFailureUpdateORDeleteCart(it.msg ?: "")
                    }
                }
            } catch (e: ApiException) {
                cartRespListener.onApiFailureUpdateORDeleteCart(e.message!!)
            } catch (e: NoInternetException) {
                cartRespListener.onNoInternetAvailableUpdateORDeleteCart(e.message!!)
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: String?, prodQty: Int?, updateProd: Int) {
        Coroutines.main {
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if (it.status == 1) {
                        cartRespListener.onCartProductItemAdded(it.msg!!)
                        getCartData(userID, token)
                    } else if (it.status == 2) {
                        cartRespListener.onInvalidCredential()
                    } else {
                        cartRespListener.onFailure(it.msg ?: "", true)
                    }
                }
            } catch (e: ApiException) {
                cartRespListener.onApiFailure(e.message!!)
            } catch (e: NoInternetException) {
                cartRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun updateCartItem(userID: Int, token: String, prodID: String, prodQty: Int, updateProd: Int) {
        Coroutines.main {
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = cartRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if (it.status == 1) {
                        cartRespListener.onCartProductItemAdded("Cart updated successfully")
                        getCartData(userID, token)
                    } else if (it.status == 2) {
                        cartRespListener.onInvalidCredential()
                    } else {
                        cartRespListener.onFailureUpdateORDeleteCart(it.msg ?: "")
                    }
                }
            } catch (e: ApiException) {
                cartRespListener.onApiFailureUpdateORDeleteCart(e.message!!)
            } catch (e: NoInternetException) {
                cartRespListener.onNoInternetAvailableUpdateORDeleteCart(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main {
            try {
                cartRespListener.onApiCallStarted()
                val prodResponse = cartRepo.getCartData(userID, token)
                prodResponse.let {
                    if (it.status == 1) {
                        if (it.cartdata != null) {
                            cartRespListener.onCartEmpty(false)
                            cartRepo.insertCartList(it.cartdata!!)
                        } else {
                            cartRespListener.onCartEmpty(true)
                        }
                    } else if (it.status == 2) {
                        cartRespListener.onInvalidCredential()
                    } else {
                        cartRespListener.onFailure(it.msg ?: "", false)
                    }
                }
            } catch (e: ApiException) {
                cartRespListener.onApiFailure(e.message!!)
            } catch (e: NoInternetException) {
                cartRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getFreeDeliveryData() {
        viewModelScope.launch {
            _loadFreeDeliveryDataState.value = ProgressState
            try {
                val prodBenefitResp = cartRepo.getFreeDeliveryData()
                prodBenefitResp.let {
                    if (it.status == 1) {
                        _loadFreeDeliveryDataState.value = LoadFreeDeliverySuccessState((it.freedelivery
                                ?: ArrayList()).firstOrNull()?.price ?: 0)
                    } else {
                        _loadFreeDeliveryDataState.value = ErrorState(Exception(it.msg ?: ""))
                    }
                }
            } catch (e: ApiException) {
                _loadFreeDeliveryDataState.value = ErrorState(e)
            } catch (e: NoInternetException) {
                _loadFreeDeliveryDataState.value = ErrorState(e)
            }
        }
    }


    fun deleteFavItemFromDB(favID: Int) {
        Coroutines.main {
            cartRepo.deleteFavItem(favID)
        }
    }

    data class LoadFreeDeliverySuccessState(val freeDeliveryAmount: Int) : ActivityState()
}