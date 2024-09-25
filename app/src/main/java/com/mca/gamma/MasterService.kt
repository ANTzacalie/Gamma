package com.mca.gamma

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference


//actualizat cu transmission object NK/11/2023
// OBIECTUL VA FI FOLOSIT IN CONSTRUCTIE!
class SocketBack: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {

        TODO("Not yet implemented")

    }

}