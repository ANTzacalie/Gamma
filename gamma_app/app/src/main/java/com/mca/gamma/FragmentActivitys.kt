package com.mca.gamma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.*


// LOGIN
class LoginActivity(context: Context) : Fragment(R.layout.activity_login) {

    private val apiCall = CoroutineScope(Dispatchers.Main)
    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login : Button = view.findViewById(R.id.proceedLogin)
        val inputEmail : EditText = view.findViewById(R.id.emailText)
        val inputPassword : EditText = view.findViewById(R.id.changeUsername)
        val errorTextOutput : TextView = view.findViewById(R.id.errorText)
        val changePassword: TextView = view.findViewById(R.id.changePassword)
        val getBack: CardView = view.findViewById(R.id.getBack)

        login.setOnClickListener {

            login.setBackgroundColor(resources.getColor(R.color.red))

            if (permitObjInternetAcess) {

                //We close the action of the button
                login.isEnabled = false

                //Coroutine call for HTTPS calls
                apiCall.launch {

                    withContext(Dispatchers.IO) {

                        Https().login(inputEmail.text.toString(), inputPassword.text.toString())

                    }

                    if (localUsername.isNullOrEmpty() && localId.isNullOrEmpty() || localUsername == "FAILED" && localId == "DUCK") {

                        // Enable the button again in case of an error / wrong input, nothing is received
                        Toast.makeText(appContext, "SERVER ERROR OR INCORRECT PASSWORD/EMAIL", Toast.LENGTH_LONG).show()

                        login.isEnabled = true
                        login.setBackgroundColor(resources.getColor(R.color.black))

                    } else {

                        //if httpsFun1 is successful then we store inputEmail into global var localUserEmail
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

                //If there is no internet
                Toast.makeText(appContext ,"INTERNET CONNECTION LOST", Toast.LENGTH_SHORT).show()

                login.isClickable = true
                login.setBackgroundColor(resources.getColor(R.color.black))

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



//CHANGE_PASSWORD 
class ChangePasswordActivity(context: Context) : Fragment(R.layout.activity_change_password) {

    private var email: String? = null
    private var password: String? = null
    private val appContext = context
    private val apiCall = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val next: Button = view.findViewById(R.id.next)
        val emailText: EditText = view.findViewById(R.id.emailText)
        val newPassword: EditText = view.findViewById(R.id.newUsername)
        val errorText: TextView = view.findViewById(R.id.errorText)
        val getBack: CardView = view.findViewById(R.id.getBack)

        // THIS IS THE FIRST PART OF THE PROCESS , THE USER INPUTS THE EMAIL AND ENTER A NEW PASSWORD.
        next.setOnClickListener {

            next.isClickable = false
            next.setBackgroundColor(resources.getColor(R.color.red))

            email = emailText.text.toString()
            password = newPassword.text.toString()

            if (email!!.length >= 5 && password!!.length >= 8 && '@' in email!! && password!!.isNotEmpty()) {

                apiCall.launch {

                    var response: Boolean
                    withContext(Dispatchers.IO) {

                        response = Https().changePassword1(email!!)

                    }

                    if (response) {

                        // STARTS PASSWORD CONFIRM FRAGMENT
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentConfirmChange, PasswordConfirmActivity(appContext, email, password))
                            .addToBackStack(null)
                            .commit()

                    } else {

                        Toast.makeText(appContext,"EMAIL NOT FOUND OR SERVER NOT RESPONDING!", Toast.LENGTH_SHORT).show()

                        next.isClickable = true
                        next.setBackgroundColor(resources.getColor(R.color.black))

                    }

                }

            } else {

                Toast.makeText(appContext , "EMAIL SHOULD BE AT LEAST 5 CHARACTERS AND PASSWORD SHOULD BE AT LEAST 8 , EMAIL SHOULD CONTAIN @", Toast.LENGTH_LONG).show()

                next.isClickable = true
                next.setBackgroundColor(resources.getColor(R.color.black))

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
            next.setBackgroundColor(resources.getColor(R.color.red))

            val code = inputCode.text.toString()

            apiCall.launch {

                var response: Boolean

                withContext(Dispatchers.IO) {

                    response = Https().changePassword2(code , email!! , password!!)

                }

                if(response) {

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentCodeVerification, LoginActivity(appContext))
                        .addToBackStack(null)
                        .commit()

                } else {

                    Toast.makeText(appContext, "SOMETHING IS WRONG!", Toast.LENGTH_SHORT).show()

                    next.setBackgroundColor(resources.getColor(R.color.black))
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



//ACCOUNT_CREATED
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

}


//CODE_VERIFICATION
class CodeVerificationActivity(context: Context) : Fragment(R.layout.activity_code_verification) {

    private val apiCall = CoroutineScope(Dispatchers.Main)
    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputCode: EditText = view.findViewById(R.id.codeText)
        val next: Button = view.findViewById(R.id.sendCode)
        val getBack: CardView = view.findViewById(R.id.getBack)

        next.setOnClickListener {

            //We check if there is internet connection
            if(permitObjInternetAcess) {

                //We close the action of Button
                next.isClickable = false
                next.setBackgroundColor(resources.getColor(R.color.red))

                //Coroutine call for HTTPS calls
                apiCall.launch {

                    val responseServer: String

                    withContext(Dispatchers.IO) {

                        responseServer = Https().codeVerify(inputCode.text.toString())

                    }

                    if(responseServer.isNotEmpty() && responseServer != "false") {

                        //KEEP THE SAC ON THE GLOBAL VAR
                        serverAccessCode = responseServer

                        //STORE THE RECEIVED DATA FORM SERVER
                        val key = AndroidLocalStorage(appContext)
                        key.storeConnectionData(localUserEmail, serverAccessCode , localUsername , localId)

                        //NOW HERE WE CREATE THE MAIN TABLE , ONE SINGLE TIME
                        MasterDb(appContext).mainTable()
                        MasterDb(appContext).settingsTable()

                        //STARTS MAIN_ACTIVITY
                        startActivity(Intent("MainActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

                    } else {

                        Toast.makeText(appContext, "WRONG CODE/SERVER ERROR", Toast.LENGTH_SHORT).show()

                        next.setBackgroundColor(resources.getColor(R.color.black))
                        next.isClickable = true

                    }

                }

            } else {

                Toast.makeText(appContext, "INTERNET CONNECTION LOST!", Toast.LENGTH_SHORT).show()

                next.setBackgroundColor(resources.getColor(R.color.black))
                next.isClickable = true

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

// TODO: NOT COMPLETE
class OpenSettingsUser(context: Context) : Fragment(R.layout.settings_users) {

    private val appContext = context

    /*

        FOR ANY OPERATION THAT MAKES AN UI CHANGE TO THIS FRAGMENT ON ANOTHER FRAGMENT , variable backToParentFragment SHOULD BE MADE TRUE!
        // TODO: Da bind la cele scrise mai jos cu noua baza de date si server!

    */

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getBack: ImageView = view.findViewById(R.id.goBack)
        val removeFriend: Button = view.findViewById(R.id.removeFriend)
        val exportChat: Button = view.findViewById(R.id.exportChat)
        val changeUsernameFriend: Button = view.findViewById(R.id.changeUsername)
        val userPicture: ImageView = view.findViewById(R.id.cardImage)
        val usernameOnFragment: TextView = view.findViewById(R.id.usernameDisplayText)

        //val image: ImageView = view.findViewById(R.id.cardImage) //Image we will add the friend's profile image, we see how we add it (where we store the image in the first phase!)
        usernameOnFragment.text = sUsername

        val db = MasterDb(appContext)
        // take from DB the status of each switch below
        val blockState = db.getBlockState(sUser)
        val blockSwitch: MaterialSwitch = view.findViewById(R.id.switchBlock)
        if(blockState == 0) {
            blockSwitch.isChecked = false
        }else {
            blockSwitch.isChecked = true
        }

        val notifyState = db.getNotifyState(sUser)
        val notifySwitch: MaterialSwitch = view.findViewById(R.id.switchNotifications)
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

        removeFriend.setOnLongClickListener {

            TODO("MAKE A NEW FRAGMENT FOR THIS YOU IDIOT")
            // A fragment where the user decides whether to eliminate or not the friend.

            true
        }

        changeUsernameFriend.setOnClickListener {

            if(changeUsernameBoolean) {

                changeUsernameBoolean = false

                parentFragmentManager.beginTransaction()
                    .replace(R.id.changeUsernameFragment, ChangeUsernameUsers(appContext))
                    .addToBackStack(null)
                    .commit()

            }

        }

        exportChat.setOnClickListener {

            // Todo: We will add a feature to read all chat history (only text)
            // Todo: Here will be the first interaction with Android Files Stuff, I have to create a .TXT file and stored in Downloads/Documents (see)

        }

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("UserActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

    }

}


class ChangeUsernameUsers(context: Context) : Fragment(R.layout.username_changer) {

    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getBack: CardView = view.findViewById(R.id.goBack)
        val changeUsername: Button = view.findViewById(R.id.changeUsername)
        val newUsernameText: EditText = view.findViewById(R.id.newUsername)

        val db = MasterDb(appContext)

        getBack.setOnClickListener {

            changeUsernameBoolean = true

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("UserActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

        changeUsername.setOnClickListener {

            val newUsername: String = newUsernameText.text.toString()

            if (newUsername.isNotEmpty() && ' ' !in newUsername) {

                changeUsernameBoolean = true
                backToParentFragment = true

                sUsername = newUsername
                // Update the username here , in db for the specific friend.

                startActivity(Intent("UserActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            } else if(newUsername.isEmpty()){

                Toast.makeText(appContext , "USERNAME CANNOT BE EMPTY!", Toast.LENGTH_SHORT).show()

            } else if(' ' in newUsername) {

                Toast.makeText(appContext , "USERNAME CANNOT CONTAIN WHITESPACES!", Toast.LENGTH_SHORT).show()

            }

        }

    }

}

class ChangeUsernameLocalUser(context: Context) : Fragment(R.layout.username_changer) {

    private val apiCall = CoroutineScope(Dispatchers.Main)
    private val appContext = context

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getBack: CardView = view.findViewById(R.id.goBack)
        val changeUsername: Button = view.findViewById(R.id.changeUsername)
        val newUsernameText: EditText = view.findViewById(R.id.newUsername)

        getBack.setOnClickListener {

            if (parentFragmentManager.backStackEntryCount > 0) {

                parentFragmentManager.popBackStack()

            } else {

                startActivity(Intent("SettingsActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

            }

        }

        changeUsername.setOnClickListener {

            changeUsername.isClickable = false
            changeUsername.setBackgroundColor(resources.getColor(R.color.red))

            val newUsername: String = newUsernameText.text.toString()

            if (newUsername.isNotEmpty() && ' ' !in newUsername && permitObjInternetAcess) {

                apiCall.launch {

                    var responseServer: Boolean

                    withContext(Dispatchers.IO) {

                        responseServer = Https().changeUsername(newUsername)

                    }

                    if(responseServer) {

                        localUsername = newUsername
                        AndroidLocalStorage(appContext).storeConnectionData(localUserEmail , serverAccessCode , newUsername , localId)

                        if (parentFragmentManager.backStackEntryCount > 0) {

                            parentFragmentManager.popBackStack()

                        } else {

                            startActivity(Intent("SettingsActivity").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))

                        }

                    } else {

                        Toast.makeText(appContext, "NO SERVER RESPONSE , TRY AGAIN LATER!", Toast.LENGTH_LONG).show()

                        changeUsername.isClickable = true
                        changeUsername.setBackgroundColor(resources.getColor(R.color.black))

                    }

                }

            } else if(newUsername.isEmpty()){

                Toast.makeText(appContext , "USERNAME CANNOT BE EMPTY!", Toast.LENGTH_SHORT).show()

                changeUsername.isClickable = true
                changeUsername.setBackgroundColor(resources.getColor(R.color.black))

            } else if(' ' in newUsername) {

                Toast.makeText(appContext , "USERNAME CANNOT CONTAIN WHITESPACES!", Toast.LENGTH_SHORT).show()

                changeUsername.isClickable = true
                changeUsername.setBackgroundColor(resources.getColor(R.color.black))

            } else if(!permitObjInternetAcess) {

                Toast.makeText(appContext , "NO INTERNET CONNECTION!", Toast.LENGTH_SHORT).show()

                changeUsername.isClickable = true
                changeUsername.setBackgroundColor(resources.getColor(R.color.black))

            }

        }

    }

}
