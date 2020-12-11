package com.notebook.android.ui.orderSummary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException

class OrderSummaryVM(
    val orderSummaryRepo:OrderSummaryRepo) : ViewModel() {
    
    lateinit var orderSummaryListener: OrderResponseListener

    fun orderPlacedByCOD(orderDetails: OrderPaymentDetail, type:String){
        Coroutines.main{
            try {
                orderSummaryListener.onApiCallStarted()
                val prodResponse = orderSummaryRepo.orderPlacedWithCOD(orderDetails)
                prodResponse.let {
                    if(it.status == 1){
                        orderSummaryListener.onSuccessOrder(it, type)
                    }else{
                        orderSummaryListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

//    type -> 1 -> buy now from product page
    // type -> 0 -> cart page
    fun paymentSaveToDB(paymentRawData: AfterPaymentRawData){
        Coroutines.main{
            try {
                orderSummaryListener.onApiCallAfterPayment()
                val prodResponse = orderSummaryRepo.paymentSaveToDB(paymentRawData)
                prodResponse.let {
                    if(it.status == 1){
                        orderSummaryListener.onSuccesPayment(paymentRawData.orderId, it.status,
                            paymentRawData.amount, paymentRawData.txtMsg)
                    }else{
                        orderSummaryListener.onFailurePayment(paymentRawData.orderId, it.status,
                            paymentRawData.amount, paymentRawData.txtMsg)
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    /*fun getApplyCouponData(userID:Int){
        Coroutines.main{
            try {
                val prodResponse = orderSummaryRepo.getApplyCouponData(userID)
                prodResponse.let {
                    if(it.status == 1){
                        orderSummaryRepo.insertCouponDat(it.coupon?:ArrayList())
                    }else{
                        orderSummaryListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }*/

    var cfTokenObserver: MutableLiveData<AddWalletResponse> = MutableLiveData()
    fun addWalletFromServer(addWalletData: AddWallet){
        Coroutines.main{
            try {
                orderSummaryListener.onApiCallStarted()
                val addAddressResponse = orderSummaryRepo.addWalletAmountFromGateway(addWalletData)
                addAddressResponse.let {
                    if (it.status == 1){
                        cfTokenObserver.value = addAddressResponse
                        orderSummaryListener.onSuccessCFToken()
                    }else{
                        orderSummaryListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun walletSuccessAfterAddFromServer(walletSuccessRaw: WalletSuccess, amount:Float, txtMsg:String){
        Coroutines.main{
            try {
                orderSummaryListener.onApiCallStarted()
                val userLogoutResponse = orderSummaryRepo.afterAddWalletSuccessFromServer(walletSuccessRaw)
                userLogoutResponse.let {
                    if(it.status == 1){
                        orderSummaryListener.onSuccess(walletSuccessRaw.orderId, walletSuccessRaw.status, amount, txtMsg)
                    }else{
                        orderSummaryListener.onWalletFailure(walletSuccessRaw.orderId, walletSuccessRaw.status, amount, txtMsg)
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getWalletAmountFromServer(walletAmountRaw: WalletAmountRaw){
        Coroutines.main{
            try {
                orderSummaryListener.onApiCallStarted()
                val walletAmountResponse = orderSummaryRepo.getWalletAmountFromServer(walletAmountRaw)
                walletAmountResponse.let {
                    if(it.status == 1){
                        orderSummaryListener.walletAmount(it.amount?:"")
                    }else{
                        orderSummaryListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                orderSummaryListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                orderSummaryListener.onNoInternetAvailable(e.message!!)
            }
        }
    }
}