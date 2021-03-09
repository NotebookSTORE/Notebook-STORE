package com.notebook.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*

class FirebaseMessagingService : FirebaseMessagingService(), KodeinAware {

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(applicationContext)
    }
    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance<AuthViewModelFactory>()
    private val authViewModel: AuthViewModel by lazy {
        AuthViewModel(viewModelFactory.authRepository)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data.get("title")
        val message = data.get("body")
        val topic = remoteMessage.notification?.tag
//        val clickAction = remoteMessage.notification?.clickAction
//        Log.e("FirebaseMsgService", "onMessageReceived: Message Received: \n" +
//                "Title: " + title + "\n" +
//                "Message: " + message)
//        Log.e("notification data", " :: ${data.get("description")}")

        Log.e("FirebaseMsgService", "onMessageReceived: Message Received: \n" +
                "Title: " + data.get("title") + "\n" +
                "Message: " + data.get("body"))
        Log.e("notification data", " :: ${data.get("description")}")

        showNotification(title?:"", message?:"")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        notebookPrefs.firebaseDeviceID = token
        sendTokenToServer(token)
//        sendNotification(title?:"", message?:"")
    }

    private fun sendTokenToServer(token: String) {
        notebookPrefs.userToken?.let {
            if (it.isNotEmpty()) {
                authViewModel.updateDeviceToken(it, token)
            }
        }
    }

    private fun showNotification(title: String, messageBody: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val notificationCompatBuilder = createNotificationCompatBuilder(
            title, messageBody, getString(R.string.notification_channel_id)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = getString(R.string.notification_channel_id)
            val name = "Notebook Channel"
            val descriptionText = "Notebook notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        val id = ((Date().time / 1000L) % Integer.MAX_VALUE).toInt()

        notificationCompatBuilder.let {
            notificationManager.notify(id, it.build())
        }
    }

    private fun createNotificationCompatBuilder(
        title: String, messageBody: String,
        channelId: String
    ): NotificationCompat.Builder {

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(Color.argb(100,25,121,188))
            .setContentTitle(title)
            .setContentText(messageBody)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
    }
}