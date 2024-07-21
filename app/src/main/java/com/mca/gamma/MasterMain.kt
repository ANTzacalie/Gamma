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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/*
MASTER COMMENT:
--> LA SFARSIT < CAND TERMINAM APLICATIA , VOM AVEA TOATE COMMENTURILE IN ENGLEZA
--> PUTEM SCHIMBA LAYOUTUL IN ACTIVITATE cu setContentView de cate ori vrem , acu atunci cand o facem toata logica noului layout trebuie pusa in runOnUiThread{}
    -- PUTEM FACE REGISTER/LOGIN MAI SMOOTH!
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

        Log.d("LOCAL USER:" , "$localUserEmail + $localUsername + $localId + $serverAccessCode")
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

        //main ui
        val intent1 = Intent("MainActivity")
        //register ui
        val intent2 = Intent("RegisterActivity")

        //aici verificam daca userul este conectat sau nu
        if(!permitActivityAfterLogin) {

            startActivity(intent1); Log.d("INIT_ACTIVITY" ,"ACTIVITY_MAIN")

        }
        else {

            startActivity(intent2); Log.d("INIT_ACTIVITY" ,"ACTIVITY_REGISTER")

        }

    }

    override fun onStop() {
        super.onStop()

        //SO THE APP IS NOT GONNA GET HERE BY MISTAKE
        finish()

    }

}

//register class, aici se face inregistrarea userului in db a serverului
class RegisterActivity : AppCompatActivity() {

    private var isExit = false

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

        val intent2 = Intent("LoginActivity")
        val intent3 = Intent("AccountCreatedActivity")

        buttonA.setOnClickListener {

            if (permitObjInternetAcess) {

                //inchidem actiunea butonului pentru user
                buttonA.isEnabled = false

                //verificam daca datele introduse respecta condiitiile
                if(inputUsername.length() > 3 && inputUsername.length() <= 30 && inputEmail.length() >= 5 && inputPassword.length() >= 8 && inputPassword.length() <= 50 && '@' in inputEmail.text.toString()) {

                    //am folosit thread pentru ca functiile HTTPS dureaza , iar limbajul este asincron , inauntrul thread este normal (linear)
                    thread {

                         //trimitem datele la server si asteptam raspunusul
                         serverResponse = Https().httpsFun4(inputUsername.text.toString(), inputEmail.text.toString(), inputPassword.text.toString())

                         runOnUiThread {

                             when (serverResponse) {

                                 "true" -> {

                                     isExit = true
                                     startActivity(intent3)

                                 }
                                 "false" -> {

                                     //redeschidem actiunea butonului pentru user
                                     buttonA.isEnabled = true

                                     //asta se afisaza daca in server accountul exista
                                     errorTextOutput.text = "ACCOUNT ALREADY EXISTS!"

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

                        errorTextOutput.text = "INCORRECT EMAIL , SHOULD CONTAIN CARACTER '@'"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true

                    } else if(inputUsername.length() > 30) {

                        errorTextOutput.text = "USERNAME TOO LONG , MUST BE UNDER 30 CARACTERS"

                        //redeschidem actiunea butonului pentru user
                        buttonA.isEnabled = true
                    }

                }

            } else {

                errorTextOutput.text = "CONNECTION LOST"

            }

        }

        buttonB.setOnClickListener {

            if(permitObjInternetAcess) {

               startActivity(intent2)

            }

        }

    }

    override fun onStop() {
        super.onStop()

        if(isExit) { finish() }

    }

}

//login class , trebuie sa mai modificam interfata + box view , la fel ca register si sa aducem niste improvment in caz de credentiale gresite
class LoginActivity : AppCompatActivity() {

    private var isExit = false

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val intent = Intent("CodeVerificationActivity")
        val intent2 = Intent("RegisterActivity")
        val buttonA : Button = findViewById(R.id.proceedLogin)
        val inputEmail : EditText = findViewById(R.id.emailText)
        val inputPassword : EditText = findViewById(R.id.passwordText)
        val errorTextOutput : TextView = findViewById(R.id.errorText)
        val buttonB: TextView = findViewById(R.id.changePassword)

        buttonA.setOnClickListener {

            if (permitObjInternetAcess) {

                //inchidem actiunea butonului
                buttonA.isEnabled = false

                //am folosit thread pentru ca functiile HTTPS dureaza , iar limbajul este asincron in interiorul activity
                thread {

                    //trimitem datele la server si asteptam raspunusul
                    Https().httpsFun1(inputEmail.text.toString() , inputPassword.text.toString())

                    runOnUiThread {

                        if (localUsername.isNullOrEmpty() && localId.isNullOrEmpty() || localUsername == "FAILED" && localId == "DUCK") {

                            // Enable the button again in case of an error / wrong input, nothing is received
                            errorTextOutput.text = "SERVER ERROR OR INCORECT PASSWORD/EMAIL"
                            buttonA.isEnabled = true

                        } else {

                            //daca httpsFun1 este un success atunci inregistram localUserEmail
                            localUserEmail = inputEmail.text.toString()
                            isExit = true
                            startActivity(intent)

                        }

                    }

                }

            } else {

                //daca nu exista internet
                errorTextOutput.text = "CONNECTION LOST"

            }

        }

        buttonB.setOnClickListener {

            //redirectionam din nou la Signup
            startActivity(Intent("ChangePasswordActivity"))

        }

    }

    override fun onStop() {
        super.onStop()

        if(isExit) { finish() }

    }

}

//CHANGE PASSWORD ACTIVITY
class ChangePasswordActivity : AppCompatActivity() {

    // TODO: EXPERIMENTAL
    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val button: Button = findViewById(R.id.next)
        val emailText: EditText = findViewById(R.id.emailText)
        val newPassword: EditText = findViewById(R.id.passwordText)
        val errorText: TextView = findViewById(R.id.errorText)

        // THIS IS THE FIRST PART OF THE PROCESS , THE USER INPUTS THE EMAIL AND ENTER A NEW PASSWORD.
        button.setOnClickListener {

            email = emailText.text.toString()
            password = newPassword.text.toString()

            if(!email.isNullOrEmpty() && !password.isNullOrEmpty() && '@' in email!!) {

               thread {

                   var response = Https().httpsFun8(email!!)

                   runOnUiThread {

                       if(response) {

                           setContentView(R.layout.activity_code_verification)
                           window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                           window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

                           runOnUiThread {

                               // THIS IS THE SECOUND PARD , THE USER RECIVES A CODE AND ENTER IT HERE AND THEN GETS BACK TO LOGIN!
                               val inputCode: EditText = findViewById(R.id.codeText)
                               val buttonA: Button = findViewById(R.id.sendCode)

                               buttonA.setOnClickListener {

                                   response = false
                                   buttonA.isClickable = false
                                   val code = inputCode.text.toString()

                                   thread {

                                       response = Https().httpsFun6( code , email!! , password!!)

                                       runOnUiThread {

                                           if(response) {

                                               finish()

                                           } else {

                                               errorText.text = "Somthing is wrong , server problem or wrong code"

                                           }

                                       }

                                       buttonA.isClickable = true

                                   }

                               }

                           }

                       } else {

                           errorText.text = "SOMTHING IS WRONG , SERVER ERROR OR INCORECT EMAIL/PASSWORD"

                       }

                   }

               }

            } else if(email!!.length < 11 || password!!.length < 8 || '@' !in email!!) {

                errorText.text = "EMAIL SHOULD BE AT LEAST 11 CARACTERS AND PASSWORD SHOULD BE AT LEAST 8 , EMAIL SHOULD CONTAIN @"

            } else {

                errorText.text = "EMAIL OR PASSWORD CANNOT BE EMPTY!"

            }

        }

    }

}

//interfata account created , daca creeam un account cu succes
class AccountCreatedActivity : AppCompatActivity() {

    private var isExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_registerd)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor =  ContextCompat.getColor(this, R.color.black)

        val intent = Intent("LoginActivity")
        val buttonA : Button = findViewById(R.id.proceedToLogin)

        buttonA.setOnClickListener{

            isExit = true;
            startActivity(intent)

        }

    }

    override fun onStop() {
        super.onStop()

        if(isExit) { finish() }

    }

}

//code verification activity , clasa terminata
class CodeVerificationActivity : AppCompatActivity() {

    private var isExit = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_verification)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val intent = Intent("MainActivity")
        val inputCode: EditText = findViewById(R.id.codeText)
        val buttonA: Button = findViewById(R.id.sendCode)

        buttonA.setOnClickListener {

            //verificam daca exista conexiune la internet
            if(permitObjInternetAcess) {

                //inchidem actiunea buttonului
                buttonA.isClickable = false

                //thread nou din accelas motiv ca cele de mai sus
                thread {

                    //trimitem datele la server si asteptam raspunsul
                    val responseServer = Https().httpsFun5(inputCode.text.toString())

                    runOnUiThread {

                        //verificam raspunsul de la server
                        if(responseServer.isNotEmpty() && responseServer != "false") {

                            //datele intoarse de la server sunt stocate in local storage
                            val keystore = AndroidLocalStorage(applicationContext)
                            keystore.saveESUI(localUserEmail!!, responseServer , localUsername!! , localId!!)

                            //CREEAM TABELUL MAIN , DUPA CONNECTARE(DACA NU EXISTA)
                            MasterDb(applicationContext).tableCreatorMain()

                            //stocam in variabila globala serverAccessCode si permitem activitati sa fie distrusa prin permitD
                            serverAccessCode = responseServer; isExit = true

                            //deschidem activitate MAIN
                            startActivity(intent)

                        } // o sa adaugam un else aici care afisaza pe ecran ca am introdus codul gresit

                    }

                    //deschidem actiunea butonului
                    buttonA.isClickable = true

                }

            }

        }

    }

    override fun onStop() {
        super.onStop()

        //distrugem activitatea
        if(isExit){ finish() }

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
        val loadFileButton: ImageButton = findViewById(R.id.imageVideoDocumentLoader) //asta cand vom adauga file support(mai mult info in MasterVar)
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

        // TODO: ADD THE DATE TO MESSAGE
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

        //classLinearLayout si constraintLayout sunt globale doar aici in classa
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
    fun buttonB(view: View) { startActivity(Intent("BlackListActivity")) }
    fun buttonC(view: View) { startActivity(Intent("LogsActivity")) }
    fun buttonD(view: View) { startActivity(Intent("settingsActivity")) }

    private fun userOnFirstConnect() {

        //verificam daca userul este conectat la server sau se executa codul o singura data la conectare
        if(permitActivityAfterLogin && permitObjInternetAcess) {
            Log.d("MAIN PERMIT" , "TRANSMISSION ACTIVATED FOR FIRST TIME")

            Transmission.start() // conectare la server , o singura data
            permitActivityAfterLogin = false

        }

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