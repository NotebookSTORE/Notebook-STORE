package com.notebook.android.ui.myAccount.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.ui.dashboard.listener.UserProfileUpdateListener
import com.notebook.android.ui.myAccount.address.listener.AddWalletResponseListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileVM(
    val profileRepo:ProfileRepo
):ViewModel() {

    lateinit var profileUpdateListener: UserProfileUpdateListener
    lateinit var addWalletListener: AddWalletResponseListener
    var profileImageRemoveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getUserData() = profileRepo.getUser()
    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.clearCartTable()
            profileRepo.clearAddressTable()
            profileRepo.clearOrderTable()
            profileRepo.clearFavouriteTable()
        }
    }

    fun profileUpdate(userID: RequestBody, email: RequestBody, fullname: RequestBody,
                      profileImg: MultipartBody.Part,
                      dob: RequestBody, gender: RequestBody, mobile:RequestBody){
        Coroutines.main{
            try {
                profileUpdateListener.onApiCallStarted()
                val userProfileUpdateResp = profileRepo.userProfileUpdate(userID, email, fullname, profileImg,
                    dob, gender, mobile)
                userProfileUpdateResp.let {
                    if(it.status == 1){
                        Log.e("user", " :: ${it.userverifieddata?.phone} " +
                                ":: ${it.userverifieddata?.profile_image} :: userID -> ${it.userverifieddata?.id}")
                        profileRepo.updateUser(it.user!!)
                        profileUpdateListener.onSuccess(it.user!!)
                    }else if(it.status == 2){
                        profileUpdateListener.otpVerifyWhenProfileUpdate(it.userverifieddata!!.otp)
                    }else{
                        profileUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun profileStringUpdate(userID: Int, email: String, fullname: String,
                            profileImg: String, dob: String, gender: String, mobile:String){
        Coroutines.main{
            try {
                profileUpdateListener.onApiCallStarted()
                val userProfileUpdateResp = profileRepo.userProfileStringUpdate(userID, email, fullname,
                    profileImg, dob, gender, mobile)
                userProfileUpdateResp.let {
                    if(it.status == 1){
                        Log.e("user", " :: ${it.userverifieddata?.phone} :: " +
                                "${it.userverifieddata?.profile_image} :: userID -> ${it.userverifieddata?.id}")
                        profileRepo.updateUser(it.user!!)
                        profileUpdateListener.onSuccess(it.user!!)
                    }else if(it.status == 2){
                        profileUpdateListener.otpVerifyWhenProfileUpdate(it.userverifieddata!!.otp)
                    }else{
                        profileUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun profileImageRemove(email: String){
        Coroutines.main{
            try {
                val userProfileUpdateResp = profileRepo.userProfileImageRemove(email)
                userProfileUpdateResp.let {
                    if(it.status == 1){
                        profileImageRemoveData.value = true
                    }else{
                        profileUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun verifyOtp(mobNumber:String, otpValue:String){
        Coroutines.main{
            try {
                profileUpdateListener.onApiCallOtpVerifyStarted()
                val otpVerifyResponse = profileRepo.otpVerifyApi(mobNumber, otpValue)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        if(it.user != null){
                            profileRepo.updateUser(it.user!!)
                        }
                        profileUpdateListener.onOtpSuccess(it.user)
                    }else{
                        profileUpdateListener.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    var cfTokenObserver:MutableLiveData<AddWalletResponse> = MutableLiveData()

    fun addWalletFromServer(addWalletData: AddWallet){
        Coroutines.main{
            try {
                addWalletListener.onApiCallStarted()
                val addAddressResponse = profileRepo.addWalletAmountFromGateway(addWalletData)
                addAddressResponse.let {
                    if (it.status == 1){
                        cfTokenObserver.value = addAddressResponse
                        addWalletListener.onSuccessCFToken()
                    }else{
                        addWalletListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                addWalletListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addWalletListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun userLogoutFromServer(userID:Int, token: String){
        Coroutines.main{
            try {
                profileUpdateListener.onApiCallStarted()
                val userLogoutResponse = profileRepo.logoutUserFromServer(userID, token)
                userLogoutResponse.let {
                    if(it.status == 1){
                        profileUpdateListener.onSuccessLogout()
                    }else if(it.status == 2){
                        profileUpdateListener.onInvalidCredential()
                    } else{
                        profileUpdateListener.onFailure(it.msg)
                    }
                }
            }catch (e: ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun walletSuccessAfterAddFromServer(walletSuccessRaw:WalletSuccess, amount:Float, txtMsg:String){
        Coroutines.main{
            try {
                addWalletListener.onApiCallStarted()
                val userLogoutResponse = profileRepo.afterAddWalletSuccessFromServer(walletSuccessRaw)
                userLogoutResponse.let {
                    if(it.status == 1){
                        addWalletListener.onSuccess(walletSuccessRaw.orderId, walletSuccessRaw.status, amount, txtMsg)
                    }else if(it.status == 2){
                        addWalletListener.onInvalidCredential()
                    }else{
                        addWalletListener.onWalletFailure(walletSuccessRaw.orderId, walletSuccessRaw.status, amount, txtMsg)
                    }
                }
            }catch (e: ApiException){
                addWalletListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                addWalletListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getWalletAmountFromServer(walletAmountRaw: WalletAmountRaw){
        Coroutines.main{
            try {
                profileUpdateListener.onApiCallStarted()
                val walletAmountResponse = profileRepo.getWalletAmountFromServer(walletAmountRaw)
                walletAmountResponse.let {
                    if(it.status == 1){
                        profileUpdateListener.walletAmount(it.amount?:"")
                    }else if(it.status == 2){
                        profileUpdateListener.onInvalidCredential()
                    }else{
                        profileUpdateListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                profileUpdateListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                profileUpdateListener.onNoInternetAvailable(e.message!!)
            }
        }
    }
}