package com.mca.gamma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Standby() {

    // standby for connRequest only
    fun connRequestStandby(userEmail: String, connCode: String) {
        Log.d("STB" , "REQUEST PROCEEDING TO USER: $userEmail")

        val data = JSONObject().apply {

            put("senderUsername", localUsername)
            put("senderEmail", localUserEmail)
            put("receiverEmail", userEmail)
            put("serverAccessCode", serverAccessCode)
            put("connCode", connCode)
            put("senderId", localId)

        }

        val event = "conn_request"

        if(permitObjInternetAcess) {

            Transmission.sendAny(event , data)

        }

    }

    fun arbStandby(userEmail: String, connCode: String, context: Context) {
        Log.d("STB_ARB" , "ACCESSED BY: $userEmail")

        val db = MasterDb(context)
        val todo: String = db.getTodoFromMain(userEmail)

        when(todo) {

            "REQUEST_ACCEPTED" -> { // friend_accept / accept on specific clause

                val data = JSONObject().apply {

                    put("senderUsername", localUsername)
                    put("senderEmail", localUserEmail)
                    put("receiverEmail", userEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderId", localId)
                    put("senderRequest", "REQUEST_ACCEPTED")

                }

                if (permitObjInternetAcess) {

                    Transmission.sendAny("arb_request", data)

                }

            }

            "REQUEST_DENIED" -> { //friend_refuse / deny / default_deny

                val data = JSONObject().apply {

                    put("senderUsername", localUsername)
                    put("senderEmail", localUserEmail)
                    put("receiverEmail", userEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderId", localId)
                    put("senderRequest", "REQUEST_DENIED")

                }

                if (permitObjInternetAcess) {

                    Transmission.sendAny("arb_request", data)

                }

            }

            "BLOCKED_INTERNAL" -> { // internal block to a specific friend

                val data = JSONObject().apply {

                    put("senderUsername", localUsername)
                    put("senderEmail", localUserEmail)
                    put("receiverEmail", userEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderId", localId)
                    put("senderRequest", "BLOCKED_EXTERNAL")

                }

                if (permitObjInternetAcess) {

                    Transmission.sendAny("arb_request", data)

                }

            }

        }

    }

    //if message not received / not confirmed this will be used on login / oth login
    fun messageStandBy(userEmail: String , connCode: String , context: Context) {

        val db = MasterDb(context)
        val userId = db.getIdFromMain(userEmail)
        val messageList: MutableList<String?> = db.messageLoaderStandBy(userId)
        val messageIdList: MutableList<String?> = db.messageLoaderIdStb(userId)
        val messagesTimeStamps: MutableList<String?> = db.getMessagesTimeStamp(userId)

        Log.d("STB","M_STB USER BY: $userEmail WITH CONN CODE: $connCode")
        if(messageList.size > 0) {

            val messages = JSONObject().apply {

                put("messageList", JSONArray(messageList))
                put("messageListSize", (messageList.size).toString())
                put("messageIdList", JSONArray(messageIdList))
                put("messageTimeStamps", JSONArray(messagesTimeStamps))
                put("senderEmail", localUserEmail)
                put("receiverEmail", userEmail)
                put("serverAccessCode", serverAccessCode)
                put("connCode", connCode)
                put("senderId", localId)

            }

            val event = "send_messages_bulk"

            if(permitObjInternetAcess) {

                Transmission.sendAny(event, messages)

            }

        }

    }

}


class Https() {

    // register request
    fun httpsFun4(text1: String, text2: String, text3: String): String {

        val client = OkHttpClient()
        val url = "$serverAddress/fun4"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "username": "$text1",
                "email": "$text2",
                "password": "$text3"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                return jsonObject.getString("response")
            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN4", "FAILED")

            return "FAILED"

        } finally {

            client.connectionPool.evictAll()

        }

        return "FAILED"

    }

    //login https request
    fun httpsFun1(text1: String, text2: String ) {

        val client = OkHttpClient()
        val url = "$serverAddress/fun1"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                
                "email": "$text1",
                "password": "$text2"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                localUsername = jsonObject.getString("username")
                localId = jsonObject.getString("id")
                Log.d("HTTPS FUN1" , "ID: $localId , USERNAME: $localUsername")

            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN1", "FAILED")
            localUsername = ""
            localId = ""

        } finally {

            client.connectionPool.evictAll()

        }

    }

    //https request pt code verification
    fun httpsFun5(text1: String): String {

        val client = OkHttpClient()
        val url = "$serverAddress/fun5"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "code": "$text1",
                "email": "$localUserEmail"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                return jsonObject.getString("response")
            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN5", "FAILED")

            return "false"

        } finally {

            client.connectionPool.evictAll()

        }

        return "false"

    }



    fun httpsFun6(code: String , email: String , password: String): Boolean {

        val client = OkHttpClient()
        val url = "$serverAddress/fun6"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "code": "$code",
                "email": "$email",
                "newPassword": "$password"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                return jsonObject.getBoolean("response")
            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN6", "FAILED")

            return false

        } finally {

            client.connectionPool.evictAll()

        }

        return false

    }

    fun httpsFun8(email: String): Boolean {

        val client = OkHttpClient()
        val url = "$serverAddress/fun8"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "email": "$email"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                return jsonObject.getBoolean("response")
            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN8", "FAILED")

            return false

        } finally {

            client.connectionPool.evictAll()

        }

        return false

    }

    fun httpsFun7(newUsername: String): Boolean {

        val client = OkHttpClient()
        val url = "$serverAddress/fun7"

        val mediaType = "application/json".toMediaType()
        val requestBody = """
            {
                "email": "$localUserEmail",
                "newUsername": "$newUsername",
                "serverAccessCode": "$serverAccessCode"
            }
        """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseData)

                return jsonObject.getBoolean("response")
            }

        } catch (e: IOException) {

            Log.d("HTTPS FUN7", "FAILED")

            return false

        } finally {

            client.connectionPool.evictAll()

        }

        return false

    }

}

