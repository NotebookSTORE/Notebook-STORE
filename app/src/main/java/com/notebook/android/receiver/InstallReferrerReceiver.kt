package com.notebook.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notebook.android.data.preferences.NotebookPrefs

class InstallReferrerReceiver : BroadcastReceiver() {

    private lateinit var notebookPrefs: NotebookPrefs

    override fun onReceive(context: Context?, intent: Intent?) {
        notebookPrefs = context?.let { NotebookPrefs(it) }!!
        val referrer = intent?.getStringExtra("referrer")
        Log.e("refferalCode", " :: $referrer")
        notebookPrefs.merchantRefferalID = referrer


        if (intent?.action != null) {
            if (intent.action.equals("com.android.vending.INSTALL_REFERRER")) {

                val extras = intent.extras
                if (extras != null) {
                    val referrerId = extras.getString("referrer")

                    Log.e("Receiver Referral", "===>" + referrerId)
                    if(referrerId?.startsWith("utm_sourc") == true){
                        notebookPrefs.merchantRefferalID = ""
                    }else{
                        notebookPrefs.merchantRefferalID = referrerId
                    }
                }
            }
        }
    }
}