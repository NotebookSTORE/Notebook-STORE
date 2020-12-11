package com.notebook.android.ui.drawerFrag

import androidx.lifecycle.ViewModel
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.ui.dashboard.repository.DashboardRepo
import com.notebook.android.ui.drawerFrag.listener.ContactUsListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException

class AboutUsViewModel(
    val dashboardRepo: DashboardRepo
) : ViewModel() {


    lateinit var aboutUsApiListener: ContactUsListener

    fun getAboutUs(){
        Coroutines.main{
            try {
                aboutUsApiListener.onApiCallStarted()
                val aboutUsResponse = dashboardRepo.getAboutUs()
                aboutUsResponse.let {
                    if(it.status == 1){
                       aboutUsApiListener.onSuccess(it.aboutus[0].description)
                    } else {
                        aboutUsApiListener.onFailure(it.msg)
                    }
                }
            }catch (e: ApiException){
                aboutUsApiListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                aboutUsApiListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

}