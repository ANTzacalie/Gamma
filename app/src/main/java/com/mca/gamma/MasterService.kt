package com.mca.gamma

import android.app.Service
import android.content.Intent
import android.os.IBinder


//actualizat cu transmission object NK/11/2023
//nu vom mai implementa aici Transmission vom folosi direct obiectul!
class SocketBackgroundService : Service() {

    override fun onCreate() {
        super.onCreate()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {

        TODO("Not yet implemented")

    }

}