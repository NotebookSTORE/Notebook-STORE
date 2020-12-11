package com.notebook.android.data.network

import android.content.Context
import android.net.ConnectivityManager
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException

class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {

    val applicationContext = context.applicationContext
    override fun intercept(chain: Interceptor.Chain): Response {

        try {
            if(!isNetworkAvailable()){
                throw NoInternetException("No Active Internet Connection.")
            }else{
                return chain.proceed(chain.request())
            }
        }catch (e: SocketTimeoutException){
            throw NoInternetException("Socket Time out. Please try again.")
        }catch (e:Exception){
            throw ApiException("Connection time out. Please try again.")
        }
    }

    fun isNetworkAvailable() : Boolean{
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also {
            return it != null && it.isConnected
        }
    }
}