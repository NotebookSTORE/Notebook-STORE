package com.notebook.android.ui.myAccount.address

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.cashfree.CFTokenResponse
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.ui.myAccount.address.listener.AddWalletResponseListener
import com.notebook.android.ui.myAccount.address.listener.AddressAddUpdateListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddressVM(
    val addrRepo: AddressRepo
) : ViewModel() {

    /*Theme.MaterialComponents.DayNight.NoActionBar.Bridge*/


    lateinit var addrAddUpdateListener:AddressAddUpdateListener

    //get data function here...
    fun getUserData() = addrRepo.getUser()
    fun getAllCountryDataFromDB() = addrRepo.getAllCountryFromDB()
    fun getAllAddressDataFromDB() = addrRepo.getAllAddressesFromDB()

    fun getCountryFromServer(){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiCountryStarted()
                val countryDataResponse = addrRepo.getCountryDataFromServer()
                countryDataResponse.let {
                    if(it.status == 1){
                        addrRepo.insertAllCountryToDB(it.country?:ArrayList())
                        addrAddUpdateListener.onSuccessCountry()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            addrRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            addrRepo.clearCartTable()
            addrRepo.clearAddressTable()
            addrRepo.clearOrderTable()
            addrRepo.clearFavouriteTable()
        }
    }

    //add address edittext value here from two-way binding....
    /*var edtBuildingStreet:String ?= null
    var edtLocality:String ?= null
    var edtCity:String ?= null
    var edtState:String ?= null
    var edtPincode:String ?= null
    var edtCountry:String ?= null

    fun addOrUpdateAddressClickListener(view: View){
        if(TextUtils.isEmpty(edtBuildingStreet)){
            addrAddUpdateListener.onFailure("Enter building/street here")
        }else if(TextUtils.isEmpty(edtLocality)){
            addrAddUpdateListener.onFailure("Enter locality here")
        }else if(TextUtils.isEmpty(edtCity)){
            addrAddUpdateListener.onFailure("Enter city here")
        }else if(TextUtils.isEmpty(edtState)){
            addrAddUpdateListener.onFailure("Enter state here")
        }else if(TextUtils.isEmpty(edtPincode)){
            addrAddUpdateListener.onFailure("Enter pincode here")
        }else if(TextUtils.isEmpty(edtCountry)){
            addrAddUpdateListener.onFailure("Enter country here")
        }else{

            addAddressToServer(0, ",", edtBuildingStreet!!, edtLocality!!, edtState!!, edtPincode!!, edtCountry!!, edtCity!!)
        }
    }*/

    /*fun showCountryPopup(view:View){
        addrAddUpdateListener.onCallCountryDialogOpen()
    }*/

    fun addAddressToServer(userID:Int, token:String, street:String,
                           locality:String, state:String, pincode:String,
                           country:String, city:String){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiStarted()
                val addAddressResponse = addrRepo.addAddressToServer(userID, token, street, locality,
                    state, pincode, country, city)
                addAddressResponse.let {
                    if(it.status == 1){
                       //do something for response
                        addrAddUpdateListener.onSuccess()
                    }else if(it.status == 2){
                        addrAddUpdateListener.onInvalidCredential()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun updateAddressToServer(userID:Int, token:String, street:String,
                              locality:String, state:String, pincode:String,
                              country:String, city:String, multiAddressID:Int){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiStarted()
                val updateAddressResponse = addrRepo.updateAddressToServer(userID, token, street, locality,
                    state, pincode, country, city, multiAddressID)
                updateAddressResponse.let {
                    if(it.status == 1){
                        //handle update address .....
                        addrAddUpdateListener.onSuccess()
                    }else if(it.status == 2){
                        addrAddUpdateListener.onInvalidCredential()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }


    fun fetchAddressFromServer(userID:Int, token:String){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiStarted()
                val updateAddressResponse = addrRepo.fetchAddressesFromServer(userID, token)
                updateAddressResponse.let {
                    if(it.status == 1){
                        addrRepo.insertAllAddressToDB(it.address?:ArrayList())
                        addrAddUpdateListener.onSuccess()
                    }else if(it.status == 2){
                        addrAddUpdateListener.onInvalidCredential()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun makeDefaultAddress(userID:Int, token:String, multiAddressID:Int){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiStarted()
                val updateAddressResponse = addrRepo.makeDefaultAddressThroughServer(userID, token, multiAddressID)
                updateAddressResponse.let {
                    if(it.status == 1){
                        //handle update address .....
                        addrAddUpdateListener.makeDefaultOrDeleteAddressSuccess()
                        addrRepo.updateUser(it.user)
                    }else if(it.status == 2){
                        addrAddUpdateListener.onInvalidCredential()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun deleteAddressFromServer(userID:Int, token:String, multiAddressID:Int){
        Coroutines.main{
            try {
                addrAddUpdateListener.onApiStarted()
                val updateAddressResponse = addrRepo.deleteAddressThroughServer(userID, token, multiAddressID)
                updateAddressResponse.let {
                    if(it.status == 1){
                        //handle update address .....
                        addrAddUpdateListener.makeDefaultOrDeleteAddressSuccess()
                        addrRepo.deleteAddressData(multiAddressID)
                    }else if(it.status == 2){
                        addrAddUpdateListener.onInvalidCredential()
                    }else{
                        addrAddUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                addrAddUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addrAddUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    /*fun setCountryValue(selectValue: String) {
        edtCountry = selectValue
    }*/
}