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

//updated with transmission object (from NK/11/2023 ==>>> to 10/10/2024)
// ALL LOGIC INSIDE OBJECT , ONLY CONTROL HERE

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

/*
// TO BE IMPLEMENTED INT THE MAIN OBJECT
object Transmit: AppCompatActivity() {

    private val options: IO.Options = IO.Options().apply { reconnection = true; forceNew = true }
    val socket: Socket = IO.socket("https://antsecurehost.go.ro:8080" , options)

    private var showImage: WeakReference<ImageView>? = null
    private var appContext: WeakReference<Context>? = null

    fun addImageView(imageView: ImageView) {

        showImage = WeakReference(imageView)

    }

    fun addContext(context: Context) {

        appContext = WeakReference(context)

    }

    private fun getImageView(): ImageView? {

        return showImage?.get()

    }

    private fun getContext(): Context? {

        return appContext?.get()

    }

    init {

        Log.d("SOCKET_IO" , "INITIALIZED ON_BOOT")

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

    fun connect() {

        socket.connect()

        val data = JSONObject().apply {

            put("serverAccessCode", "LR5zwVgqnZRf") // "yPqp2OJsbIZG" / "LR5zwVgqnZRf"
            put("senderEmail", "antoniomihalceacatalin43@gmail.com") // "antoniomihalceacatalin@gmail.com" / "antoniomihalceacatalin43@gmail.com"

        }

        socket.emit("on_connect", data)

    }

    fun disconnect() {

        val data = JSONObject().apply {

            put("serverAccessCode", "LR5zwVgqnZRf") // "yPqp2OJsbIZG" / "LR5zwVgqnZRf"
            put("senderEmail", "antoniomihalceacatalin43@gmail.com") // "antoniomihalceacatalin@gmail.com" / "antoniomihalceacatalin43@gmail.com"

        }

        socket.emit("on_disconnect", data)

        socket.disconnect()

    }

    // WITH THIS FUNCTION WE READ THE FILE FROM URI AND RETURN IT AS A BYTE_ARRAY FOR FURTHER PROCESS
    private fun readFileFromUri(uri: Uri, context: Context): ByteArray? {

        try {

            // Declaring and init I/O STREAM
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val byteArrayOutputStream = ByteArrayOutputStream()

            if (inputStream != null) {

                // Creating the buffer
                val buffer = ByteArray(1024)
                var length: Int

                // Read the file content into a byte array
                while (inputStream.read(buffer).also { length = it } != -1) {

                    byteArrayOutputStream.write(buffer, 0, length)

                }

                inputStream.close()
                return byteArrayOutputStream.toByteArray()

            }

        } catch (e: Exception) {

            Log.d("READ FILE FROM URI" ,"File unsuccessfully read , error message: ${e.message}")

        }

        return null

    }

    // Function to decode Base64 to file and save it locally , also identify the file extension for easy managing, returns the URI after is done
    private fun decodeBase64AndSaveFile(base64File: String, mimeType: String , context: Context): Uri? {

        try {

            // Decode Base64 string to byte array
            val fileBytes = Base64.decode(base64File, Base64.DEFAULT)

            // Determine the file extension based on the MIME type
            val fileExtension = when {

                mimeType.startsWith("image/") -> ".jpg"

                mimeType.startsWith("video/") -> ".mp4"

                mimeType.startsWith("text/") -> ".txt"

                else -> ".bin"

            }

            // TODO: Save the file to external storage (modify path as needed) , *"received_file" need to add a random name generator or send the original filename.
            val fileName = "received_file$fileExtension"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val fos = FileOutputStream(file)

            fos.write(fileBytes)
            fos.close()

            // Return the file's URI
            return Uri.fromFile(file)

        } catch (e: Exception) {

            Log.d("DECODE BASE64 FILE" , "File not decode, error: ${e.message}")

        }

        return null

    }

}

*/