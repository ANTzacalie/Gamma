package com.mca.gamma
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.google.android.material.materialswitch.MaterialSwitch
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

            "REQUEST_ACCEPTED" -> {

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

            "REQUEST_DENIED" -> { //asta este si firend remove

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

            "BLOCKED_INTERNAL" -> { //asta este si friend block in acelas timp

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

    fun httpsFun4(text1: String, text2: String, text3: String): String {

        val client = OkHttpClient()
        val url = "https://antsecurehost.go.ro:8080/fun4"

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
        val url = "https://antsecurehost.go.ro:8080/fun1"

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
        val url = "https://antsecurehost.go.ro:8080/fun5"

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
        val url = "https://antsecurehost.go.ro:8080/fun6"

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
        val url = "https://antsecurehost.go.ro:8080/fun8"

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

}

class UserActivityChild(): AppCompatActivity() {

    fun sendMessage(inputText: EditText, linearLayout: LinearLayout, context: Context , sUser: String? , sUKey: String?, sId : String?) {

        //luam inputul din textbox linie cu linie
        val fullInputText = inputText.text.toString().lines()

        //transformam textul luat linie cu linie in multiple line(una sub alta)
        val finalText = fullInputText.joinToString("\n")

        //afisam un card cu inputul din textVar
        CardViews().sendMessageCard(finalText, Time().getCurrentTime(), linearLayout, context)

        //triitem inputul din textVar la Transmission , apoi la server
        Transmission.sendMessage(sUser, sUKey, finalText, sId) //trimitem mesajul
        Log.d("MESSAGE SENT", "MESSAGE: $finalText")

        //stergem textul din caseta unde introducem text
        inputText.text.clear()

    }

    @SuppressLint("InflateParams")
    fun openSettings(constraintLayout: ConstraintLayout, usernameTextView: TextView, context: Context, db: MasterDb, sUser: String?, sId : String?, sUsername: String?) {

        //TODO: DE CONTINUAT CE A MAI RAMAS

        //TOATE SETARILE INDIVIDUALE PENTRU PRIETENI
        val inflater = LayoutInflater.from(ContextThemeWrapper(context, R.style.Theme_gamma)) // ContextWrapper ne ofera posibilitatea de a impacheta contextul aplicatiei cu o tema specifica!
        val settingsInterface = inflater.inflate(R.layout.settings_users , null) as ViewGroup

        val goBack: ImageView = settingsInterface.findViewById(R.id.goBack)
        val removeFriend: Button = settingsInterface.findViewById(R.id.removeFriend)
        val exportChat: Button = settingsInterface.findViewById(R.id.exportChat)
        val changeUsernameFriend: Button = settingsInterface.findViewById(R.id.changeUsernameF)
        val relativeLayout: RelativeLayout = settingsInterface.findViewById(R.id.relativeLayout)
        //val image: ImageView = card.findViewById(R.id.cardImage) //in image vom adauga src imagiea de profil a prietenului , vedem cum o adaugam (unde stocam imaginea in prima faza!)
        settingsInterface.findViewById<TextView>(R.id.usernameDisplayText).text = sUsername

        //luam din db statusul fiecarui switch de mai jos
        val blockState = db.getBlockState(sUser)
        val blockSwitch: MaterialSwitch = settingsInterface.findViewById(R.id.switchBlock)
        if(blockState == 0) {
            blockSwitch.isChecked = false
        }else {
            blockSwitch.isChecked = true
        }

        val notifyState = db.getNotifyState(sUser)
        val notifySwitch: MaterialSwitch = settingsInterface.findViewById(R.id.switchNotifications)
        if(notifyState == 0) {
            notifySwitch.isChecked = false
        }else {
            notifySwitch.isChecked = true
        }


        blockSwitch.setOnCheckedChangeListener { _, isChecked ->

            if(isChecked) {
                db.updateBlockState(sUser, 1)
            }else {
                db.updateBlockState(sUser, 0)
            }

        }

        notifySwitch.setOnCheckedChangeListener { _, isChecked ->

            if(isChecked) {
                db.updateNotifyState(sUser , 1)
            }else {
                db.updateNotifyState(sUser , 0)
            }

        }

        goBack.setOnClickListener {

            constraintLayout.removeView(settingsInterface)

        }

        removeFriend.setOnLongClickListener {

            Transmission.refuseRequest(sUser!!); if(context is Activity) { context.finish() }

            true
        }

        changeUsernameFriend.setOnClickListener {

            if(changeUsernameBoolean) {

                changeUsernameBoolean = false

                CardViews().changeUsername(context, relativeLayout , usernameTextView , settingsInterface , db , sUser , sId)

            }

        }

        exportChat.setOnClickListener {

            // TODO: O SA ADAUGAM O FUNCTIE SA CITEASCA TOT CHAT HISTORY (DOAR TEXT)
            // TODO: AICI VA FI PRIMA INTERACTIUNE CU ANDROID FILES STUFF , TREBUIE SA CREEAM UN FISIER .TXT SI STOCAT IN DOWNLOADS/DOCUMENTS(VEDEM)

        }

        constraintLayout.addView(settingsInterface)

    }

}

class CardViews(): AppCompatActivity() {

    // CARDUL ESTE COMPUS DIN: TEXT_VIEW(TEXTUL TRIMIS/PRIMIT) + TEXT_VIEW(MESSAGE_DATE)
    // TODO: ADAUGA MESSAGE DELETE , DOAR PT USERUL LOCAL!

    @SuppressLint("InflateParams", "MissingInflatedId")
    fun sendMessageCard(messageText: String? , messageDateText: String?, activityLayout: LinearLayout, context: Context) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.chat_card , null)

        val card: CardView = cardLayout.findViewById(R.id.cardView)
        val cardText: TextView = cardLayout.findViewById(R.id.cardTextB)
        val cardDateText: TextView = cardLayout.findViewById(R.id.dateText)

        card.setCardBackgroundColor(context.getColorStateList(R.color.light_blue))

        cardText.text = messageText
        cardDateText.text = messageDateText

        activityLayout.addView(cardLayout)

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

    @SuppressLint("InflateParams") //cu aceasta functie schimbam usernamul la friends
    fun changeUsername(context: Context, relativeLayout: RelativeLayout, userTextActivity: TextView, settingsInterface: ViewGroup, db: MasterDb, sUser: String?, sId: String?) {

        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.username_changer , null) as ViewGroup

        val changeButton: Button = cardLayout.findViewById(R.id.change)
        val constraintFather: ConstraintLayout = cardLayout.findViewById(R.id.constraintFather)
        val constraintChild: ConstraintLayout = cardLayout.findViewById(R.id.constraintChild)

        changeButton.setOnClickListener{

            val newUsername: String = cardLayout.findViewById<EditText>(R.id.inputText).text.toString()

            if (newUsername.isNotEmpty() && ' ' !in newUsername) {

                settingsInterface.findViewById<TextView>(R.id.usernameDisplayText).text = newUsername
                db.updateUsernameIdMain(sUser, newUsername, sId)
                userTextActivity.text = newUsername

            }

            relativeLayout.removeView(cardLayout); changeUsernameBoolean = true

        }

        constraintFather.setOnClickListener {// DONE: CU ACEST LISENER DISTRUGEM INTERFATA AFISATA("cardLayout");

            relativeLayout.removeView(cardLayout); changeUsernameBoolean = true

        }; constraintChild.setOnClickListener { }// TODO: "DOSE NOTHING , ITS HERE SO THE FATHER DOESN'T KILL HIM"

        relativeLayout.addView(cardLayout)
    }

    @SuppressLint("InflateParams")
    fun mainUiCard(dataVector: MutableList<String?>, activityLayout: LinearLayout, context: Context , constraintLayout: ConstraintLayout) {

        //am folosit inflater ca sa utilizam acceasi interfata a cardului din fisierul main_conn_card de mai multe ori
        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.main_conn_card , null) as ViewGroup

        //layoutul meniului cu butoane , il dam functiei sa il afiseze pe ecran
        val buttonLayout = inflater.inflate(R.layout.conn_main_remove_block_prompt , null) as ViewGroup

        //luam cardul pentru a gasi textul si a adauga o imagine in el
        val card: CardView = cardLayout.findViewById(R.id.mainConnCard)

        //val image: ImageView = card.findViewById(R.id.cardImage) //in image vom adauga src imagiea de profil a prietenului , vedem cum o adaugam (unde stocam imaginea in prima faza!)
        val cardUsername: TextView = card.findViewById(R.id.cardText)
        val buildText: String? = dataVector[3] //username friend

        cardUsername.text = buildText
        card.setOnClickListener {

            val intent = Intent("InterfaceActivity")

            //asa transferam informatii activitati , prin intent.putExtra
            sUser = dataVector[0]
            sUKey = dataVector[1]
            sUsername = dataVector[3]
            sId = dataVector[4]

            //ca sa putem deschide o noua activitate dintro functie externa
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        }

        card.setOnLongClickListener {

            if(cardMainBoolean) {

                cardMainBoolean = false

                //chemam o functie ca sa putem adauga buttonLayout mai ordonat , decat sa punem totul aici
                removeFromFriendsCard(dataVector[3], dataVector[0], activityLayout, buttonLayout, cardLayout , constraintLayout)

            }

            //setOnLongClickLiserner ii trebue expresie lambda , trebuie sa returneze ceva ca sa o putem folosi
            true
        }

        activityLayout.addView(cardLayout)
    }

    @SuppressLint("InflateParams")
    fun connUiCard(dataVector: MutableList<String?>, activityLayout: LinearLayout, context: Context , constraintLayout: ConstraintLayout) {

        //am folosit inflater ca sa utilizam acceasi interfata a cardului din fisierul main_conn_card de mai multe ori
        val inflater = LayoutInflater.from(context)
        val cardLayout = inflater.inflate(R.layout.main_conn_card , null) as ViewGroup

        //layoutul meniului cu butoane , il dam functiei sa il afiseze pe ecran
        val buttonLayout = inflater.inflate(R.layout.conn_main_remove_block_prompt , null) as ViewGroup

        //luam cardul pentru a gasi textul si a adauga o imagine in el
        val card: CardView = cardLayout.findViewById(R.id.mainConnCard)

        //val image: ImageView = card.findViewById(R.id.cardImage) //in image vom adauga src imagiea de profil a prietenului , vedem cum o adaugam (unde stocam imaginea in prima faza!)
        val cardUsername: TextView = card.findViewById(R.id.cardText)
        val buildText: String = dataVector[0] + " WITH ID:" + dataVector[2]

        cardUsername.text = buildText
        card.setOnClickListener {

            if(cardConnBoolean) {

                cardConnBoolean = false

                //chemam o functie ca sa putem adauga buttonLayout mai ordonat , decat sa punem totul aici
                connAcceptRefuseCard(dataVector[3], dataVector[0], activityLayout, buttonLayout, cardLayout, constraintLayout)

            }

        }

        card.setOnLongClickListener {

            if(cardConnBoolean) {

                cardConnBoolean = false

                //chemam o functie ca sa putem adauga buttonLayout mai ordonat , decat sa punem totul aici
                blockConnRequest(dataVector[0]!!, activityLayout, buttonLayout, cardLayout, constraintLayout)

            }

            //setOnLongClickLiserner trebuie sa returneze ceva ca sa il putem folosi
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

            //acceptam conn requestul aici
            Transmission.acceptRequest(userEmail)

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        denyButton.setOnClickListener {

            //refuzam conn requestul aici
            Transmission.refuseRequest(userEmail)

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        constraintFather.setOnClickListener {//buttonLayout father

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

            //acceptam conn requestul aici
            Transmission.refuseRequest(userEmail)

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardMainBoolean = true
        }

        goBack.setOnClickListener {

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
            constraintLayout.removeView(buttonLayout)

            cardMainBoolean = true
        }

        constraintFather.setOnClickListener {//buttonLayout father

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

            //acceptam conn requestul aici
            Transmission.blockRequest(userEmail)

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
            constraintLayout.removeView(buttonLayout)
            activityLayout.removeView(cardLayout)

            cardConnBoolean = true
        }

        goBack.setOnClickListener {

            //distrugem buttonLayout si cardul creat butoanelor, pentru ca nu ne mai trebuie
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

    //pt a verifica accesul la internet
    fun hasInternetAccess(context: Context): Boolean {

        Log.d("HasInternet","hasInternetAccess")

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    }

}

class Time {  // TODO: tot nu avem timezone , lasam asa pentru moment

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