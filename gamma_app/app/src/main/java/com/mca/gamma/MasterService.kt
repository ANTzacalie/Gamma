package com.mca.gamma

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

// TODO: IMPLEMENT THE SERVICE TO REPLACE THE LOGIC OF THE TRANSMISSION_OBJECT
/** AND WITH THIS WE HAVE CONNECTION TO SERVER IN BACKGROUND AND IN_APP **/

class TransmissionBackground: Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {

        fun getService(): TransmissionBackground = this@TransmissionBackground

    }

    override fun onBind(intent: Intent?): IBinder {

        return binder

    }

    override fun onCreate() {
        super.onCreate()

        Log.d("ServerConnectionService", "Service created")

        startServerLogic()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("ServerConnectionService", "Service started")
        startForegroundService() // Ensure the service is running in the foreground with notification

        return START_STICKY // Ensures the service keeps running unless explicitly stopped

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("ServerConnectionService", "Service destroyed")
        stopServerLogic()

    }

    // server logic is started here
    private fun startServerLogic() {

        Transmission.start()
        Transmission.addContext(applicationContext)
        Log.d("ServerConnectionService", "Server connection started")

    }

    // server connection logic is closed with this function
    private fun stopServerLogic() {

        Transmission.stop()
        Log.d("ServerConnectionService", "Server connection stopped")
        // Clean up any resources related to the server connection

    }

    private fun startForegroundService() {

        //CAN BE NAMED AS WANTED , NO RESTRICTIONS
        val channelId = "server_service"
        val channelName = "Server background service"
        val channelNewId = createNotificationChannel(channelId, channelName)

        //We use the new channelID to display the notification of the foreground service
        val notification: Notification = NotificationCompat.Builder(this, channelNewId)
            .setContentTitle("Server Running")
            .setContentText("The server connection is active.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        startForeground(1, notification) // Starts the foreground service

    }

    // Creates a notification channel and return channelId
    private fun createNotificationChannel(channelId: String, channelName: String): String {

        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)

        return channelId

    }

}