class UserActivityChild(): AppCompatActivity() {

    fun sendMessage(inputText: EditText, linearLayout: LinearLayout, context: Context , sUser: String? , sUKey: String?, sId : String? , fileUri: Uri? , fileType: String?) {

        // take the input from textbox line with line
        val fullInputText = inputText.text.toString().lines()

        // we turn the text taken line with line into multiple line (one below)
        val finalText = fullInputText.joinToString("\n")

        // we display a card with input in textvar
        CardViews().sendMessageCard(finalText, Time().getCurrentTime(), linearLayout, context , fileUri , fileType)

        //Sending the message
        Transmission.sendMessage(sUser, sUKey, finalText, sId) //trimitem mesajul
        Log.d("MESSAGE SENT", "MESSAGE: $finalText")

        // we delete the text from the box where we enter text
        inputText.text.clear()

    }

    //FOR OTHER FILE EXTENSIONS , NOT SUPPORTED BY THE APP
    private fun openFileWithExternalApp(uri: Uri) {

        val intent = Intent(Intent.ACTION_VIEW).apply {

            setDataAndType(uri, contentResolver.getType(uri)) // SET THE URI AND MIME TYPE(FILE TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // GET READ PERMISSION

        }

        if(intent.resolveActivity(packageManager) != null) {

            startActivity(Intent.createChooser(intent, "Open file with: "))

        } else {

            Toast.makeText(this, "No app was found to open this file" , Toast.LENGTH_SHORT).show()

        }

    }

    // FOR FILE SHARE TO OTHER APPS , THIS IS WHAT WE GONNA USE ON CHAT_CARD FOR REDIRECT!
    fun shareFileWithOtherApps(uri: Uri) {

        // Create an intent for sending the file
        val intent = Intent(Intent.ACTION_SEND).apply {

            type = contentResolver.getType(uri)  // Get the file type (MIME type) based on the Uri
            putExtra(Intent.EXTRA_STREAM, uri)   // Pass the file Uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Grant read permission for the receiving app

        }

        // Show the app chooser with the title "Transfer file with:"
        startActivity(Intent.createChooser(intent, "Transfer file with:"))

    }

    // OPENS THE DEFAULT FILE PICKER , HERE THE USER SELECTS THE FILE IT WANTS TO TRANSFER
    fun openFilePicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            type = "*/*"  // Allow any file type to be selected
            addCategory(Intent.CATEGORY_OPENABLE)

        }

        startActivityForResult(intent, 1)
    }

}

class CardViews(): AppCompatActivity() {

