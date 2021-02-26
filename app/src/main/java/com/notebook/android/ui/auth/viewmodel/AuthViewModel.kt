package com.notebook.android.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.ui.auth.repository.AuthRepository
import com.notebook.android.ui.auth.responseListener.AuthLoginListener
import com.notebook.android.ui.auth.responseListener.AuthResponseListener
import com.notebook.android.ui.auth.responseListener.ChangePassListener
import com.notebook.android.ui.auth.responseListener.ForgotResponseListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException

class AuthViewModel(
    val authRepository: AuthRepository
) : ViewModel() {

    var authResponseListener:AuthResponseListener ?= null
    var forgotResponseListener:ForgotResponseListener ?= null
    var changePassListener:ChangePassListener ?= null
    lateinit var authLoginListener:AuthLoginListener

    fun userSignUpCall(name:String, email:String, mobNumber:String, password:String,
                       deviceID: String, typeUser:Int, refferalCode:String){
        Coroutines.main{
            try {
                authResponseListener?.onApiCallStarted()
                val userSignupResponse = authRepository.userSignupApi(name, email, mobNumber,
                    password, deviceID, typeUser, refferalCode)
                userSignupResponse.let {
                    if(it.status == 1){
                        authResponseListener?.onSuccess(it)
                    }else{
                        authResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authResponseListener?.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authResponseListener?.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun socialMobileLogin(fullname:String, email:String, deviceID:String,
                          userType:Int, mobNumber:String, profileImage:String){
        Coroutines.main{
            try {
                authResponseListener?.onApiCallStarted()
                val userSignupResponse = authRepository.socialMobileLogin(fullname, email, mobNumber,
                    profileImage, deviceID, userType)
                userSignupResponse.let {
                    if(it.status == 1){
                        authResponseListener?.onSuccess(it)
                    }else{
                        authResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authResponseListener?.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authResponseListener?.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun socialMobileLoginServer(username: String, email: String,
                          deviceID:String, profileImg: String, userType:Int, socialID:String){
        Coroutines.main{
            try {
                authLoginListener.onApiCallStarted()
                val userSignupResponse = authRepository.socialMobileLoginServer(username, email,
                    deviceID, profileImg, userType, socialID)
                userSignupResponse.let {
                    if(it.status == 1){
                        authLoginListener.onSuccessSocial(it.user!!, it.user!!.imageupdated?:0, userType)
                        authRepository.insertUser(it.user!!)
                    }else{
                        authLoginListener.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authLoginListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authLoginListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun mobileLoginExistChecking(email:String, userType:Int){
        Coroutines.main{
            try {
                authLoginListener.onApiCallStarted()
                val otpVerifyResponse = authRepository.mobileLoginExist(email, userType)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        authRepository.insertUser(it.user!!)
                        authLoginListener.onLoginUserExist(it.status, it.user!!.imageupdated?:0, userType)
                    }else{
                        authLoginListener.onLoginUserExist(it.status, it.user?.imageupdated?:0, userType)
                        authLoginListener.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authLoginListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authLoginListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun verifyOtp(mobNumber:String, otpValue:String){
        Coroutines.main{
            try {
                authResponseListener?.onApiCallStarted()
                val otpVerifyResponse = authRepository.otpVerifyApi(mobNumber, otpValue)
                otpVerifyResponse.let {
                   if(it.status == 1){
                       if(it.user != null){
                           authRepository.insertUser(it.user!!)
                       }
                        authResponseListener?.onOtpSuccess(it.user)
                    }else{
                        authResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authResponseListener?.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authResponseListener?.onInternetNotAvailable(e.message!!)
            }
        }
    }

    fun verifyOtpWithForgotPass(mobNumber:String, otpValue:String){
        Coroutines.main{
            try {
                val otpVerifyResponse = authRepository.otpVerifyApi(mobNumber, otpValue)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        forgotResponseListener?.onOtpSuccess(it.user!!)
                    }else{
                        forgotResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                forgotResponseListener?.onFailure(e.message!!)
            }catch (e:NoInternetException){
                forgotResponseListener?.onFailure(e.message!!)
            }
        }
    }

    fun verifyOtpWithForgotPassEmail(email:String, otpValue:String){
        Coroutines.main{
            try {
                val otpVerifyResponse = authRepository.otpVerifyWithEmail(email, otpValue)
                otpVerifyResponse.let {
                    if(it.status == 1){
                        forgotResponseListener?.onOtpSuccess(it.user!!)
                    }else{
                        forgotResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                forgotResponseListener?.onFailure(e.message!!)
            }catch (e:NoInternetException){
                forgotResponseListener?.onFailure(e.message!!)
            }
        }
    }

    fun login(email:String, password:String, deviceId:String){
        Coroutines.main{
            try {
                authLoginListener.onApiCallStarted()
                val loginResponse = authRepository.login(email, password, deviceId)
                loginResponse.let {
                    if(it.status == 1){
                        authRepository.insertUser(it.user!!)
                        authLoginListener.onSuccess(it.user!!)
                    }else{
                        authLoginListener.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                authLoginListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                authLoginListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun updateDeviceToken(token:String, deviceId:String){
        Coroutines.main{
            authRepository.updateDeviceToken(token, deviceId)
        }
    }

    fun forgotPass(inputValue:String){
        Coroutines.main{
            try {

                val forgotResponse = authRepository.forgotPassword(inputValue)
                forgotResponse.let {
                    if(it.status == 1){
                        forgotResponseListener?.onSuccess(it)
                    }else{
                        forgotResponseListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                forgotResponseListener?.onFailure(e.message!!)
            }catch (e:NoInternetException){
                forgotResponseListener?.onFailure(e.message!!)
            }
        }
    }

    fun changePass(mobNumber:String, password:String){
        Coroutines.main{
            try {
                val changePassResponse = authRepository.changePassword(mobNumber, password)
                changePassResponse.let {
                    if(it.status == 1){
                        changePassListener?.onSuccess(true)
                    }else{
                        changePassListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                changePassListener?.onFailure(e.message!!)
            }catch (e:NoInternetException){
                changePassListener?.onFailure(e.message!!)
            }
        }
    }

    fun changePassWithEmail(email:String, password:String){
        Coroutines.main{
            try {

                val changePassResponse = authRepository.changePasswordWithEmail(email, password)
                changePassResponse.let {
                    if(it.status == 1){
                        changePassListener?.onSuccess(true)
                    }else{
                        changePassListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                changePassListener?.onFailure(e.message!!)
            }catch (e:NoInternetException){
                changePassListener?.onFailure(e.message!!)
            }
        }
    }
}