package com.mca.gamma
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*


/* Todo:
Master Comment:
-> At the end <when we finish the application, we will have all the comments in English!>
*/

// MasterActivity, the beginning of application and initializations , SETUP()
class MasterActivity : Application() {

    override fun onCreate() {
        super.onCreate()

        // take the values stored in Android_local_storage
        val key = AndroidLocalStorage(applicationContext)

        serverAddress = key.getHP()
        if(!serverAddress.isNullOrEmpty()) {

            holdServerAddress = true

        }

        localUserEmail = key.getEmail()
        serverAccessCode = key.getSecureCode()
        localUsername = key.getUsername()
        localId = key.getId()

        // here we record the object/class Connectivity as Brodcast Received (will monitor network changes)
        val connectivityReceiver = Connectivity
        val filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(connectivityReceiver, filter)

        // we check if the user is connected and we initialize transmission and coroutineA
        if(!serverAccessCode.isNullOrEmpty()) {

            Log.d("SERVER" , "USER CONNECTED TO: $serverAddress")

            Transmission.addContext(applicationContext)
            Transmission.start()
            coroutineA.start(); permitActivityAfterLogin = false

        }

        Log.d("APP" , "LOCAL USER CONNECTED , EMAIL: $localUserEmail + USERNAME: $localUsername + ID: $localId + SAC: $serverAccessCode")

    }

    private val coroutineA = CoroutineScope(Dispatchers.Main).launch {

        Log.d("COROUTINE_A","RUNNING")
        val db = MasterDb(applicationContext)
        val standByUsers = db.findStandByMain()

        var i = 0
        repeat(standByUsers.size) {
            val getTodo = db.getTodoFromMain(standByUsers[i])
            if(getTodo == "REQUEST") {
                Transmission.haveRequest(standByUsers[i])
            } else { Transmission.updateRequestARB(standByUsers[i]) }
            i++

        }

        i = 0; val userEmail = db.mainLoader()
        // We send all the messages we have to give to friends using user_email
        repeat(userEmail.size) {
            val have = db.findMessageStandBy(userEmail[i][4])
            if (have) {
                Transmission.haveMessage(userEmail[i][0])
            }
            i++
        }

        i = 0
        // we announce all friends that we are online
        repeat(userEmail.size) {
            Transmission.imOnline(userEmail[i][0])
            i++
        }

        Log.d("COROUTINE_A" , "END")
    }

}


// IT PROMPTS TO MAIN_ACTIVITY OR SETUP
class InitActivity: AppCompatActivity() {

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // here we check whether the user is connected or not
        if(!permitActivityAfterLogin) {

            startActivity(Intent("MainActivity")); Log.d("INIT_ACTIVITY" ,"ACTIVITY_MAIN")

        } else if(!holdServerAddress) {

            runOnUiThread {

                setContentView(R.layout.server_address_activity)

                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

                val hostnameText: EditText = findViewById(R.id.hostnameText)
                val portText: EditText = findViewById(R.id.portText)
                val next: Button = findViewById(R.id.next)

                next.setOnClickListener {

                    next.isClickable = false
                    next.setBackgroundColor(resources.getColor(R.color.red))

                    val hostname = hostnameText.text.toString()
                    val port = portText.text.toString()

                    if(hostname.isNotEmpty() && port.isNotEmpty()) {

                        val key = AndroidLocalStorage(applicationContext)
                        serverAddress = "http://$hostname:$port" // HTTP FOR NOW , BACK HOME BE MODIFIED TO HTTPS!
                        key.saveHP("http://$hostname:$port")

                        startActivity(Intent("RegisterActivity")); Log.d("INIT_ACTIVITY", "ACTIVITY_REGISTER")

                    } else {

                        Toast.makeText(applicationContext ,"Hostname and port cannot be empty!", Toast.LENGTH_SHORT).show()

                        next.setBackgroundColor(resources.getColor(R.color.black))
                        next.isClickable = true

                    }

                }

            }

        } else {

            startActivity(Intent("RegisterActivity")); Log.d("INIT_ACTIVITY", "ACTIVITY_REGISTER")

        }

    }

    override fun onStop() {
        super.onStop()

        //ALWAYS DESTROYED ON STOP
        finish()

    }

}



