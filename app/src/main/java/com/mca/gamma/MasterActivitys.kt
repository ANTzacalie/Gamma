@file:Suppress("OVERRIDE_DEPRECATION")

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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/* TODO:
    MASTER COMMENT:
    --> LA SFARSIT < CAND TERMINAM APLICATIA , VOM AVEA TOATE COMMENTURILE IN ENGLEZA! >
    --> PUTEM SCHIMBA LAYOUTUL IN ACTIVITATE cu setContentView de cate ori vrem , acu atunci cand o facem toata logica noului layout trebuie pusa in runOnUiThread{}
*/

//masterActivity , inceputul aplicatie si a initializarilor
class MasterActivity : Application() {

    override fun onCreate() {
        super.onCreate()

        //LUAM VALORILE STOCATE IN ANDROID_LOCAL_STORAGE
        val key = AndroidLocalStorage(applicationContext)
        localUserEmail = key.getEmail(); serverAccessCode = key.getSecureCode(); localUsername = key.getUsername(); localId = key.getId()

        //dam context obiectului Transmission , o singura data la inceput
        Transmission.addContext(applicationContext)

        //AICI INREGISTRAM OBIECTUL/CLASA CONNECTIVITY CA BRODCAST RECEIVED(VA MONITORIZA SCHIMBARILE DE RETEA)
        val connectivityReceiver = Connectivity
        val filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(connectivityReceiver, filter)

        //verificam daca userul este conectat si initializam transmission si coroutineA
        if(!serverAccessCode.isNullOrEmpty()) { Transmission.start(); coroutineA.start(); permitActivityAfterLogin = false }

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
        //trimitem toate mesajele care avem sa le dam prietenilor folosindune de USER_EMAIl
        repeat(userEmail.size) {
            val have = db.findMessageStandBy(userEmail[i][4])
            if (have) {
                Transmission.haveMessage(userEmail[i][0])
            }
            i++
        }

        i = 0
        //anuntam toti prieteni ca suntem online
        repeat(userEmail.size) {
            Transmission.imOnline(userEmail[i][0])
            i++
        }

        Log.d("COROUTINE_A" , "END")
    }

}



class InitActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //aici verificam daca userul este conectat sau nu
        if(!permitActivityAfterLogin) {

            startActivity(Intent("MainActivity")); Log.d("INIT_ACTIVITY" ,"ACTIVITY_MAIN")

        }
        else {

            startActivity(Intent("RegisterActivity")); Log.d("INIT_ACTIVITY" ,"ACTIVITY_REGISTER")

        }

    }

    override fun onStop() {
        super.onStop()

        //ALWAYS DESTROYED ON STOP
        finish()

    }

}



