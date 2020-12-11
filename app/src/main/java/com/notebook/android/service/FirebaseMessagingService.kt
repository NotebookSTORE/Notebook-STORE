package com.notebook.android.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.ui.dashboard.MainDashboardPage

class FirebaseMessagingService : FirebaseMessagingService() {

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
        val message = remoteMessage.notification?.body
        val topic = remoteMessage.notification?.tag
        val data = remoteMessage.data
//        val clickAction = remoteMessage.notification?.clickAction
        Log.e("FirebaseMsgService", "onMessageReceived: Message Received: \n" +
                "Title: " + title + "\n" +
                "Message: " + message)
        Log.e("notification data", " :: ${data.get("description")}")

//        sendNotification(title?:"", message?:"", topic?:"", clickAction?:"")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        notebookPrefs.firebaseDeviceID = token
        Log.e("firebaseMsgSeice token", " :: ${token}")
//        sendTokenToServer(token)
//        sendNotification(title?:"", message?:"")
    }

    private var notID = 0
    private fun sendNotification(title: String, messageBody: String, topic:String, clickAction:String) {

        var intent: Intent?= null
        var pendingIntent: PendingIntent?= null
        if(topic.equals("quiz", true)){
            intent = Intent(this@FirebaseMessagingService, MainDashboardPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("fromPage", "QuizFBService")
            intent.putExtra("offerUrl", "")
            pendingIntent = PendingIntent.getActivity(this@FirebaseMessagingService, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)
        }else  if(topic.equals("recharge", true)){
            Log.e("topic", " :: $topic")
            intent = Intent(this@FirebaseMessagingService, MainDashboardPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("fromPage", "QuizFBService")
            intent.putExtra("offerUrl", "")
            pendingIntent = PendingIntent.getActivity(this@FirebaseMessagingService, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)
        }else{
            intent = Intent(this@FirebaseMessagingService, MainDashboardPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("fromPage", "QuizFBService")
            intent.putExtra("offerUrl", "")
            pendingIntent = PendingIntent.getActivity(this@FirebaseMessagingService, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /* Important Link to set click event...

        https://stackoverflow.com/questions/1198558/how-to-send-parameters-from-a-notification-click-to-an-activity*/
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setPriority(2)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notID++, notificationBuilder.build())
    }
}