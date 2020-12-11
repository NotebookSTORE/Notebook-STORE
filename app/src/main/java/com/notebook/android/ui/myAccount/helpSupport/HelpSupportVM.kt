package com.notebook.android.ui.myAccount.helpSupport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.ui.myAccount.address.listener.AddressAddUpdateListener
import com.notebook.android.ui.myAccount.helpSupport.listener.HelpSupportListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class HelpSupportVM(
    val helpSupportRepo: HelpSupportRepo
) : ViewModel() {

    lateinit var helpSupportListener: HelpSupportListener
    fun getUserData() = helpSupportRepo.getUser()

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            helpSupportRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            helpSupportRepo.clearCartTable()
            helpSupportRepo.clearAddressTable()
            helpSupportRepo.clearOrderTable()
            helpSupportRepo.clearFavouriteTable()
        }
    }

    fun getHelpSupportData(){
        Coroutines.main {
            try {
                helpSupportListener.onApiCallStarted()
                val helpSupportResponse = helpSupportRepo.helpSupportData()
                helpSupportResponse.let {
                    if(it.status == 1){
                        helpSupportListener.onSuccesHelpSupportData(it.helpSupport[0])
                    }else{
                        helpSupportListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                helpSupportListener.onFailure(e.message!!)
            }catch (e:NoInternetException){
                helpSupportListener.onFailure(e.message!!)
            }
        }
    }

    fun registerAppReportProblem(userID: RequestBody, token: RequestBody,
                                 email: RequestBody, name: RequestBody,
                                 msg: RequestBody, reportImage: MultipartBody.Part){
        Coroutines.main {
            try {
                helpSupportListener.onApiCallStarted()
                val faqResponse = helpSupportRepo.reportProblemRegisterOnServer(userID, token,
                    email, name, reportImage, msg)
                faqResponse.let {
                    if(it.status == 1){
                        helpSupportListener.onSuccess(it.msg!!)
                    }else if(it.status == 2){
                        helpSupportListener.onInvalidCredential()
                    }else{
                        helpSupportListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                helpSupportListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                helpSupportListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun registerAppFeedback(userID:Int, token:String, email:String, name:String, rating:Float, msg:String){
        Coroutines.main {
            try {
                helpSupportListener.onApiCallStarted()
                val faqResponse = helpSupportRepo.appFeedbackRegisterOnServer(userID, token, email, name, rating, msg)
                faqResponse.let {
                    if(it.status == 1){
                        helpSupportListener.onSuccess(it.msg?:"")
                    }else if(it.status == 2){
                        helpSupportListener.onInvalidCredential()
                    }else{
                        helpSupportListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                helpSupportListener.onApiFailure(e.message!!)
            }catch (e:NoInternetException){
                helpSupportListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

}