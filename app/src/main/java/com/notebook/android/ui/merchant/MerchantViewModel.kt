package com.notebook.android.ui.merchant

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.User
import com.notebook.android.ui.merchant.responseListener.MerchantBannerListener
import com.notebook.android.ui.merchant.responseListener.MerchantBenefitListener
import com.notebook.android.ui.merchant.responseListener.PrimeRegisterRespListener
import com.notebook.android.ui.merchant.responseListener.RegularRegisterRespListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MerchantViewModel(
    val merchantRepo:MerchantRepo
) : ViewModel() {

    lateinit var primeRespListener:PrimeRegisterRespListener
    lateinit var regularRespListener:RegularRegisterRespListener
    lateinit var merchantBenefitListener:MerchantBenefitListener

    fun getUserData() = merchantRepo.getUser()
    fun getMerchantBenefitData(merchType:String) = merchantRepo.getMerchantBenefitDataAccToType(merchType)

    fun getBannerData(bannerType:Int){
        Coroutines.main{
            try {
                merchantBenefitListener.onApiCallStarted()
                val bannerResp = merchantRepo.getBannerData(bannerType)
                bannerResp.let {
                    if(it.status == 1){
                        merchantBenefitListener.onSuccessBannerResponse(it.bannerresponse?:ArrayList())
                    }else{
                        merchantBenefitListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                merchantBenefitListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                merchantBenefitListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun fetchAddressFromServer(userID:Int, token:String){
        Coroutines.main{
            try {
                merchantBenefitListener.onApiCallStarted()
                val updateAddressResponse = merchantRepo.fetchAddressesFromServer(userID, token)
                updateAddressResponse.let {
                    if(it.status == 1){
                        val addrListSize = it.address?.size?:0
                        for (addr in 0 until addrListSize){
                            if(it.address!![addr].defaultaddress == 1){
                                val address = it.address!!.get(addr)
                                val addressModal = Gson().toJson(address)
                                merchantBenefitListener.onSuccessDefaultAddress(addressModal)
                            }
                        }
                        merchantRepo.insertAllAddressToDB(it.address?:ArrayList())
                    }else{
                        merchantBenefitListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                merchantBenefitListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                merchantBenefitListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerPrimeUsingDetails(fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
                                  address: RequestBody, locality: RequestBody, city: RequestBody,
                                  state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
                                  identityImg: MultipartBody.Part, panCardImg: MultipartBody.Part, identityImg2: MultipartBody.Part,
                                  cancelChequeImg: MultipartBody.Part,accountNum: RequestBody,
                                  bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
                                  upi: RequestBody, refferalCode: RequestBody,
                                  deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody){

        Coroutines.main {
            try {
                primeRespListener.onStarted()
                val registerPrimeResponse = merchantRepo.registerPrimeMerchant(fullName, email, dob, phone,
                    address, locality, city, state, pincode, country, identityDetail,
                    panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg, accountNum, bankName,
                    ifscCode, bankLocation, upi, refferalCode, deviceID, registerForPart, instituteValue)
                registerPrimeResponse.let {
                    if (it.status == 1){
                        if(it.otp.isNullOrEmpty() && it.user != null){
                            merchantRepo.update(it.user!!)
                            primeRespListener.onUpdatedRegularMerchant(it.user!!)
                        }else{
                            primeRespListener.onSuccessResponse(it)
                        }
                    }else{
                        primeRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                primeRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                primeRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerPrimeUpdateUsingDetails(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, cancelChequeImg:RequestBody, accountNum: RequestBody,
        bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
        upi: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody){

        Coroutines.main {
            try {
                primeRespListener.onStarted()
                val registerPrimeResponse = merchantRepo.registerPrimeMerchantUpload(fullName, email, dob, phone,
                    address, locality, city, state, pincode, country, identityDetail,
                    panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg, accountNum, bankName,
                    ifscCode, bankLocation, upi, refferalCode, deviceID, registerForPart, instituteValue)
                registerPrimeResponse.let {
                    if (it.status == 1){
                        if(it.otp.isNullOrEmpty() && it.user != null){
                            merchantRepo.update(it.user!!)
                            primeRespListener.onUpdatedRegularMerchant(it.user!!)
                        }else{
                            primeRespListener.onSuccessResponse(it)
                        }
                    }else{
                        primeRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                primeRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                primeRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerPrimeUpdateUsingDetailsOnlyCheque(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody,
        panCardNo: RequestBody, identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, cancelChequeImg: MultipartBody.Part, accountNum: RequestBody,
        bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
        upi: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody){

        Coroutines.main {
            try {
                primeRespListener.onStarted()
                val registerPrimeResponse = merchantRepo.registerPrimeMerchantUploadOnlyCheque(fullName, email, dob, phone,
                    address, locality, city, state, pincode, country, identityDetail,
                    panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg, accountNum, bankName,
                    ifscCode, bankLocation, upi, refferalCode, deviceID, registerForPart, instituteValue)
                registerPrimeResponse.let {
                    if (it.status == 1){
                        if(it.otp.isNullOrEmpty() && it.user != null){
                            merchantRepo.update(it.user!!)
                            primeRespListener.onUpdatedRegularMerchant(it.user!!)
                        }else{
                            primeRespListener.onSuccessResponse(it)
                        }
                    }else{
                        primeRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                primeRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                primeRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerRegularUsingDetails(fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
                                    address: RequestBody, locality: RequestBody, city: RequestBody,
                                    state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
                                  identityImg: MultipartBody.Part, panCardImg: MultipartBody.Part,
                                    identityImg2: MultipartBody.Part, refferalCode: RequestBody ,
                                    deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody){

        Coroutines.main {
            try {
                regularRespListener.onStarted()
                val registerRegualrResponse = merchantRepo.registerRegularMerchant(fullName, email, dob, phone,
                    address, locality, city, state, pincode, country, identityDetail,
                    panCardNo, identityImg, panCardImg, identityImg2,
                    refferalCode, deviceID, registerForPart, instituteValue)
                registerRegualrResponse.let {
                    if (it.status == 1){
                        if(it.otp.isNullOrEmpty() && it.user != null){
                            merchantRepo.update(it.user!!)
                            regularRespListener.onUpdatedRegularMerchant(it.user!!)
                        }else{
                            regularRespListener.onSuccessResponse(it)
                        }
                    }else{
                        regularRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                regularRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                regularRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerRegularUpdateUsingDetails(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody){

        Coroutines.main {
            try {
                regularRespListener.onStarted()
                val registerRegualrResponse = merchantRepo.registerRegularMerchantUpdate(fullName, email, dob, phone,
                    address, locality, city, state, pincode, country, identityDetail,
                    panCardNo, identityImg, panCardImg, identityImg2,
                    refferalCode, deviceID, registerForPart, instituteValue)
                registerRegualrResponse.let {
                    if (it.status == 1){
                        if(it.otp.isNullOrEmpty() && it.user != null){
                            merchantRepo.update(it.user!!)
                            regularRespListener.onUpdatedRegularMerchant(it.user!!)
                        }else{
                            regularRespListener.onSuccessResponse(it)
                        }
                    }else{
                        regularRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                regularRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                regularRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun verifyOtpPrime(mobNumber:String, otpValue:String, user: User?){
        Coroutines.main{
            try {
                val otpVerifyResponse = merchantRepo.otpVerifyApi(mobNumber, otpValue)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        if(user != null){
                            merchantRepo.update(it.user!!)
                        }else{
                            merchantRepo.insertUser(it.user!!)
                        }
                        primeRespListener.onPrimeMerchantOTPVerify(it.user!!)
                    }else{
                        primeRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                primeRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                primeRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun verifyOtpRegular(mobNumber:String, otpValue:String, user: User?){
        Coroutines.main{
            try {
                val otpVerifyResponse = merchantRepo.otpVerifyApi(mobNumber, otpValue)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        if(user != null){
                            merchantRepo.update(it.user!!)
                        }else{
                            merchantRepo.insertUser(it.user!!)
                        }
                        regularRespListener.onRegularMerchantOTPVerify(it.user!!)
                    }else{
                        regularRespListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                regularRespListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                regularRespListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun merchantBenefitData(){
        Coroutines.main{
            try {
                merchantBenefitListener.onApiCallStarted()
                val merchantBenefitResponse = merchantRepo.getMerchantBenefitDataFromServer()
                merchantBenefitResponse.let {
                    if(it.status == 1){
                        merchantBenefitListener.onSuccessResponse(it.msg, it.membership[0].primemember_charge)
                        merchantRepo.insertMerchantBenefitData(it.merchantbenefit?:ArrayList())
                    }else{
                        merchantBenefitListener.onFailure(it.msg)
                    }
                }
            }catch (e:ApiException){
                merchantBenefitListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                merchantBenefitListener.onNoInternetAvailable(e.message!!)
            }
        }
    }
}