// Register Class, here is the user registration in db of the server
class RegisterActivity : AppCompatActivity() {

    private val apiCall = CoroutineScope(Dispatchers.Main)

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val next : Button = findViewById(R.id.next)
        val toLogin : TextView = findViewById(R.id.goToLogin)
        val inputUsername : EditText = findViewById(R.id.usernameText)
        val inputEmail : EditText = findViewById(R.id.emailText)
        val inputPassword : EditText = findViewById(R.id.changeUsername)
        val goBack: CardView = findViewById(R.id.goBack)
        var serverResponse : String


        goBack.setOnClickListener {

            val key = AndroidLocalStorage(applicationContext)
            key.saveHP(null)
            restartApp()

        }

        next.setOnClickListener {

            // we close the action of the USER button
            next.isEnabled = false
            next.setBackgroundColor(resources.getColor(R.color.red))

            if (permitObjInternetAcess) {

                // we check if the data entered comply with the conditions
                if(inputUsername.length() in 4..30 && inputEmail.length() >= 5 && inputPassword.length() >= 8 && inputPassword.length() <= 50 && '@' in inputEmail.text.toString()) {

                    //Coroutine call for HTTPS calls
                    apiCall.launch {

                        withContext(Dispatchers.IO) {

                            serverResponse = Https().httpsFun4(inputUsername.text.toString(), inputEmail.text.toString(), inputPassword.text.toString())

                        }

                        when (serverResponse) {

                            "true" ->  {

                                if(savedInstanceState == null) {

                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentRegister, AccountCreatedActivity(applicationContext))
                                        .commitNow()

                                }

                            }
                            "false" -> {

                                // reopen the action of the user button
                                next.isEnabled = true
                                next.setBackgroundColor(resources.getColor(R.color.black))

                                // this is displayed if in the server the account already exists
                                Toast.makeText(applicationContext, "ERROR CREATING ACCOUNT!", Toast.LENGTH_SHORT).show()

                            }
                            "FAILED" -> {

                                // reopen the action of the user button
                                next.isEnabled = true
                                next.setBackgroundColor(resources.getColor(R.color.black))

                                //This is displayed if the server does not answer
                                Toast.makeText(applicationContext, "SERVER NOT RESPONDING" ,Toast.LENGTH_SHORT).show()

                            }

                        }
                    
                    }
                } else {

                    // for cases when the input at the user is not in accordance with the server
                    if(inputUsername.length() <= 3) {

                        Toast.makeText(this, "USERNAME TOO SHORT! , SHOULD BE BIGGER THAN 3 CHARACTERS", Toast.LENGTH_LONG).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    } else if(inputEmail.length() < 5) {

                        Toast.makeText(this, "EMAIL TOO SHORT!", Toast.LENGTH_SHORT).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    } else if(inputPassword.length() < 8) {

                        Toast.makeText(this, "PASSWORD TOO SHORT! , SHOULD BE BIGGER THAN 7 CHARACTERS", Toast.LENGTH_LONG).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    } else if(inputPassword.length() > 50) {

                        Toast.makeText(this, "PASSWORD TOO LONG! , SHOULD BE LESSER THAN 50 CHARACTERS", Toast.LENGTH_LONG).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    } else if('@' !in inputEmail.toString()) {

                        Toast.makeText(this, "INCORRECT EMAIL , SHOULD CONTAIN CHARACTER '@'", Toast.LENGTH_LONG).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    } else if(inputUsername.length() > 30) {

                        Toast.makeText(this , "USERNAME TOO LONG , MUST BE UNDER 30 CHARACTERS" , Toast.LENGTH_LONG).show()

                        // reopen the action of the user button
                        next.isEnabled = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    }

                }

            } else {

                Toast.makeText(this, "INTERNET CONNECTION LOST" , Toast.LENGTH_SHORT).show()

                // reopen the action of the user button
                next.isEnabled = true
                next.setBackgroundColor(resources.getColor(R.color.black))

            }

        }

        toLogin.setOnClickListener {

            if(permitObjInternetAcess) {

                if(savedInstanceState == null) {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentRegister, LoginActivity(applicationContext))
                        .commitNow()

                }

            }

        }

    }

    private fun restartApp() {

        holdServerAddress = false

        // Intent to restart the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) }
        if (intent != null) {
            startActivity(intent)
        }

        // Finish the current activity
        finish()

    }

}



