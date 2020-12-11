package com.notebook.android.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.getSystemService


object ConnectivityUtil {

    fun isNetworkAvailable(context: Context): Boolean {
        // Get Connectivity Manager class object from Systems Service
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        // Get Network Info from connectivity Manager
        val networkInfo = cm!!.activeNetworkInfo

        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected && networkInfo.isAvailable
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }


    fun getWifiLevel(context: Context): Int {
        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val linkSpeed = wifiManager.connectionInfo.rssi
        return WifiManager.calculateSignalLevel(linkSpeed, 5)
    }



    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     *
     * @return
     */
    fun isConnectedWifi(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     *
     * @return
     */
    fun isConnectedMobile(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    fun isConnectedFast(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && isConnectionFast(
            info.type,
            info.subtype
        )
    }
    /**
     * Check if the connection is fast
     *
     * @param subType
     * @return
     */
    fun isConnectionFast(type: Int, subType: Int): Boolean {
        return if (type == ConnectivityManager.TYPE_WIFI) {
            true
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subType) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
                TelephonyManager.NETWORK_TYPE_EHRPD -> true // ~ 1-2 Mbps
                TelephonyManager.NETWORK_TYPE_EVDO_B -> true // ~ 5 Mbps
                TelephonyManager.NETWORK_TYPE_HSPAP -> true // ~ 10-20 Mbps
                TelephonyManager.NETWORK_TYPE_IDEN -> false // ~25 kbps
                TelephonyManager.NETWORK_TYPE_LTE -> true // ~ 10+ Mbps
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                else -> false
            }
        } else {
            false
        }
    }
}