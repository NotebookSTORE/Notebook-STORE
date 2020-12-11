package com.notebook.android.ui.dashboard.searchProduct

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchProductVM(
    val searchRepo:SearchProdRepo
) : ViewModel() {

    lateinit var searchProdResponseListener:SearchProdResponseListener
    fun getUserData() = searchRepo.getUser()
    fun getAllSearchProduct() = searchRepo.getAllSearchProduct()

    val email = MutableLiveData<String>()
    // This observer will invoke onEmailChanged() when the user updates the email
    private val emailObserver = Observer<String> { getSearchProdResult(it) }

    init {
        email.observeForever(emailObserver)
    }

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            searchRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            searchRepo.clearCartTable()
            searchRepo.clearAddressTable()
            searchRepo.clearOrderTable()
            searchRepo.clearFavouriteTable()
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {
                searchProdResponseListener.onCartItemAddCallStarted()
                val prodResponse = searchRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        searchProdResponseListener.onCartItemAdded("Item added successfully ")
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        searchProdResponseListener.onInvalidCredential()
                    }else{
                        searchProdResponseListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                searchProdResponseListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                searchProdResponseListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {
                val prodResponse = searchRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
                        if(it.cartdata != null){
                            searchRepo.insertCartList(it.cartdata!!)
                        }
                    }else if(it.status == 2){
                        searchProdResponseListener.onInvalidCredential()
                    }else{
                        searchProdResponseListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                searchProdResponseListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                searchProdResponseListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun searchButtonClickListener(view: View){
        if(email.value?.isNotEmpty() == true){
            getSearchProdResult(email.value!!)
        }
    }

    fun clearSearchTable(){
        Coroutines.main {
            searchRepo.clearSearchProductTable()
        }
    }

    fun getSearchProdResult(title:String){
        if (title.length>1){
            Coroutines.main{
                try {
                    searchProdResponseListener.onApiStarted()
                    searchRepo.clearSearchProductTable()
                    val prodResponse = searchRepo.getSearchProductResult(title)
                    prodResponse.let {
                        if(it.status == 1){
                            if(it.product.isEmpty()){
                                searchProdResponseListener.onSuccessListEmpty(it.product)
                            }else{
                                searchRepo.insertAllSearchProduct(it.product)
                                searchProdResponseListener.onSuccess(it.product)
                            }
                        }else{
                            searchProdResponseListener.onFailure(it.msg?:"")
                        }
                    }
                }catch (e: ApiException){
                    searchProdResponseListener.onApiFailure(e.message!!)
                }catch (e: NoInternetException){
                    searchProdResponseListener.onNoInternetAvailable(e.message!!)
                }
            }
        }
    }

    override fun onCleared() {
        email.removeObserver(emailObserver)
    }
}