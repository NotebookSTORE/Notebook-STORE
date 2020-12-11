package com.notebook.android.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NetworkReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        /*try {
            if (isOnline(context!!)) {
                sendMessage(context, true)
                Log.e("Pankaj Mangal", "Online Connect Intenet ")
            } else {
                sendMessage(context, false)
//                Toast.makeText(context, "No Internet Connected", Toast.LENGTH_SHORT).show()
                Log.e("Pankaj Mangal", "Conectivity Failure !!! ")
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }*/
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
// be received by the ReceiverActivity.
    private fun sendMessage(context: Context?, isConnected:Boolean) {
        val intent = Intent("network_state_check")
        intent.putExtra("checkNetwork", isConnected)
        intent.putExtra("message", "This is my message!")
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    /*

How to show a slow internet connection to the user when the network is connected Note: Not a network type (2G,3G,4G, WIFI)



It seem that android doesn't allow this directly. You can try to download some file to determine the speed of your internet. Below are the list of the connection qualities:

    POOR // Bandwidth under 150 kbps.
    MODERATE // Bandwidth between 150 and 550 kbps.
    GOOD // Bandwidth over 2000 kbps.
    EXCELLENT // Bandwidth over 2000 kbps.
    UNKNOWN // connection quality cannot be found.
*/
    fun isOnline(context: Context): Boolean {
        return try {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) {
                // connected to the internet
                when (activeNetwork.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                    }
                    else -> {
                    }
                }
            } else {
                // not connected to the internet
            }
            val nc: NetworkCapabilities? =
                cm.getNetworkCapabilities(cm.activeNetwork)
            val downSpeed = nc?.linkDownstreamBandwidthKbps
            val upSpeed = nc?.linkUpstreamBandwidthKbps
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            netInfo != null && netInfo.isConnected
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
            false
        }
    }
}