//register class, aici se face inregistrarea userului in db a serverului
class RegisterActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val buttonA : Button = findViewById(R.id.createAccount)
        val buttonB : TextView = findViewById(R.id.goToLogin)
        val inputUsername : EditText = findViewById(R.id.usernameText)
        val inputEmail : EditText = findViewById(R.id.emailText)
        val inputPassword : EditText = findViewById(R.id.passwordText)
        val errorTextOutput : TextView = findViewById(R.id.errorText)
        var serverResponse : String

        buttonA.setOnClickListener {

            if (permitObjInternetAcess) {

                //inchidem actiunea butonului pentru user
                buttonA.isEnabled = false

                //verificam daca datele introduse respecta condiitiile
                if(inputUsername.length() in 4..30 && inputEmail.length() >= 5 && inputPassword.length() >= 8 && inputPassword.length() <= 50 && '@' in inputEmail.text.toString()) {

                    //am folosit thread pentru ca functiile HTTPS dureaza , iar limbajul este asincron , inauntrul thread este normal (linear)
                    thread {

                         //trimitem datele la server si asteptam raspunusul
                         serverResponse = Https().httpsFun4(inputUsername.text.toString(), inputEmail.text.toString(), inputPassword.text.toString())

                         runOnUiThread {

                             when (serverResponse) {

                                 "true" ->  {

                                     if(savedInstanceState == null) {

                                         supportFragmentManager.beginTransaction()
                                             .replace(R.id.fragmentRegister, AccountCreatedActivity(applicationContext))
                                             .commitNow()

                                     }

                                 }
                                 "false" -> {

                                     //redeschidem actiunea butonului pentru user
                                     buttonA.isEnabled = true

                                     //asta se afisaza daca in server accountul exista
                                     errorTextOutput.text = "ERROR CREATING ACCOUNT!"

                                 }
                                 "FAILED" -> {

                                     //redeschidem actiunea butonului pentru user
                                     buttonA.isEnabled = true

                                     //asta se afisaza daca serverul nu raspunde
                                     errorTextOutput.text = "SERVER NOT RESPONDING"

                                 }

                             }

                         }

                    }

                } else {

                    //pentru cazurile cand inputul de la user nu este conform serverului
                    if(inputUsername.length() <= 3) {

                        errorTextOutput.text = "USERNAME TOO SHORT! , SHOULD BE BIGGER THAN 3 CHARACTERS"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if(inputEmail.length() < 5) {

                        errorTextOutput.text = "EMAIL TOO SHORT!"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if(inputPassword.length() < 8) {

                        errorTextOutput.text = "PASSWORD TOO SHORT! , SHOULD BE BIGGER THAN 7 CHARACTERS"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if(inputPassword.length() > 50) {

                        errorTextOutput.text = "PASSWORD TOO LONG! , SHOULD BE LESSER THAN 50 CHARACTERS"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if('@' !in inputEmail.toString()) {

                        errorTextOutput.text = "INCORRECT EMAIL , SHOULD CONTAIN CHARACTER '@'"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if(inputUsername.length() > 30) {

                        errorTextOutput.text = "USERNAME TOO LONG , MUST BE UNDER 30 CHARACTERS"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true
                    }

                }

            } else {

                errorTextOutput.text = "INTERNET CONNECTION LOST"

            }

        }

        buttonB.setOnClickListener {

            if(permitObjInternetAcess) {

                if(savedInstanceState == null) {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentRegister, LoginActivity(applicationContext))
                        .commitNow()

                }

            }

        }

    }

}



//clasa in lucru  ,USER UI
class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) { Log.d("USER_ACTIVITY" ,"CREATED")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        
        val sendMessageButton: ImageButton = findViewById(R.id.sendMessage)
        val loadProfileIcon: ImageView = findViewById(R.id.cardImage) //asta cand vom adauga file support(mai mult info in MasterVar)
        val userSettingsButton: LinearLayout = findViewById(R.id.userSettings)
        val inputText: EditText = findViewById(R.id.messageText)
        val usernameTextView: TextView = findViewById(R.id.usernameDisplayText)
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardsLayout)
        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintUserUi)

        //variabila globala care stocheaza emailul userului cu care comunicam
        globalSpecificUser = sUser

        //adaugam in obiectul Transmission classLinearLayout pentru a putea actualiza interfata
        Transmission.addLayout(cardLinearLayout)

        //permite obiectului sa execute actiuni in MSG_Lisener , dupa ce classLinearLayout a fost adaugat
        permitObjUser = true

        //initializam data de baze , deschidem o instanta a ei
        val db = MasterDb(applicationContext)

        //introducem text in caseta textViewCIA pentru a afisa usernameul prietenului
        usernameTextView.text = "$sUsername"

        sendMessageButton.setOnClickListener { UserActivityChild().sendMessage(inputText , cardLinearLayout , applicationContext , sUser , sUKey , sId) }
        userSettingsButton.setOnClickListener { UserActivityChild().openSettings(constraintLayout , usernameTextView, applicationContext, db , sUser , sId , sUsername) }

        //aici incarcam mesajele respectivului user(prieten) in messageArray
        val messageArray = db.messageLoader(sId) //vom face un mecanism de sinconizre folosindune de scoll(in interfata)

        if(messageArray.size > 0) {

            for (i in messageArray.size - 1 downTo 0) {

                when (messageArray[i][0]) {

                    //mesajele celui care a trimis catre utilizatorul curent
                    // TODO: ADD THE DATE TO MESSAGE
                    "$sUser" -> { CardViews().receiveMessageCard(messageArray[i][1] , messageArray[i][3] , cardLinearLayout , applicationContext) }

                    //mesajele celui care utilizeaza aplicatia curenta
                    "$localUserEmail" -> { CardViews().sendMessageCard(messageArray[i][1] , messageArray[i][3] , cardLinearLayout , applicationContext) }

                }

            }

        }

    }

    fun buttonC(view: View) { finish() }

    override fun onStop() {
        super.onStop()

        //pentru orice eventualitate , inlocuim globalSpecificUser cu null
        globalSpecificUser = null

        //nu mai permite obiectului Transmission sa execute actiuni in MSG_liserner
        permitObjUser = false

    }

    override fun onRestart() { // ASA II DAU REFRESH ACTIVITAIT DACA USERUL A PARASITO SI REINTRA!
        super.onRestart()

        finish()
        startActivity(Intent("InterfaceActivity"))

    }

}



