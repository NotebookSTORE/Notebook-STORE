package com.notebook.android.ui.drawerFrag

import com.google.android.gms.common.api.ApiException
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.ui.drawerFrag.listener.ContactUsListener
import com.notebook.android.ui.drawerFrag.listener.FaqDataListener
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DrawerPartVM(
    val drawerPartRepo:DrawerPartRepo
) : BaseVM() {

    lateinit var faqDataListener:FaqDataListener
    lateinit var contactUsListener:ContactUsListener
    fun getAllFaqDataFromDB() = drawerPartRepo.getAllFaqDataFromDB()

    fun postContactUsDataServer(fullname:String, phone:String, email:String, msg:String){
       Coroutines.main {
           try {
               contactUsListener.onApiCallStarted()
             /*  val contactUsResponse = async(Dispatchers.IO) {
                   drawerPartRepo.callContactUsApi(fullname, phone, email, msg)
               }.await()*/
               val contactUsResponse = drawerPartRepo.callContactUsApi(fullname, phone, email, msg)
               contactUsResponse.let {
                   if(it.status == 1){
                       contactUsListener.onSuccess(it.msg!!)
                   }else{
                       contactUsListener.onFailure(it.msg!!)
                   }
               }
           }catch (e:ApiException){
               contactUsListener.onApiFailure(e.message!!)
           }catch (e:NoInternetException){
               contactUsListener.onNoInternetAvailable(e.message!!)
           }
       }
    }

    fun getHelpSupportData(){
        Coroutines.main {
            try {
//                contactUsListener.onApiCallStarted()
                val helpSupportResponse = drawerPartRepo.helpSupportData()
                helpSupportResponse.let {
                    if(it.status == 1){
                        contactUsListener.onSuccesHelpSupportData(it.helpSupport[0])
                    }else{
                        contactUsListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: com.notebook.android.utility.ApiException){
                contactUsListener.onFailure(e.message!!)
            }catch (e:NoInternetException){
                contactUsListener.onFailure(e.message!!)
            }
        }
    }

    fun getFaqData(){
//        faqData = MutableLiveData()
        launch(Dispatchers.Main){
            try {
                faqDataListener.onApiCallStarted()
                val contactUsResponse = withContext(Dispatchers.IO) {
                    drawerPartRepo.getFaqDataFromServer()
                }
                contactUsResponse.let {
                    if(it.status == 1){
                        withContext(Dispatchers.IO){
                            for(i in it.faqdata!!.indices){
                                if (i == 0){
                                    it.faqdata!![i].isExpandable = true
                                }
                            }
                            drawerPartRepo.insertFaqDataIntoDB(it.faqdata?:ArrayList())
                            faqDataListener.onSuccess(it.msg)
                        }
                    }else{
                        faqDataListener.onFailure(it.msg!!)
                    }
                }
            }catch (e:ApiException){
                faqDataListener.onFailure(e.message!!)
            }catch (e:NoInternetException){
                faqDataListener.onFailure(e.message!!)
            }
        }
    }

}