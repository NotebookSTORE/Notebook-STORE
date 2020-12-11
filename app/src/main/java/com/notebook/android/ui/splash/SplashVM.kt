package com.notebook.android.ui.splash

import androidx.lifecycle.ViewModel
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException

class SplashVM(
    val repoSplash:SplashRepository
):ViewModel() {

    lateinit var splashListener:SplashResponseListener

    fun getBannerData(){
        Coroutines.main{
            try {
                val bannerResp = repoSplash.getBannerData()
                bannerResp.let {
                    if(it.status == 1){
                        repoSplash.insertAllBannerIntoDB(it.bannerresponse?:ArrayList())
                        getDrawerCategoryData()
                    }else{
                        splashListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                splashListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                splashListener.onFailure(e.message!!)
            }
        }
    }

    //getDrawerData from api...
    fun getDrawerCategoryData(){
        Coroutines.main{
            try {
                val drawerResp = repoSplash.getDrawerCategoryData()
                drawerResp.let {
                    if(it.status == 1){
                        repoSplash.insertAllDrawerSubCategData(it.catsub?:ArrayList())
                        splashListener.onSuccess()
                    }else{
                        splashListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                splashListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                splashListener.onFailure(e.message!!)
            }
        }
    }

    suspend fun getAllCategoryData(){
        Coroutines.main{
            try {
                val categResp = repoSplash.getAllCategory()
                categResp.let {
                    if(it.status == 1){
                        repoSplash.insertAllCategoryIntoDB(it.category?: ArrayList())
                        getAllSubCategoryData()
                    }else{
                        splashListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                splashListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                splashListener.onFailure(e.message!!)
            }
        }
    }

    suspend fun getAllSubCategoryData(){
        Coroutines.main{
            try {
                val categResp = repoSplash.getAllSubCategory()
                categResp.let {
                    if(it.status == 1){
                        repoSplash.insertAllSubCategoryIntoDB(it.subcategory?: ArrayList())
                        splashListener.onSuccess()
                    }else{
                        splashListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                splashListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                splashListener.onFailure(e.message!!)
            }
        }
    }
}