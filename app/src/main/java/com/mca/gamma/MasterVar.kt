package com.mca.gamma

import java.time.ZoneId


// SERVER ADDRESS
var serverAddress: String? = null
var holdServerAddress: Boolean = false

// Broadcast Receiver, we use it there to know the number of connections/disconnections from the Internet
var used: Int = 0 // a simple global meter

// I have no idea if we use it properly, but it's here
var systemTimeZone: ZoneId? = null
//

///// variables stored locally in Keystore
var serverAccessCode: String? = "" // the unique access code to the server, if the user wants can be stored in memory with Keystore

var localUserEmail: String? = "" // here we store the user's email, if the user wants to stock in memory with Keystore

var localUsername: String? = "" // here we stock the usernam, and if the user wants to stock in memory with Keystore

var localId: String? = "" // unique user of the user
/////

///USER_COMM_UI
var sUser: String? = null
var sId: String? = null
var sUsername: String? = null
var sUKey: String? = null
var globalSpecificUser: String? = "" // the friend you are talking to (specific for the interface update)
var globalSpecificDbId = "" //*We don't need
var globalSpecificKey = "" //*We don't need
var backToParentFragment: Boolean = false
///

//###.DB
//--->CONN UI
var cardConnBoolean: Boolean = true
///////////////////////////////////////////////

//---->MAIN UI
var cardMainBoolean: Boolean = true
//

//--->>PART OF SETTINGS AND I_SETTINGS
var changeUsernameBoolean: Boolean = true
//

//##. Tranmission obj
var permitObjMain: Boolean = false // Mainde, so we can update the interface

var permitObjConn: Boolean = false // understand, so we can update the interface

var permitObjUser: Boolean = false // users, so we can update the interface

var permitActivityAfterLogin: Boolean = true // required at the first connection to the server (user), then required for ConnectivityBject so as not to intervene with Masteractivity

var permitObjInternetAcess: Boolean = false // Connectivity Object, this shows us if there is internet
//##. Transmission obj


// *Version 2.89 gamma *///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*

/*
    // TODO: AM NOTAT CU BULINA ROSIE CE AVEM DE MODIFICAT! , TREBUIE SA ADAUGAM FILE SUPPORT ACOLO(IMAGE,VIDEO,YOUTUBE si OTHER)
    TODO: SERVER DATA TRANSFER TEST IS FINISHED , ANDROID MEDIA FILES / OTHER FILES TEST FINISHED AND OBJECT LOGIC IS CORRECT FOR BOTH SERVICE AND APPLICATION ACTIVITY!
        --- IMPLEMENT ANDROID STORAGE FUNCTIONS
        --- IMPLEMENT SERVER FILE_TRANSFER MODIFICATIONS
        --- IMPLEMENT VIDEO/IMAGE/TEXT VIEWR AND FILE SEND / ADD / DELETE
        --- ALL FILES WILL BE NAMED WITH A RANDOM GENERATED NAME!
        --- STORAGE OF FILES WILL BE IN APP PRIVATE STORAGE!
        --- DATABASE WILL BE STORED IN THE SAME PLACE(NEED TESTING ETC..)

        --- IMPLEMENTATION STARTED , AM ADAUGAT TOTA LOGICA DIN APP TEST , DE FACUT LEGATURA COMPLET
 */

// Todo: Integrate the application settings and the rest of the Main interface (Black List and Groups (will have a separate table in DB for settings only))
// Todo Master:
    // - implements View fragment for:
        // - for main_activity (Father Activity) => Black_list, Groups
        // - we start when we finish the rest of the activities!

// TODO: CHECK IF SERVER IS ONLINE!
// TODO: IMPLEMNET SETTINGS TABLE IN DB
// TODO: FIX MESSAGE_DATE FOR DIFFERENT TIME ZONES
    // AND ALSO MAKE THE CHAT_CARD BE SIMETRICAL TO THE OBJECTS INSIDE

// TODO: TRANSLATE TRANSMISSION AND DATABSE COMMENTS


// --->>> TODO: THINK WHAT CAN BE ADDED TO THE INDIVIDUAL SETTINGS INTERFACE , TO NOT LET THAT MUCH EMPTY SPACE THERE
            // -- MEDIA STUFF MAYBE , A VIEWER FOR MEDIA FILES(PICTURES AND VIDEOS ONLY) , TODO: IT WILL BE ADDED



// Todo: Another time, somewhere in August AND OCTOBER****:
// Todo: to start the logical implementation to support files and images, to add the structures for retention of images where you need
// Todo: to implement Link View / YouTube View (Video Loader), Redirect Fun + Elimination Messages,
// Todo: The application will not support the transfer of files (until the Alpha 3.0 version)
// Todo: to be solved with Masterservice.kt and with the exit from the application etc ...
// Todo: Implemented Group + Group Chat, separate table in the database, more than a separated database!
// Todo: Not necessarily*, add a Hart-Beat System to know if the server is offline/online, to reconnect to it, we see if it does it automatically!
// Todo: The messages must be ordered according to the date and time!
// Todo: Add message delete , only for local user!
// TODO: ADD NOTIFICATIONS TO THE APP !
        // -- BE ABLE TO RESPOND TO MESSAGES IN NOTIFICATIONS !
// - messages with pictures / links / youtube link will be separated from the normal message, we see how we do it


// Todo , what I have implemented:
// Users_settings as fragment to user_ui
// Restored the prompts of refuse/block
// Added and implemented Message ID + Todo: Testing
// Added Forgot Password
// Restored message_card_sr
// Restored Register/Login/Change_PASSWORD
// Remade the login / register / code_ver / password_change / register_finis activitys into fragments , for more smooth transition
// Setup , there the user inputs the hostname and port
// Remade some stuff in setup activitys/fragments
// Made user_settings a fragment of UserActivity
// Modified chnage_username fun to match the new user_settings fragment
// ADDED CHNAGE USERNAME AS FRAGMENT TO SETTINGS_USERS
// MADE THE BUTTONS TURN RED AFTER PRESS WHILE PROCESSING
// ADED BACK BUTTON TO REGISTER ACTIVITY TO BE ABLE TO GO BACK TO INIT


// Todo , what I have partially implemented
// IMPLEMENTED LOCAL USER SETTINGS , NOT FINISHED THO // TODO: DE TESTAT
