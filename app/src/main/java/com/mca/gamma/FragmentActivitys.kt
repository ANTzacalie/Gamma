package com.mca.gamma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly


//login class , trebuie sa mai modificam interfata + box view , la fel ca register si sa aducem niste improvment in caz de credentiale gresite
class LoginActivity(context: Context) : Fragment(R.layout.activity_login) {

    private val apiCall = CoroutineScope(Dispatchers.Main)
    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login : Button = view.findViewById(R.id.proceedLogin)
        val inputEmail : EditText = view.findViewById(R.id.emailText)
        val inputPassword : EditText = view.findViewById(R.id.passwordText)
        val errorTextOutput : TextView = view.findViewById(R.id.errorText)
        val changePassword: TextView = view.findViewById(R.id.changePassword)
        val getBack: CardView = view.findViewById(R.id.getBack)

        login.setOnClickListener {

            if (permitObjInternetAcess) {

                //inchidem actiunea butonului
                login.isEnabled = false

                //am folosit thread pentru ca functiile HTTPS dureaza , iar limbajul este asincron in interiorul activity
                apiCall.launch {

                    //trimitem datele la server si asteptam raspunusul
                    withContext(Dispatchers.IO) {

                        Https().httpsFun1(inputEmail.text.toString(), inputPassword.text.toString())

                    }

                    if (localUsername.isNullOrEmpty() && localId.isNullOrEmpty() || localUsername == "FAILED" && localId == "DUCK") {

                        // Enable the button again in case of an error / wrong input, nothing is received
                        errorTextOutput.text = "SERVER ERROR OR INCORRECT PASSWORD/EMAIL"
                        login.isEnabled = true

                    } else {

                        //daca httpsFun1 este un success atunci inregistram localUserEmail
                        localUserEmail = inputEmail.text.toString()

                        if(savedInstanceState == null) {

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentLogin, CodeVerificationActivity(appContext))
                                .addToBackStack(null)
                                .commit()

                        }

                    }

                }

            } else {

                //daca nu exista internet
                errorTextOutput.text = "INTERNET CONNECTION LOST"

            }

        }

        // START CHANGE PASSWORD FRAGMENT
        changePassword.setOnClickListener {

            if(savedInstanceState == null) {

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentLogin, ChangePasswordActivity(appContext))
                    .addToBackStack(null)
                    .commit()

            }

        }

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("RegisterActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

    }

}



//CHANGE PASSWORD ACTIVITY
class ChangePasswordActivity(context: Context) : Fragment(R.layout.activity_change_password) {

    private var email: String? = null
    private var password: String? = null
    private val appContext = context
    private val apiCall = CoroutineScope(Dispatchers.Main)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val next: Button = view.findViewById(R.id.next)
        val emailText: EditText = view.findViewById(R.id.emailText)
        val newPassword: EditText = view.findViewById(R.id.passwordText)
        val errorText: TextView = view.findViewById(R.id.errorText)
        val getBack: CardView = view.findViewById(R.id.getBack)

        // THIS IS THE FIRST PART OF THE PROCESS , THE USER INPUTS THE EMAIL AND ENTER A NEW PASSWORD.
        next.setOnClickListener {

            email = emailText.text.toString()
            password = newPassword.text.toString()

            try {

                if (email!!.length >= 5 && password!!.length >= 8 && '@' in email!! && password!!.isNotEmpty()) {

                    apiCall.launch {

                        var response: Boolean
                        withContext(Dispatchers.IO) {

                            response = Https().httpsFun8(email!!)

                        }

                        if (response) {

                            // STARTS PASSWORD CONFIRM FRAGMENT
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentConfirmChange, PasswordConfirmActivity(appContext, email, password))
                                .addToBackStack(null)
                                .commit()

                        } else {

                            errorText.text = "EMAIL INCORRECT/NOT FOUND!"

                        }

                    }

                } else {

                    errorText.text = "EMAIL SHOULD BE AT LEAST 5 CHARACTERS AND PASSWORD SHOULD BE AT LEAST 8 , EMAIL SHOULD CONTAIN @"

                }

            } catch (e: Exception) {

                Log.e("CHANGE PASSWORD ACTIVITY" , "INPUT ERROR: " + e.message)

            }

        }

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("RegisterActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

    }

}