//USER UI
class UserActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) { Log.d("USER_ACTIVITY" ,"CREATED")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        
        val sendMessageButton: ImageButton = findViewById(R.id.sendMessage)
        val loadProfileIcon: ImageView = findViewById(R.id.cardImage) // that when we add File Support (more Info in Mastervar)
        val userSettingsButton: LinearLayout = findViewById(R.id.userSettings)
        val inputText: EditText = findViewById(R.id.messageText)
        val usernameTextView: TextView = findViewById(R.id.usernameDisplayText)
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardsLayout)

        // the global variable that stores the user of the user with whom we communicate
        globalSpecificUser = sUser

        // We add in the object of the transmission classlinearlayout to be able to update the interface
        Transmission.addLayout(cardLinearLayout)

        // allows the object to perform actions in msg_lisener, after classlinearlayout has been added
        permitObjUser = true

        // we initialize the date of bases, we open an instance of it
        val db = MasterDb(applicationContext)

        // we enter text in the textViewcia box to display the friend's username
        usernameTextView.text = sUsername

        sendMessageButton.setOnClickListener { UserActivityChild().sendMessage(inputText , cardLinearLayout , applicationContext , sUser , sUKey , sId) }

        userSettingsButton.setOnClickListener {

            if(savedInstanceState == null) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentSettingsUsers, OpenSettingsUser(applicationContext))
                    .commitNow()

            }

        }

        // here we load all the messages with the friend in  messageArray for now
        val messageArray = db.messageLoader(sId)

        if(messageArray.size > 0) {

            for (i in messageArray.size - 1 downTo 0) {

                when (messageArray[i][0]) {

                    //The messages of the one who sent to the current user
                    "$sUser" -> { CardViews().receiveMessageCard(messageArray[i][1] , messageArray[i][3] , cardLinearLayout , applicationContext) }

                    // messages of the one using the current application
                    "$localUserEmail" -> { CardViews().sendMessageCard(messageArray[i][1] , messageArray[i][3] , cardLinearLayout , applicationContext) }

                }

            }

        }

    }

    fun buttonC(view: View) { finish() }

    override fun onStop() {
        super.onStop()

        // for any eventuality we replace globalspecificer with null
        globalSpecificUser = null

        // no longer allow the transmission object to perform actions in msg_liserner
        permitObjUser = false

    }

    override fun onStart() {
        super.onStart()

        if(backToParentFragment) {

            backToParentFragment = false

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSettingsUsers, OpenSettingsUser(applicationContext))
                .commitNow()

        }
        
    }

    override fun onRestart() { 
        super.onRestart()

        finish()
        startActivity(Intent("InterfaceActivity"))

    }

}