//MainUI
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.gri30)

        //verificam daca userul este conectat pentru prima data , ca sa deschidem transmission!
        userOnFirstConnect()

        //cardLinearLayout si constraintLayout
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardLinearLayoutMain)
        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)

        //dam obiectului constraint layout , pentru a functiona live friend accept
        Transmission.addConstraint(constraintLayout)

        // dam obiectului linearLayoutul
        Transmission.addLayout(cardLinearLayout)

        //deschidem o instanta a date de baze MasterDb
        val db = MasterDb(applicationContext)

        //incarcam toti prieteni din db in mainArray
        val mainArray = db.mainLoader()

        //afisam pe ecran , prin carduri , toti prieteni connectati
        for(i in 0 until mainArray.size step 1) {

            val listA: MutableList<String?> = mutableListOf(mainArray[i][0] , mainArray[i][1] , mainArray[i][2] , mainArray[i][3] , mainArray[i][4])
            CardViews().mainUiCard(listA , cardLinearLayout , applicationContext , constraintLayout)

        }; cardMainBoolean = true

        //da permisiune obiectului Transmsiion sa execute anumite actiuni
        permitObjMain = true

    }

    override fun onStop() {
        super.onStop()

        //ia permisiune obiectului Transmsiion sa execute anumite actiuni
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

        //verificam daca userul este conectat la server sau se executa codul o singura data la conectare
        if(permitActivityAfterLogin && permitObjInternetAcess) {
            Log.d("MAIN PERMIT" , "TRANSMISSION ACTIVATED FOR FIRST TIME")

            Transmission.start() // CONNECTS THE APP TO SERVER FOR FIRST TIME
            permitActivityAfterLogin = false

        }

    }

}

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        TODO("DE CONTINUAT ACTIVITATEA!")

    }

    override fun onRestart() {
        super.onRestart()

        finish()
        startActivity(Intent("SettingsActivity"))

    }

}

//ConnUI
class ConnActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conn_ui)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.gri30)

        //val intent1 = Intent("com.example.ACTION_AC6") //buton de inapoi de inplementat in colt sus!!!
        val buttonA: Button = findViewById(R.id.sendConnRequest)
        val inputConnRequest: EditText = findViewById(R.id.inputConnRequest)

        //linearLayoutul si constraint layoutul pentru toata clasa
        val cardLinearLayout: LinearLayout = findViewById(R.id.cardLinearLayoutConn)
        val constraintLayout: ConstraintLayout = findViewById(R.id.connConstraintLayout)

        //dam constraint layoutul obiectuil, pentru a functiona live friend request
        Transmission.addConstraint(constraintLayout)

        //adaugam in obiect cand intram in activitate , ca sa putem afisa pe interfata
        Transmission.addLayout(cardLinearLayout)

        //deschidem o instanta DB
        val db = MasterDb(applicationContext)

        //stocam in connArray toate CONN requesturile externe
        val connArray = db.connLoader(); Log.d("CREATE_CONN", "LOADER")

        //afisam pe interfata toate conn_requests
        for(i in 0 until connArray.size step 1) {

            val listA : MutableList<String?> = mutableListOf(connArray[i][0] ,connArray[i][1], connArray[i][2], connArray[i][3] , connArray[i][4], connArray[i][5])
            CardViews().connUiCard(listA ,cardLinearLayout , applicationContext , constraintLayout)

        }; cardConnBoolean = true

        //permite obiectului Transmission sa faca anumite executi
        permitObjConn = true

        buttonA.setOnClickListener { //TODO: FA O FUNCTIE

            //extragem textul din textBox
            val userInput = inputConnRequest.text.toString()

            //aici facem request_conn
            Transmission.sendRequest(userInput, Random().genRandomCode(12))

            //stergem textul de pe ecranul userului
            inputConnRequest.text.clear()

        }

    }

    override fun onStop() {
        super.onStop()

        //permite obiectului Transmission sa faca anumite executi
        permitObjConn = false

    }

    override fun onRestart() {
        super.onRestart()

        finish()
        startActivity(Intent("ConnActivity"))

    }

    fun buttonConn(view: View) { finish() }

}











/*
/*
de cate ori avem nevoie de un multithread task folosim corutina !!1
corutineName = CoroutineScope(Dispatchers.Main).launch {


           //aici vine codul executat de corutina


}
*/

activarea ei : corutineName?.start() si cancel() pt a o oprio

/*
repeat(numberOfTimes){ //functie repetativa speciala , nu este while / for , este una aparte , break nu poate exista aici , repeta de n ori cat este specificat ca parametru !!!
....bucata cod
}
 */

 */