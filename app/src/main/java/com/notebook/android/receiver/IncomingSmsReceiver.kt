package com.notebook.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class IncomingSmsReceiver: BroadcastReceiver() {

    // Get the object of SmsManager
    val sms: SmsManager = SmsManager.getDefault()
    override fun onReceive(context: Context?, intent: Intent?) {

        // Retrieves a map of extended data from the intent.
        val bundle = intent?.extras
        try {
            if (bundle != null) {
                val pdusObj:Array<Any> = bundle.get("pdus") as Array<Any>

                for (element in pdusObj) {
                    val currentMessage: SmsMessage = SmsMessage.createFromPdu(element as ByteArray)
                    val phoneNumber = currentMessage.getDisplayOriginatingAddress()

                    val senderNum = phoneNumber
                    val message:String = currentMessage.displayMessageBody
                        .replace("\\D", "");

                    //message = message.substring(0, message.length()-1);
                    Log.i("SmsReceiver", "senderNum: " +
                            senderNum + "; message: " + message);

                    val myIntent = Intent("otp")
                    myIntent.putExtra("message", message);
                    myIntent.putExtra("number", senderNum);
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(myIntent)
                    // Show Alert
                } // end for loop
            } // bundle is null
        } catch (e:Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e)
        }
    }
}