//MAIN UI
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.gri30)

        // We check if the user is first connected to open the transmission!        
        userOnFirstConnect()

        //cardLinearLayoutSiConstraintLayout
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardLinearLayoutMain)
        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)

        // give the object of Constraint Layout, to work Live Friend ACCEPT
        Transmission.addConstraint(constraintLayout)

        // give the object Linearlayout
        Transmission.addLayout(cardLinearLayout)

        // We open an instance of MasterDB
        val db = MasterDb(applicationContext)

        // we load all friends from DB in Main array
        val mainArray = db.mainLoader()

        // we display on the screen, through cards, all connected friends
        for(i in 0 until mainArray.size step 1) {

            val listA: MutableList<String?> = mutableListOf(mainArray[i][0] , mainArray[i][1] , mainArray[i][2] , mainArray[i][3] , mainArray[i][4])
            CardViews().mainUiCard(listA , cardLinearLayout , applicationContext , constraintLayout)

        }; cardMainBoolean = true

        // yes permission to the object Transmission to execute certain actions
        permitObjMain = true

    }

    override fun onStop() {
        super.onStop()

        // take permission to the Transmission object to execute certain actions
        permitObjMain = false

    }

    override fun onRestart() {
        super.onRestart()

        finish()
        startActivity(Intent("MainActivity"))

    }

    fun buttonA(view: View) { startActivity(Intent("ConnActivity")) }
    fun buttonB(view: View) { startActivity(Intent("GroupsActivity")) }
    fun buttonC(view: View) { startActivity(Intent("BlackListActivity")) }
    fun buttonD(view: View) { startActivity(Intent("SettingsActivity")) }

    private fun userOnFirstConnect() {

        // We check if the user is connected to the server or the code is executed only once in connection
        if(permitActivityAfterLogin && permitObjInternetAcess) {
            Log.d("MAIN PERMIT" , "TRANSMISSION ACTIVATED FOR FIRST TIME")

            Transmission.addContext(applicationContext)
            Transmission.start()
            permitActivityAfterLogin = false

        }

    }

}

// TODO: NOT COMPLETE 
class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val goBack: ImageView = findViewById(R.id.goBack)
        val changeUsername: Button = findViewById(R.id.changeUsername)
        val displayUsername: TextView = findViewById(R.id.usernameDisplayText)
        displayUsername.text = localUsername

        goBack.setOnClickListener {

            finish()

        }

        changeUsername.setOnClickListener {

            supportFragmentManager.beginTransaction()
                .replace(R.id.settingsFragments , ChangeUsernameLocalUser(applicationContext))
                .commitNow()

        }

    }

    override fun onRestart() {
        super.onRestart()

        finish()
        startActivity(Intent("SettingsActivity"))

    }

}

// CONN UI
class ConnActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conn)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.gri30)

        //val intent1 = Intent("com.example.ACTION_AC6")
        val buttonA: Button = findViewById(R.id.sendConnRequest)
        val inputConnRequest: EditText = findViewById(R.id.inputConnRequest)

        // Linearlayout and constraint layout for the whole class
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardLinearLayoutConn)
        val constraintLayout: ConstraintLayout = findViewById(R.id.connConstraintLayout)

        // Dam Constraintlayout object, to work Live Friend Request
        Transmission.addConstraint(constraintLayout)

        // we add in the object when we enter into activity so we can display on the interface
        Transmission.addLayout(cardLinearLayout)

        // we open a MasterDB instance
        val db = MasterDb(applicationContext)

        // Store in Conn array all conn external requests
        val connArray = db.connLoader(); Log.d("CREATE_CONN", "LOADER")

        // we display on the interface all conn_requests
        for(i in 0 until connArray.size step 1) {

            val listA : MutableList<String?> = mutableListOf(connArray[i][0] ,connArray[i][1], connArray[i][2], connArray[i][3] , connArray[i][4], connArray[i][5])
            CardViews().connUiCard(listA ,cardLinearLayout , applicationContext , constraintLayout)

        }; cardConnBoolean = true

        // allows the transmission object to make certain executing
        permitObjConn = true

        buttonA.setOnClickListener {

            val userInput = inputConnRequest.text.toString()

            //here we do conn_request
            Transmission.sendRequest(userInput, Random().genRandomCode(12))
            inputConnRequest.text.clear()

        }

    }

    override fun onStop() {
        super.onStop()

        // allows the transmission object to make certain executing
        permitObjConn = false

    }

    override fun onRestart() {
        super.onRestart()

        finish()
        startActivity(Intent("ConnActivity"))

    }

    fun buttonConn(view: View) { finish() }

}