package com.notebook.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Matcher
import java.util.regex.Pattern

class GoogleSmsReadBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action && intent.extras!=null) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    var message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    Log.e("Message", message)

                    message = message.replace("Your Verfication Code is:", "").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    Log.e("otp message", " :: $message")

                    val regex = "(|^)\\d{6}"

                    val m: Matcher = Pattern.compile(regex).matcher(message)
                    if(m.find())
                        Log.e("substring", m.group(0))

                    val myIntent = Intent("otp")
                    myIntent.putExtra("otpValue", m.group(0))
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(myIntent)
                    Log.e("sms data", " :: ${message}")
//                        otpReceiver!!.onOTPReceived(otp)
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                }
            }
        }
    }
}