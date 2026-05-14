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


// VERSION 3.0

// TODO: REFA TOATE BUTOANELE DE GO_BACK , SEND ETC..  , FOLOSIM GOOGLE ICONS!
// ALL INTERNAL CODE WILL BE REDONE , AS WELL AS WELL AS FOR SERVER CODE!
// CHANGE OF METHOD , ALL SYNC WILL BE DONE ON SERVER , THIS MEANS THE SERVER WILL HAVE ALL THE CHAT_DATA + FIRENDS;
