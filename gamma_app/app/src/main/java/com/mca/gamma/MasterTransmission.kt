package com.mca.gamma
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference


object Connectivity : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null) {

            val isConnected = HasInternet().hasInternetAccess(context)

            if (isConnected && permitActivityAfterLogin && used > 0) {

                Log.d("CONNECTIVITY" , "USED[${used}]")

                Transmission.socket.disconnect()
                Transmission.start()

            }

            permitObjInternetAcess = isConnected
            used++

        }

    }

}

// ALMOST ALL SOCKET.IO LOGIC
object Transmission : AppCompatActivity() {

    private lateinit var db: MasterDb
    private val options: IO.Options = IO.Options().apply { reconnection = true; forceNew = true }
    var socket: Socket = IO.socket(serverAddress , options)

    private var activityContext: WeakReference<Context>? = null

    private fun getContext(): Context? {

        return activityContext?.get()

    }
    fun addContext(context: Context) {

        activityContext = WeakReference(context)

    }

    fun start() {

        socket.connect()
        createSocketId()
        db = MasterDb(getContext()!!)

    }
    fun stop() {

        disconnectFromServer()
        socket.disconnect()

    }

    init {

        Log.d("SOCKET_IO" , "INITIALIZED FOR FIRST TIME ON BOOT")

        socket.on("message$localUserEmail") { args ->



        }

        socket.on("request$localUserEmail") { args ->



        }

        socket.on("serverUpdates$localUserEmail") { args ->



        }

        /**
         * FUTURE IMPLEMENTATIONS MAY ADD OTHER LISTENERS FOR ADVANCED LOGIC;
         *
         *
         * **/

    }


    fun sendRequest(toUser: String?, connCode: String) {



    }

    fun acceptRequest(toUser: String?) {



    }
    fun refuseRequest(toUser: String?) { //asta se foloseste si la eliminarea prietenilor



    }

    fun blockRequest(toUser: String?) {



    }

    fun imOnline(toUser: String?) {



    }

    fun sendMessage() {

    }

    private fun createSocketId() {

        val init = JSONObject().apply {

            put("serverAccessCode", serverAccessCode)
            put("senderEmail", localUserEmail)

        }
        socket.emit("on_connect", init)

    }

    private fun disconnectFromServer() {

        val init = JSONObject().apply {

            put("serverAccessCode", serverAccessCode)
            put("senderEmail", localUserEmail)

        }
        socket.emit("on_disconnect", init)

    }

}