class PasswordConfirmActivity(context: Context , emailText: String? , passwordText: String?) : Fragment(R.layout.activity_code_verification) {

    private var email: String? = emailText
    private var password: String? = passwordText
    private var appContext = context
    private var apiCall = CoroutineScope(Dispatchers.Main)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputCode: EditText = view.findViewById(R.id.codeText)
        val next: Button = view.findViewById(R.id.sendCode)
        val errorText: TextView = view.findViewById(R.id.codeVerErrorText)
        val getBack: CardView = view.findViewById(R.id.getBack)

        next.setOnClickListener {

            next.isClickable = false
            val code = inputCode.text.toString()

            apiCall.launch {

                var response: Boolean

                withContext(Dispatchers.IO) {

                    response = Https().httpsFun6(code , email!! , password!!)

                }

                if(response) {

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentCodeVerification, LoginActivity(appContext))
                        .addToBackStack(null)
                        .commit()

                } else {

                    errorText.text = "SOMETHING IS WRONG"
                    next.isClickable = true

                }

            }

        }

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("RegisterActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

    }

}



//interfata account created , daca creeam un account cu succes
class AccountCreatedActivity(context: Context) : Fragment(R.layout.activity_account_registerd) {

    private val appContext = context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonA : Button = view.findViewById(R.id.proceedToLogin)
        buttonA.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentAccountRegistered, LoginActivity(appContext))
                .addToBackStack(null)
                .commit()

        }

    }

    @TestOnly
    override fun onDetach() {
        super.onDetach()

        Log.d("TEST" , "JUST ACA TEST")

    }

}


//code verification activity , clasa terminata
class CodeVerificationActivity(context: Context) : Fragment(R.layout.activity_code_verification) {

    private val apiCall = CoroutineScope(Dispatchers.Main)
    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputCode: EditText = view.findViewById(R.id.codeText)
        val errorText: TextView = view.findViewById(R.id.codeVerErrorText)
        val send: Button = view.findViewById(R.id.sendCode)
        val getBack: CardView = view.findViewById(R.id.getBack)


        send.setOnClickListener {

            //verificam daca exista conexiune la internet
            if(permitObjInternetAcess) {

                //inchidem actiunea buttonului
                send.isClickable = false

                //thread nou din accelas motiv ca cele de mai sus
                apiCall.launch {

                    val responseServer: String

                    //trimitem datele la server si asteptam raspunsul
                    withContext(Dispatchers.IO) {

                        responseServer = Https().httpsFun5(inputCode.text.toString())

                    }

                    // CHECKS IF SERVER RESPONSE IS APPROPRIATE
                    if(responseServer.isNotEmpty() && responseServer != "false") {

                        //KEEP THE SAC ON THE GLOBAL VAR
                        serverAccessCode = responseServer

                        //STORE THE RECEIVED DATA FORM SERVER
                        val keystore = AndroidLocalStorage(appContext)
                        keystore.storeLoginData(localUserEmail, serverAccessCode , localUsername , localId)

                        //CREEAM TABELUL MAIN , DUPA CONNECTARE(DACA NU EXISTA)
                        MasterDb(appContext).tableCreatorMain()

                        //STARTS MAIN_ACTIVITY
                        startActivity(Intent("MainActivity"))

                    } else {

                        errorText.text = "WRONG CODE/SERVER ERROR"
                        send.isClickable = true

                    }

                }

            } else {

                errorText.text = "INTERNET CONNECTION LOST!"
                send.isClickable = true

            }

        }

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("RegisterActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

    }

}

