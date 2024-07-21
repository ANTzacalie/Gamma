package com.mca.gamma

import java.time.ZoneId

// BrodCast Reciver , o folosim acolo sa stim numarul de connectari/deconectari de la internet/server
var used: Int = 0 // un simplu contor global

// nu am habar daca o folosim cum trebuie , dar e aici
var systemTimeZone: ZoneId? = null
//

///// variabile stocate local in keystore
var serverAccessCode: String? = "" // codul unic de acces la server , daca userul vrea poate fi stocat in memorie cu keystore

var localUserEmail: String? = "" // aici stocam emailul userului , daca userul vrea il stocam in memorie cu keystore

var localUsername: String? = "" //aici stocam usernamul , si daca userul vrea il stocam in memorie cu keystore

var localId: String? = "" //id unic al userului
/////

///USER_COMM_UI
var sUser: String? = null
var sId: String? = null
var sUsername: String? = null
var sUKey: String? = null
var globalSpecificUser: String? = "" //prietenul cu care vorbesti (specific pt actualizarea interfatei)
var globalSpecificDbId = "" //*nu ne trebuie
var globalSpecificKey = "" //*nu ne trebuie
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

//##.obj
var permitObjMain: Boolean = false //MainUI , ca sa putem actualiza interfata

var permitObjConn: Boolean = false //ConnUI , ca sa putem actualiza interfata

var permitObjUser: Boolean = false //UserUI , ca sa putem actualiza interfata

var permitActivityAfterLogin: Boolean = true //Necesar la prima conectare la server(USER) , apoi necesar pentru ConnectivityObject ca sa nu intervina cu MasterActivity

var permitObjInternetAcess: Boolean = false //Connectivity object , asta ne arata daca exista internet
//##.obj

// TODO: MASTER COMMENT
//  -- MESAJELE CU IMAGINI / LINKURI / YOUTUBE LINK VOR FI SEPARATE DE MESAJUL NORMAL , VEDEM CUM FACEM CU ASTA

/*VERSION 2.77 */////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*

/*
    // TODO: DE TESTAT NOILE CARDURI PT MESAJE
*/





//                                                      TODO: DUPA DATA DE 6 IUNIE
// TODO: USERUL VA FI INTRBAT DACA DORESTE SA AIBA SAU NU O PAROLA LA APLICATIE , VOM FOLOSI AndroidStorage  , VA FI IN SETARI OPTIUNEA!
//



//
// TODO: ALTA DATA , UNDEVA IN LUNA IULIE++:
// TODO: DE INCEPUT IMPLEMENTAREA LOGICE PENTRU A SUSTINE FISIERE SI IMAGINI , DE ADAUGAT STRUCTURILE PENTRU RETINEREA IMAGINILOR ACOLO UNDE TREBUIE
// TODO: de implementat link view / youtube view(video loader) , redirect fun + eliminare mesaje,
// TODO: aplicatia nu va suporta transferul de fisiere(pana la versiunea alpha 3.0)
// TODO: DE REZOLVAT CU MasterService.kt si cu iesirea din aplicatie etc...
// TODO: DE IMPLEMENTAT GROUP + GROUP CHAT , TABEL SEPARAT IN BAZA DE DATE , MAI DE GRABA O BAZA DE DATE SEPARATA!
// TODO: NU NEAPARAT* , SA ADAUGI UN HART-BEAT SYSTEM SA STIM DACA SERVERUL ESTE OFFLINE/ONLINE , CA SA NE RECONECTAM LA EL , VEDEM DACA O FACE AUTOMAT!
// TODO: MESAJELE TREBUIE ORDONATE IN FUNCTIE DE DATA SI ORA!




//TODO: CE AM IMPLEMENTAT:
    //SWITCHURILE IN USER_I SETTINGS
    //AM REFACUT PROMPTURILE DE REFUSE/BLOCK
    //AM ADAUGAT SI IMPLEMENTAT MESSAGE ID + TODO: DE TESTAT
    //AM ADAUGAT SYSTEMUL DE IESIRE LA FUNCTIILE ACTIVITY
    //AM ADAUGAT FORGOT PASSWORD , AM ADAUGAT CHANGE USERNAME(LOCAL)
    //AM REFACUT MESSAGE_CARD_SR


//todo: SCHEME DACA UITI:
//CA SA MARIM SPATIUL UNDE SCRIE USERUL MESAJUL , ADAUGAM DINAMIC IN COD LA INPUT TEXT : inputType="textMultiline" si maxHeight = .... ceva
//BUTOANELE IN CONN_MAIN_BUTTON_SELECTOR AU FOST FACUTE ROUND FOLOSINDUNE DE SHAPE! ===>>> urmatoarele carduri cu butoane folosim acceasi schema
//AM FACUT SCHEMA DE IESIT DIN CARD , ATUNCI CAND APESI IN AFARA LUI: IEI CONSTRAINT LAYOUTUL INFLATED SI II FACI LISENER (CA SA DISTRUGI INTERFATA CREATA), DAR TREBUI SI UN CHILD PENTRU ELEMENTE , SI EL ARE NEVOI DE LISENER(DOAR DE FATADA) CA SA NU FIE DISTRUS;