    private lateinit var mediaControls: MediaController
    // card is composed of: text_view (text sent/received) + text_view (message_date)
    // Todo: Add Message Delete, only for the local user!

    @SuppressLint("InflateParams", "MissingInflatedId")
    fun sendMessageCard(messageText: String? , messageDateText: String?, activityLayout: LinearLayout, context: Context , fileUri: Uri? , fileType: String?) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.chat_card , null)
        val mediaLayout = inflater.inflate(R.layout.media_layout , null)

        val card: CardView = cardLayout.findViewById(R.id.cardView)
        val cardText: TextView = cardLayout.findViewById(R.id.cardTextB)
        val cardDateText: TextView = cardLayout.findViewById(R.id.dateText)
        val cardImageView: ImageView = mediaLayout.findViewById(R.id.imageView)
        val cardVideoView: VideoView = mediaLayout.findViewById(R.id.videoView)
        //val cardWebView: WebView = mediaLayout.findViewById(R.id.webView)

        card.setCardBackgroundColor(context.getColorStateList(R.color.light_blue))

        cardText.text = messageText
        cardDateText.text = messageDateText

        when(fileType) { //TODO: DE TERMINAT

            ".jpg" -> {
                
            }
            ".mp4" -> {


            }
            ".txt" -> {

            }
            else -> {

            }

        }


        activityLayout.addView(cardLayout)
        activityLayout.addView(mediaLayout)

        Log.d("cardViews","sendMessageCard")

    }

    @SuppressLint("InflateParams")
    fun receiveMessageCard(messageText: String? , messageDateText: String?, activityLayout: LinearLayout, context: Context) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.chat_card , null)

        val card: CardView = cardLayout.findViewById(R.id.cardView)
        val cardText: TextView = cardLayout.findViewById(R.id.cardTextB)
        val cardDateText: TextView = cardLayout.findViewById(R.id.dateText)

        card.setCardBackgroundColor(context.getColorStateList(R.color.gri30))

        cardText.text = messageText
        cardDateText.text = messageDateText

        activityLayout.addView(cardLayout)

        Log.d("cardViews","receiveMessageCard")

    }

    //TODO: LATER! , THE ICON OF THE USER WILL BE RECEIVED WEHN BOTH USER WILL BR FRIEND , ICON WILL UPDATE AS USERS UPDATE IT , LOGIC AS IM_ONLINE WILL BE USED FOR THAT!
    @SuppressLint("InflateParams")
    fun mainUiCard(dataVector: MutableList<String?>, activityLayout: LinearLayout, context: Context , constraintLayout: ConstraintLayout) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.main_conn_card , null) as ViewGroup

        // Layout of the menu with buttons, give it to the function to display it on the screen
        val buttonLayout = inflater.inflate(R.layout.conn_main_remove_block_prompt , null) as ViewGroup

        // take the card to find the text and add a picture to it
        val card: CardView = cardLayout.findViewById(R.id.mainConnCard)

        //val image: ImageView = card.findViewById(R.id.cardImage) //Image we will add the friend's profile image, we see how we add it (where we store the image in the first phase!)
        val cardUsername: TextView = card.findViewById(R.id.cardText)
        val buildText: String? = dataVector[3] //username friend

        cardUsername.text = buildText
        card.setOnClickListener {

            val intent = Intent("UserActivity")

            // so we transfer information activities, through intent.putextra
            sUser = dataVector[0]
            sUKey = dataVector[1]
            sUsername = dataVector[3]
            sId = dataVector[4]

            // so we can open a new activity from a external function
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        }

        card.setOnLongClickListener {

            if(cardMainBoolean) {

                cardMainBoolean = false

                // call a function so we can add buttonlayout more, than to put everything here
                removeFromFriendsCard(dataVector[3], dataVector[0], activityLayout, buttonLayout, cardLayout , constraintLayout)

            }

            true
        }

        activityLayout.addView(cardLayout)
    }

    @SuppressLint("InflateParams")
    fun connUiCard(dataVector: MutableList<String?>, activityLayout: LinearLayout, context: Context , constraintLayout: ConstraintLayout) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.main_conn_card , null) as ViewGroup

        // Layout of the menu with buttons, give it to the function to display it on the screen
        val buttonLayout = inflater.inflate(R.layout.conn_main_remove_block_prompt , null) as ViewGroup

        // take the card to find the text and add a picture to it
        val card: CardView = cardLayout.findViewById(R.id.mainConnCard)

        //val image: ImageView = card.findViewById(R.id.cardImage) // Image we will add the friend's profile image, we see how we add it (where we store the image in the first phase!)
        val cardUsername: TextView = card.findViewById(R.id.cardText)
        val buildText: String = dataVector[0] + " WITH ID:" + dataVector[2]

        cardUsername.text = buildText
        card.setOnClickListener {

            if(cardConnBoolean) {

                cardConnBoolean = false

                // call a function so we can add buttonlayout more, than to put everything here
                connAcceptRefuseCard(dataVector[3], dataVector[0], activityLayout, buttonLayout, cardLayout, constraintLayout)

            }

        }

        card.setOnLongClickListener {

            if(cardConnBoolean) {

                cardConnBoolean = false

                // call a function so we can add buttonlayout more, than to put everything here
                blockConnRequest(dataVector[0]!!, activityLayout, buttonLayout, cardLayout, constraintLayout)

            }

            true
        }

        activityLayout.addView(cardLayout)
    }

    private fun connAcceptRefuseCard(username: String?, userEmail: String?, activityLayout: LinearLayout, buttonLayout: ViewGroup, cardLayout: ViewGroup, constraintLayout: ConstraintLayout) {

        val constraintFather: ConstraintLayout = buttonLayout.findViewById(R.id.constraintFather)
        val constraintChild: ConstraintLayout = buttonLayout.findViewById(R.id.constraintChild)
        val buttonLayoutText: TextView = buttonLayout.findViewById(R.id.textView4)
        val buttonLayoutText2: TextView = buttonLayout.findViewById(R.id.textView5)
        val acceptButton: Button = buttonLayout.findViewById(R.id.accept)
        val denyButton: Button = buttonLayout.findViewById(R.id.deny)

        val preMadeText: Array<String> = arrayOf("Accept" ,"Deny" ,"Accept $username request?" ,"Be carefully when accepting strangers!")
        acceptButton.text = preMadeText[0]
        denyButton.text = preMadeText[1]

        buttonLayoutText.text = preMadeText[2]; buttonLayoutText.textSize = 17.0F
        buttonLayoutText2.text = preMadeText[3]; buttonLayoutText2.textSize = 16.5F

        acceptButton.setOnClickListener {

            // we accept the reequest here
            Transmission.acceptRequest(userEmail)

            // we destroy Buttonlayout and the card created to the buttons, because we don't need
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        denyButton.setOnClickListener {

            // we refuse Conn Request here
            Transmission.refuseRequest(userEmail)

            // we destroy Buttonlayout and the card created to the buttons, because we don't need
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        constraintFather.setOnClickListener {//buttonLayoutFather

            constraintLayout.removeView(buttonLayout)

            cardMainBoolean = true

        }; constraintChild.setOnClickListener { } // TODO: "DOSE NOTHING , ITS HERE SO THE FATHER DOESN'T KILL HIM"

        constraintLayout.addView(buttonLayout)
    }

    private fun removeFromFriendsCard(username: String?, userEmail: String?, activityLayout: LinearLayout, buttonLayout: ViewGroup, cardLayout: ViewGroup, constraintLayout: ConstraintLayout) {

        val constraintFather: ConstraintLayout = buttonLayout.findViewById(R.id.constraintFather)
        val constraintChild: ConstraintLayout = buttonLayout.findViewById(R.id.constraintChild)
        val buttonLayoutText: TextView = buttonLayout.findViewById(R.id.textView4)
        val buttonLayoutText2: TextView = buttonLayout.findViewById(R.id.textView5)
        val refuse: Button = buttonLayout.findViewById(R.id.accept)
        val goBack: Button = buttonLayout.findViewById(R.id.deny)

        val preMadeText: Array<String> = arrayOf("Yes", "No", "Remove $username?", "All chat history will be deleted!")
        refuse.text = preMadeText[0]
        goBack.text = preMadeText[1]

        buttonLayoutText.text = preMadeText[2]; buttonLayoutText.textSize = 17.0F
        buttonLayoutText2.text = preMadeText[3]; buttonLayoutText2.textSize = 16.5F

        refuse.setOnClickListener {

            // we accept the request here
            Transmission.refuseRequest(userEmail)

            // we destroy Buttonlayout and the card created to the buttons, because we don't need them anymore
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardMainBoolean = true
        }

        goBack.setOnClickListener {

            // we destroy Buttonlayout and the card created to the buttons, because we don't need them anymore
            constraintLayout.removeView(buttonLayout)

            cardMainBoolean = true
        }

        constraintFather.setOnClickListener { // buttonLayout father

            constraintLayout.removeView(buttonLayout)

            cardMainBoolean = true

        }; constraintChild.setOnClickListener { } // TODO: "DOSE NOTHING , ITS HERE SO THE FATHER DOESN'T KILL HIM"

        constraintLayout.addView(buttonLayout)
    }

    private fun blockConnRequest(userEmail: String?, activityLayout: LinearLayout, buttonLayout: ViewGroup, cardLayout: ViewGroup, constraintLayout: ConstraintLayout) {

        val constraintFather: ConstraintLayout = buttonLayout.findViewById(R.id.constraintFather)
        val constraintChild: ConstraintLayout = buttonLayout.findViewById(R.id.constraintChild)
        val buttonLayoutText: TextView = buttonLayout.findViewById(R.id.textView4)
        val buttonLayoutText2: TextView = buttonLayout.findViewById(R.id.textView5)
        val refuse: Button = buttonLayout.findViewById(R.id.accept)
        val goBack: Button = buttonLayout.findViewById(R.id.deny)

        val preMadeText: Array<String> = arrayOf("Yes" ,"No" ,"Block $userEmail?","You will not receive any more requests from this person!")
        refuse.text = preMadeText[0]
        goBack.text = preMadeText[1]

        buttonLayoutText.text = preMadeText[2]; buttonLayoutText.textSize = 16.0F
        buttonLayoutText2.text = preMadeText[3]; buttonLayoutText2.textSize = 15.5F

        refuse.setOnClickListener {

            // we accept the reequest here
            Transmission.blockRequest(userEmail)

            // we destroy Buttonlayout and the card created to the buttons, because we don't need
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        goBack.setOnClickListener {

            // we destroy Buttonlayout and the card created to the buttons, because we don't need
            constraintLayout.removeView(buttonLayout)

            cardConnBoolean = true
        }

        constraintFather.setOnClickListener {//buttonLayout father

            constraintLayout.removeView(buttonLayout)

            cardMainBoolean = true

        }; constraintChild.setOnClickListener { } // TODO: "DOSE NOTHING , ITS HERE SO THE FATHER DOESN'T KILL HIM"

        constraintLayout.addView(buttonLayout)
    }

}

class Random() {

    fun genRandomCode(length: Int): String { Log.d("Random", "12 CHARACTER STRING GENERATED")

        val allChars  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=<>?"

        val password = StringBuilder()

        repeat(length) {

            val randomIndex = (allChars.indices).random()
            val randomChar = allChars[randomIndex]
            password.append(randomChar)

        }

        return password.toString()
    }

}

class HasInternet() {

    // to check the internet access
    fun hasInternetAccess(context: Context): Boolean {

        Log.d("HasInternet","hasInternetAccess used!")

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    }

}

class Time { // Todo: We still have no timezone, we leave this as it is for the moment.

    fun convertTS(messageTimeStamp: String): String {

        val timestampFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val timestamp = LocalDateTime.parse(messageTimeStamp, timestampFormatter)

        return timestamp.toString()

    }

    fun getCurrentTime(): String {

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        return currentDateTime.format(formatter).toString()

    }

}












//folosim asta doar pt developmentul local , un certificat unsigned , cand trecem la over the network folosim un certificat signed
/*
class InsecureHttpsClient() {

    fun createInsecureOkHttpClient(): OkHttpClient {
        Log.d("InsecureHttpsClient","OBJECT X509TrustManager")

        val trustAllCertificates: X509TrustManager = @SuppressLint("CustomX509TrustManager")

        object : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustAllCertificates), SecureRandom())
        }

        // Create an OkHttpClient that trusts all certificates
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates)
            .hostnameVerifier { _, _ -> true } // Accept all hostnames (not recommended for production)
            .build()

    }

}
*/