package com.mca.gamma
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
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
                //////////////////////////
                Transmission.syncActionB()

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

        socket.on(channel + "STATUS") { args -> // lisens for status requests and send your status
            val data = args[0] as JSONObject
            val request = data.getString("senderRequest")
            val senderEmail = data.getString("senderEmail")
            val connCode : String = data.getString("connCode")

            when (request) {

                "statusRequest" -> {

                    Log.d("statusRequest" , "USED BY: $senderEmail")
                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "stb_request")

                    }

                    socket.emit("activity_?", pack)

                }

                "request_received" -> {

                    Log.d("request_received" , "USED BY: $senderEmail")
                    val access: Boolean = db.validateUser(senderEmail , connCode)

                    if(access) {

                        db.updateStatusTodoTypeMain(senderEmail , "INACTIVE" , "NOTHING" , "REQUEST_INTERNAL")

                    }

                }

                "arb_standby" -> {

                    Log.d("arb_standby" , "USED BY: $senderEmail")

                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "stb_arb")

                    }

                    socket.emit("activity_?", pack)

                }

                "request_accepted_received" -> {

                    Log.d("request_accepted_received" , "USED BY: $senderEmail")
                    val access: Boolean = db.validateUser(senderEmail , connCode)

                    if(access) {

                        db.updateStatusTodoTypeMain(senderEmail , "CONNECTED" , "NOTHING" , "NOTHING")


                    }

                }

                "request_denied_received" -> {

                    Log.d("request_denied_received" , "USED BY: $senderEmail")
                    val valid: Boolean = db.findUserInMain(senderEmail)

                    if(valid) {

                        db.deleteFromMain(senderEmail)

                    }

                }

                "request_block_received" -> {

                    Log.d("request_block_received" , "USED BY: $senderEmail")
                    val access: Boolean = db.validateUser(senderEmail , connCode)

                    if(access) {

                        db.updateStatusTodoTypeMain(senderEmail, "INACTIVE", "NOTHING" , "BLOCKED_INTERNAL")

                    }

                }

                "statusMessage" -> {

                    Log.d("statusMessage" , "REQUESTED BY USER: $senderEmail")

                    val access = db.validateUser(senderEmail, connCode)

                    if (access) {

                        Log.d("statusMessage" , "VERF SUCCESS")
                        val pack = JSONObject().apply {

                            put("senderEmail", localUserEmail)
                            put("receiverEmail", senderEmail)
                            put("serverAccessCode", serverAccessCode)
                            put("connCode", connCode)
                            put("senderRequest", "stb_messages")

                        }

                        socket.emit("activity_?", pack)
                    }
                    else {

                        Log.d("statusMessage" , "NO ACCESS")

                    }

                }

            }

        }

        socket.on(channel + "STAND_BY") { args -> // lisens for standBy and imOnline
            val data = args[0] as JSONObject
            val senderRequest = data.getString("senderRequest")
            val senderEmail = data.getString("senderEmail")
            val connCode = data.getString("connCode")

            when(senderRequest) {

                "imOnline" -> {

                    Log.d("imOnline" , "USED BY: $senderEmail")

                    val access: Boolean = db.validateUser(senderEmail, connCode)
                    val restrictionType: String = db.getTypeFromMain(senderEmail)

                    if(access && restrictionType == "NOTHING") {

                        Log.d("USER ONLINE: ", senderEmail)
                        Standby().messageStandBy(senderEmail , connCode , getContext()!!)

                    } else if(access && restrictionType == "BLOCKED_INTERNAL")  {

                        val pack = JSONObject().apply {

                            put("senderUsername", localUsername)
                            put("senderEmail", localUserEmail)
                            put("receiverEmail", senderEmail)
                            put("serverAccessCode", serverAccessCode)
                            put("connCode", connCode)
                            put("senderId", localId)
                            put("senderRequest", "BLOCKED_EXTERNAL")

                        }

                        socket.emit("arb_request" , pack)

                    } else {

                        val pack = JSONObject().apply {

                            put("senderEmail", localUserEmail)
                            put("receiverEmail", senderEmail)
                            put("serverAccessCode", serverAccessCode)
                            put("connCode", connCode)
                            put("senderRequest", "request_denied_received")

                        }

                        socket.emit("activity_?" , pack)
                    }

                }

                "stb_request" -> {

                    Log.d("stb_request" , "USED BY: $senderEmail")

                    val access: Boolean = db.validateUser(senderEmail , connCode)
                    val todo: String = db.getTodoFromMain(senderEmail)

                    if(access && todo == "REQUEST") {

                        Standby().connRequestStandby(senderEmail , connCode)

                    }

                }

                "stb_arb" -> {

                    Log.d("stb_arb" , "USED BY: $senderEmail")
                    val access: Boolean = db.validateUser(senderEmail , connCode)

                    if(access) { Log.d("STB_ARB_STANDBY", "ACCESSED BY: $senderEmail")

                        Standby().arbStandby(senderEmail ,connCode , getContext()!!)

                    }

                }

                "stb_messages" -> {

                    Log.d("stb_messages" , "REQUESTED BY USER: $senderEmail")

                    val access: Boolean = db.validateUser(senderEmail , connCode)

                    if(access) {

                        Log.d("stb_messages" , "VERIFICATION SUCCESS")
                        Standby().messageStandBy(senderEmail , connCode , getContext()!!)

                    }
                    else {

                        Log.d("stb_messages" , "VERIFICATION FAILED") //ar trebui sa trimitem un raspuns inapoi userului , vom vedea

                    }

                }

            }

        }

        socket.on(channel + "GET_REQUEST") { args -> // listen for conn requests
            val data = args[0] as JSONObject
            val senderUsername = data.getString("senderUsername")
            val senderRequest = data.getString("senderRequest")
            val senderEmail = data.getString("senderEmail")
            val senderId = data.getString("senderId")
            val connCode = data.getString("connCode")

            Log.d("GET_REQUEST" , "USED BY: $senderEmail")
            val valid: Boolean = db.findUserInMain(senderEmail)
            val restrictionType: String = db.getTypeFromMain(senderEmail)
            val validMain: Boolean = db.validateUser(senderEmail , connCode)

            if (!valid && senderRequest == "CONN?") {

                Log.d("GET_REQUEST" , "CONN VALID FOR: $senderEmail")
                db.insertIntoMain(senderEmail , connCode , "INACTIVE" , senderUsername , senderId , "NOTHING" , "REQUEST_EXTERNAL" , 0 , 0)

                if(permitObjConn) {

                    val classLayout = getLinearLayout()
                    val classConstraint = getConstraintLayout()
                    val classContext = getContext()

                    runOnUiThread {

                        CardViews().connUiCard(mutableListOf(senderEmail, connCode , senderId, senderUsername, "INACTIVE" , "REQUEST_EXTERNAL"), classLayout!!, classContext!!, classConstraint!!)

                    }

                }

                val pack = JSONObject().apply {

                    put("senderEmail", localUserEmail)
                    put("receiverEmail", senderEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderRequest", "request_received")

                }

                socket.emit("activity_?" , pack)

            }
            else if((validMain || valid) && restrictionType == "BLOCKED_INTERNAL") {

                Log.d("GET_REQUEST" , "CONN BLOCKED FOR: $senderEmail")
                val pack = JSONObject().apply {

                    put("senderEmail", localUserEmail)
                    put("receiverEmail", senderEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderRequest", "request_denied_received")

                }

                socket.emit("activity_?" , pack)

            }
            else if(validMain && senderRequest == "CONN?") {

                Log.d("GET_REQUEST" , "CONN VALID RESENT TO: $senderEmail")
                db.updateStatusTodoTypeMain(senderEmail , "INACTIVE" , "NOTHING" , "REQUEST_EXTERNAL")

                val pack = JSONObject().apply {

                    put("senderEmail", localUserEmail)
                    put("receiverEmail", senderEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderRequest", "request_received")

                }

                socket.emit("activity_?" , pack)

            }
            else if(valid && db.getStatusMain(senderEmail) == "CONNECTED") {

                db.updateConnCodeMain(senderEmail , connCode)

                val pack = JSONObject().apply {

                    put("senderEmail", localUserEmail)
                    put("receiverEmail", senderEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderRequest", "request_accepted_received")

                }

                socket.emit("activity_?" , pack)

            }

        }

        socket.on(channel + "PROCESS_REQUEST") { args ->
            val data = args[0] as JSONObject
            val senderUsername = data.getString("senderUsername")
            val senderRequest = data.getString("senderRequest")
            val senderEmail = data.getString("senderEmail")
            val senderId = data.getString("senderId")
            val connCode = data.getString("connCode")

            Log.d("PROCESS_REQUEST" , "USED BY USER: $senderEmail")
            val access: Boolean = db.validateUser(senderEmail , connCode)
            val restrictionType: String = db.getTypeFromMain(senderEmail)
            val userStatus: String = db.getStatusMain(senderEmail)


            if(restrictionType == "NOTHING" || restrictionType == "REQUEST_INTERNAL") {

                if (access && senderRequest == "REQUEST_ACCEPTED" && userStatus == "INACTIVE") {

                    Log.d("PROCESS_REQUEST" , "USER ACCEPTED REQUEST: $senderEmail")
                    db.updateStatusTodoTypeMain(senderEmail, "CONNECTED", "NOTHING" , "NOTHING")
                    db.updateUsernameIdMain(senderEmail , senderUsername , senderId)
                    db.tableCreatorUser(senderId)

                    if (permitObjMain) {

                        val classLayout = getLinearLayout()
                        val classConstraint = getConstraintLayout()
                        val classContext = getContext()

                        runOnUiThread {

                            CardViews().mainUiCard(mutableListOf(senderEmail, connCode, senderUsername, senderId), classLayout!!, classContext!! , classConstraint!!)

                        }

                    }

                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "request_accepted_received")

                    }

                    socket.emit("activity_?" , pack)

                }
                else if (access && senderRequest == "REQUEST_ACCEPTED" && userStatus == "CONNECTED") {

                    Log.d("PROCESS_REQUEST" , "RESENT ACCEPTED REQUEST: $senderEmail")
                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "request_accepted_received")

                    }

                    socket.emit("activity_?" , pack)

                }
                else if (access && senderRequest == "REQUEST_DENIED") {

                    Log.d("PROCESS_REQUEST" , "REQUEST DENIED BY USER: $senderEmail")
                    db.deleteFromMain(senderEmail)

                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "request_denied_received")

                    }

                    socket.emit("activity_?" , pack)

                    //**

                }
                else if (access && senderRequest == "BLOCKED_EXTERNAL") {

                    Log.d("PROCESS_REQUEST" , "BLOCKED EXTERNAL BY USER: $senderEmail")
                    db.updateStatusTodoTypeMain(senderEmail ,"INACTIVE" , "BLOCKED_EXTERNAL" , "NOTHING")

                    val pack = JSONObject().apply {

                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderRequest", "request_block_received")

                    }

                    socket.emit("activity_?" , pack)

                }

            }
            else {

                val pack = JSONObject().apply {

                    put("senderEmail", localUserEmail)
                    put("receiverEmail", senderEmail)
                    put("serverAccessCode", serverAccessCode)
                    put("connCode", connCode)
                    put("senderRequest", "request_denied_received")

                }

                socket.emit("activity_?" , pack)

            }

        }

        socket.on(channel + "RECEIVE_MESSAGE") { args ->
            val data = args[0] as JSONObject
            val senderEmail = data.getString("senderEmail")
            val message = data.getString("message")
            val messageId = data.getString("messageId")
            val connCode = data.getString("connCode")
            val senderId = data.getString("senderId")
            val messageTimeStamp = data.getString("messageTimeStamps")

            Log.d("MSG_LISTENER", "USED BY: $senderEmail WITH MESSAGE: $message")
            val access: Boolean = db.validateUser(senderEmail , connCode)
            val restrictionType: String = db.getTypeFromMain(senderEmail)

            if(access && restrictionType != "BLOCKED_INTERNAL") {

                Log.d("MSG","FROM: $senderEmail")
                val classLayout = getLinearLayout()
                val classContext = getContext()

                if (!senderId.isNullOrEmpty()) {

                    db.insertIntoUser(senderId, senderEmail, message, "received+" , Time().convertTS(messageTimeStamp) , messageId)

                    if(permitObjUser && globalSpecificUser == senderEmail) {

                            // TODO: ADD THE DATE TO MESSAGE
                            runOnUiThread { CardViews().receiveMessageCard(message , messageTimeStamp, classLayout!!, classContext!!) }

                    }

                    val emptyList: MutableList<String> = mutableListOf("NULL")

                    val packBack = JSONObject().apply {

                        put("message", message)
                        put("senderRequest", "received+")
                        put("messageId" , messageId)
                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderId", localId)
                        put("messageList", JSONArray(emptyList))
                        put("messageIdList" , JSONArray(emptyList))
                        put("messageListSize" , "NULL")

                    }
                    socket.emit("check_message_sent?", packBack)
                    Log.d("MESSAGE_STATUS_BACK" , "MESSAGE RECEIVED")

                }

            } // TODO: ADAUGA DACA OMU E BLOCKED SAU NU EXISTA

        }

        socket.on(channel + "RECEIVE_BULK_MESSAGE") { args ->
            val data = args[0] as JSONObject
            val senderEmail = data.getString("senderEmail")
            val connCode = data.getString("connCode")
            val senderId = data.getString("senderId")
            val messageIdList = data.getJSONArray("messageIdList")
            val messages = data.getJSONArray("messageList")
            val messageTS = data.getJSONArray("messageTimeStamps")
            val messagesListSize = data.getString("messageListSize")

            Log.d("MESSAGES_MSG" , "USED BY $senderEmail")
            val access : Boolean = db.validateUser(senderEmail , connCode)
            val restrictionType: String = db.getTypeFromMain(senderEmail)

            if(access && restrictionType != "BLOCKED_INTERNAL") {

                Log.d("MESSAGES_MSG", "USED BY $senderEmail , HAS ACCESS")
                val classLayout = getLinearLayout()
                val classContext = getContext()

                if (!senderId.isNullOrEmpty()) {

                    for (i in 0 until messagesListSize.toInt() step 1) {

                        db.insertIntoUser(senderId, senderEmail, messages[i].toString(), "received+", Time().convertTS(messageTS[i].toString()) , messageIdList[i].toString())
                        if(permitObjUser && globalSpecificUser == senderEmail) {

                            // TODO: ADD THE DATE TO MESSAGE
                            runOnUiThread { CardViews().receiveMessageCard(messages[i].toString(), messageTS[i].toString(), classLayout!!, classContext!!) }

                        }

                    }

                    val packBack = JSONObject().apply {

                        put("messageList", messages)
                        put("messageListSize", messagesListSize)
                        put("messageIdList" , messageIdList)
                        put("messageId" , "NULL")
                        put("senderRequest", "stb_messages_received")
                        put("senderEmail", localUserEmail)
                        put("receiverEmail", senderEmail)
                        put("serverAccessCode", serverAccessCode)
                        put("connCode", connCode)
                        put("senderId", localId)
                        put("message", "NULL")

                    }
                    socket.emit("check_message_sent?", packBack)

                    Log.d("MESSAGES_STATUS_BACK", "MESSAGES RECEIVED")

                }

            } // TODO: ADAUGA DACA OMU E BLOCKED SAU NU EXISTA

        }

        socket.on(channel + "CHECK_MESSAGE_SENT?") { args ->
            val data = args[0] as JSONObject
            val senderEmail = data.getString("senderEmail")
            val response = data.getString("senderRequest")
            val connCode = data.getString("connCode")
            val senderId = data.getString("senderId")
            val message = data.getString("message")
            val messageId = data.getString("messageId")
            val messageList = data.getJSONArray("messageList")
            val messageIdList = data.getJSONArray("messageIdList")
            val messageListSize = data.getString("messageListSize")

            Log.d("MSG_SENT?" , "USED BY $senderEmail WITH RESPONSE: $response")
            val access : Boolean = db.validateUser(senderEmail , connCode)
            val restrictionType: String = db.getTypeFromMain(senderEmail)

            if(access && restrictionType != "BLOCKED_INTERNAL") {

                Log.d("MSG?" , "USED BY $senderEmail WITH RESPONSE: $response , HAS ACCESS")

                when(response) {

                    "received+" -> {

                        db.updateMessageStatus(senderId, localUserEmail!!, message, messageId , "received+")

                    }
                    "received++" -> {

                        db.updateMessageStatus(senderId, localUserEmail!!, message, messageId , "received++") //asta este aici pentru viitor cand vom implementa un mecanism de view message , adica daca repsctivul user iti vede mesajul iti va raporta inapoi ca la vazut !

                    }
                    "stb_messages_received" -> {

                        Log.d("MESSAGES_msg" , "RECEIVED+++ , list size is: ${messageListSize.toInt()}")

                        for(i in 0 until  messageListSize.toInt() step 1) {

                            db.updateMessageStatus(senderId, localUserEmail!!, messageList[i].toString(), messageIdList[i].toString() , "received+")

                        }

                    }

                }

            }

        }

    }

    fun sendAny(event: String , data: JSONObject) {
        Log.d("SEND ANY:", event)

        if(permitObjInternetAcess) {

            socket.emit(event , data)

        }

    }

    fun haveRequest(toUser: String?) {
        Log.d("HAVE REQUEST FOR USER: " , toUser!!)

        val check = JSONObject().apply {

            put("senderEmail", localUserEmail)
            put("receiverEmail", toUser)
            put("serverAccessCode", serverAccessCode)
            put("senderRequest", "statusRequest")
            put("connCode" , db.getConnCodeFromMain(toUser))

        }

        socket.emit("activity_?", check)

    }

    fun updateRequestARB(toUser: String?) {
        Log.d("UPDATE REQUEST TO USER: ", toUser!!)

        val check = JSONObject().apply {

            put("senderEmail", localUserEmail)
            put("receiverEmail", toUser)
            put("serverAccessCode", serverAccessCode)
            put("senderRequest", "arb_standby")
            put("connCode" , db.getConnCodeFromMain(toUser))

        }

        socket.emit("activity_?", check)

    }

    fun haveMessage(toUser: String?) {
        Log.d("HAVE MESSAGES FOR USER:" , toUser!!)

        val check = JSONObject().apply {

            put("senderEmail", localUserEmail)
            put("receiverEmail", toUser)
            put("serverAccessCode", serverAccessCode)
            put("senderRequest", "statusMessage")
            put("connCode" , db.getConnCodeFromMain(toUser))

        }

        socket.emit("activity_?", check)

    }

    fun sendRequest(toUser: String?, connCode: String) {

        Log.d("SEND_REQUEST" , "TO USER: $toUser")
        val valid: Boolean = db.findUserInMain(toUser)

        if(!valid) {

           db.insertIntoMain(toUser , connCode , "STAND_BY" , "UNKNOWN" , "UNKNOWN" , "REQUEST" , "REQUEST_INTERNAL" , 0 , 0)

           if(permitObjInternetAcess) {

               haveRequest(toUser)

           }

        }

    }

    fun acceptRequest(toUser: String?) {

        Log.d("ACCEPT_REQUEST" , "TO USER: $toUser")
        val valid: Boolean = db.findUserInMain(toUser)

        if(valid) {

            db.updateStatusTodoTypeMain(toUser , "STAND_BY" , "REQUEST_ACCEPTED" , "NOTHING")
            db.tableCreatorUser(db.getIdFromMain(toUser))

            if(permitObjInternetAcess) {

                updateRequestARB(toUser)

            }

        }

    }
    fun refuseRequest(toUser: String?) { //asta se foloseste si la eliminarea prietenilor

        Log.d("REFUSE_REQUEST" , "TO USER: $toUser")
        val valid: Boolean = db.findUserInMain(toUser)

        if(valid) {

            db.updateStatusTodoTypeMain(toUser , "STAND_BY" , "REQUEST_DENIED" , "NOTHING")

            if(permitObjInternetAcess) {

                updateRequestARB(toUser)

            }

        }

    }

    fun blockRequest(toUser: String?) {

        Log.d("BLOCK_REQUEST" , "TO USER: $toUser")
        val valid: Boolean = db.findUserInMain(toUser)

        if(valid) {

            db.updateStatusTodoTypeMain(toUser , "STAND_BY" , "BLOCKED_INTERNAL" , "BLOCKED_INTERNAL")

            if(permitObjInternetAcess) {

                updateRequestARB(toUser)

            }

        }

    }

    fun imOnline(toUser: String?) {

        val statusCheck = db.getStatusMain(toUser)

        if(statusCheck == "CONNECTED") {

            val pack = JSONObject().apply {

                put("senderEmail", localUserEmail)
                put("receiverEmail", toUser)
                put("serverAccessCode", serverAccessCode)
                put("connCode", db.getConnCodeFromMain(toUser))
                put("senderRequest", "imOnline")

            }

            socket.emit("activity_?" , pack)

        }

    }

    fun sendMessage(toUser: String?, connCode: String?, yourMessage: String?, friendId : String?) {

        val currentTime = Time().getCurrentTime()
        val messageId = Random().genRandomCode(14)

        val message = JSONObject().apply {

            put("message", yourMessage)
            put("receiverEmail", toUser)
            put("messageTimeStamps" , currentTime)
            put("senderEmail", localUserEmail)
            put("serverAccessCode", serverAccessCode)
            put("connCode", connCode)
            put("senderId", localId)
            put("messageId", messageId)

        }

        if(permitObjInternetAcess) {

            Log.d("MSG SENT", "MSG SENT ONLINE")
            db.insertIntoUser(friendId, localUserEmail!!, yourMessage, "STAND_BY" , currentTime , messageId)
            socket.emit("send_message", message)

        }
        else {

            Log.d("MSG SENT", "MSG SENT OFFLINE")
            db.insertIntoUser(friendId, localUserEmail!!, yourMessage, "STAND_BY" , currentTime , messageId)

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

    fun syncActionB() {

        Log.d("COROUTINE_B / FUN B","RUNNING")

        val standByUsers = db.findStandByMain()

        var i = 0
        repeat(standByUsers.size) {

            val getTodo = db.getTodoFromMain(standByUsers[i])

            if(getTodo == "REQUEST") {

                haveRequest(standByUsers[i])

            } else {

                updateRequestARB(standByUsers[i])

            }

            i++

        }

        i = 0; val userEmail = db.mainLoader()
        //trimitem toate mesajele care avem sa le dam prietenilor folosindune de USER_EMAIl
        repeat(userEmail.size) {

            val have = db.findMessageStandBy(userEmail[i][4])

            if (have) {

                haveMessage(userEmail[i][0])

            }

            i++

        }

        i = 0
        //anuntam toti prieteni ca suntem online
        repeat(userEmail.size) {

            imOnline(userEmail[i][0])

            i++

        }

        Log.d("COROUTINE_B" , "END")

    }

}


/*

    private fun createInsecureSocket(url: String): Socket {
        val trustManager = InsecureTrustManager()
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustManager), SecureRandom())

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        return IO.socket(url, IO.Options().apply {
            callFactory = client
            webSocketFactory = client
        })
    }

    @SuppressLint("CustomX509TrustManager")
    private class InsecureTrustManager : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

*/
