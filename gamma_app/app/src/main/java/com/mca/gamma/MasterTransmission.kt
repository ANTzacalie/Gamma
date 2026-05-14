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
    private var classLinearLayout: WeakReference<LinearLayout>? = null
    private var classConstraintLayout: WeakReference<ConstraintLayout>? = null
    private val channel = localUserEmail

    private fun getLinearLayout(): LinearLayout? {

        return classLinearLayout?.get()

    }
    private fun getConstraintLayout(): ConstraintLayout? {

        return classConstraintLayout?.get()

    }
    private fun getContext(): Context? {

        return activityContext?.get()

    }
    fun addContext(context: Context) {

        activityContext = WeakReference(context)

    }
    fun addConstraint(constraintLayout: ConstraintLayout) {

        classConstraintLayout = WeakReference(constraintLayout)

    }
    fun addLayout(linearLayout: LinearLayout) {

        classLinearLayout = WeakReference(linearLayout)

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

    }

    /*

        socket.on("FILE_STREAM_RECEIVER") { args ->
            val data = args[0] as JSONObject
            val base64File = data.getString("someEncodedFile")
            val fileMime = data.getString("fileMime")

            Log.e("SOCKET_IO", "PACKAGE FROM USER RECEIVED")

            val filePath: Uri? = decodeBase64AndSaveFile(base64File , fileMime , getContext()!!)
            if(inActivity && allowUpdate) {

                val imageView: ImageView? = getImageView()

                if (imageView != null) {

                    runOnUiThread { imageView.setImageURI(filePath) }

                } else {

                    Log.e("SOCKET_IO", "ImageView or filePath is null!")

                }

            }

        }

    }

    // SENDING THE FILE TO ANOTHER DEVICE
    fun sendFileTest(inputFile: Uri?, context: Context) {

        val fileBytes: ByteArray? = readFileFromUri(inputFile!! , context)
        val fileMime = context.contentResolver.getType(inputFile)

        if(fileBytes != null) {

            val base64File = Base64.encodeToString(fileBytes , Base64.DEFAULT)

            val data = JSONObject().apply {

                put("someEncodedFile" , base64File)
                put("fileMime" , fileMime)

            }

            Log.d("SEND FILE" , "File was successfully sent! , file: $data")
            socket.emit("file" , data)

        }

    }


    */

    fun sendAny(event: String , data: JSONObject) {
        Log.d("SEND ANY:", event)

        if(permitObjInternetAcess) {

            socket.emit(event , data)

        }

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

    fun sendMessage(toUser: String?, connCode: String?, yourMessage: String?, friendId : String? , fileUri: Uri? , fileType: String?) {

        val currentTime = Time().getCurrentTime()
        val messageId = Random().genRandomCode(14)

        val message = JSONObject().apply {



        }

        if(permitObjInternetAcess) {



        }
        else {



        }

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
