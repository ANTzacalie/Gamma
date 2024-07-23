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




















