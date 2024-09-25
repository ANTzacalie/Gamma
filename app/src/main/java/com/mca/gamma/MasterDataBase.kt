package com.mca.gamma

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import kotlin.Exception


//////// ***** Status ***** ////// map
// ---> stand_by-> for those actions if the respective internet /user is not connected to the server
// ---> Received+ / Received ++-> The message was written and sent / seen, received
// ---> Connected-> the user has become a friend (only in the friends table !!!)
/////// ***** Status ***** ///////


// keystore class
class AndroidLocalStorage(private val context: Context) {

    private val masterKeyAlias = MasterKey.Builder(context).setKeyGenParameterSpec(MasterKeys.AES256_GCM_SPEC).build()

    fun saveLD(email: String?, serverAccessCode: String?, username : String?, id : String?) {
        Log.d("LOCAL STORAGE","SAVE LOGIN_DATA USED")

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit()
            .putString("email", email)
            .putString("serverAccessCode", serverAccessCode)
            .putString("username" , username)
            .putString("id",id)
            .apply()

    }

    fun saveHP(fullServerText: String?) {
        Log.d("LOCAL STORAGE","SAVE HOST_AND_PORT USED")

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "serverAddress",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit()
            .putString("hp" , fullServerText)
            .apply()

    }

    fun getHP(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "serverAddress",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("hp", null)

    }

    fun getEmail(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("email", null)

    }

    fun getSecureCode(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("serverAccessCode", null)

    }
    fun getUsername(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("username", null)

    }

    fun getId(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString("id", null)

    }

}


class MasterDb(context: Context) : SQLiteOpenHelper(context,"user0backup.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?){}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int){}

    fun tableCreatorMain() {

        val db = writableDatabase

        val createTableSql = """
            CREATE TABLE IF NOT EXISTS main (
                id INTEGER PRIMARY KEY,
                USER_EMAIL TEXT NOT NULL,
                CONN_KEY TEXT NOT NULL,
                STATUS TEXT NOT NULL,
                USER_NAME TEXT NOT NULL,
                USER_ID TEXT NOT NULL,
                TODO TEXT NOT NULL,
                TYPE TEXT NOT NULL,
                NOTIFY_STATE INT,
                BLOCK_STATE INT
            );
        """

        db.execSQL(createTableSql)
        db.close()
    }

    fun tableCreatorAppSettings() {

        val db = writableDatabase

        val createTableSql = """
            CREATE TABLE IF NOT EXISTS settings (
                id INTEGER PRIMARY KEY,
                ALL_NOTIFY INT,
                BLOCK_STATE INT
            );
        """

        db.execSQL(createTableSql)
        db.close()

    }

    //executed each time a new connction with another user is made
    fun tableCreatorUser(userId: String) {

        val db = writableDatabase

        Log.d("TABLE CREATOR USERS" , "TABLE CREATED FOR USER_ID: $userId")
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS $userId (
                USER TEXT NOT NULL,
                MESSAGE TEXT NOT NULL,
                STATUS TEXT NOT NULL,
                TS TEXT NOT NULL,
                ID TEXT NOT NULL
            );
        """

        db.execSQL(createTableSql)
        db.close()

    }

    fun updateStatusTodoTypeMain(userEmail : String?, status: String?, todo: String? , type: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("STATUS", status)
                put("TODO", todo)
                put("TYPE" , type)

            }

            val selection = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE STATUS AND TODO IN MAIN TABLE" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateUsernameIdMain(userEmail : String?, senderUsername : String?, senderId: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("USER_NAME", senderUsername)
                put("USER_ID", senderId)

            }

            val selection = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE USERNAME AND ID IN MAIN TABLE" , "ERROR: " + e.message); db.close()

        }

    }

    //from here we write data to the main table witch is gonna contain all the info of the friend
    fun insertIntoMain(userEmail: String?, connCode: String?, status : String?, username: String?, userId: String?, todo: String?, type: String?, notifyState: Int, blockState: Int) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("USER_EMAIL" , userEmail)
                put("CONN_KEY" , connCode)
                put("STATUS" , status)
                put("USER_NAME" , username)
                put("USER_ID" , userId)
                put("TODO" , todo)
                put("TYPE" , type)
                put("NOTIFY_STATE" , notifyState)
                put("BLOCK_STATE" , blockState)

            }

            db.insert("main", null, values)
            db.close()

        } catch (e:Exception) {

            Log.d("INSERT INTO MAIN","ERROR: " + e.message); db.close()

        }

    }

    //from here we write to user table
    fun insertIntoUser(userId: String?, userEmail : String?, userMessage : String?, status : String?, sendTime: String? , messageId: String) {

        val db = writableDatabase

        try{

            val values = ContentValues().apply {

                put("USER", userEmail)
                put("MESSAGE", userMessage)
                put("STATUS", status)
                put("TS", sendTime)
                put("ID", messageId)

            }

            if (userId != null) {
                db.insert(userId, null, values)
            }
            db.close()

        }catch (e:Exception) {

            Log.d("INSERT INTO USERS","ERROR: TABLE WITH ID $userId and CODE: " + e.message); db.close()

        }

    }

    fun updateMessageStatus(userId: String?, userEmail: String?, message: String?, messageId: String?, newStatus: String?) {

        Log.d("MSG STATUS" , "TABLE ID: $userId WITH MESSAGE: $message TO UPDATE STATUS WITH: $newStatus")

        val db = writableDatabase

        try{

            val update = ContentValues().apply {

                put("STATUS", newStatus)

            }

            val where = "USER = ? AND MESSAGE = ? AND ID = ?"
            val whereArgs = arrayOf(userEmail, message, messageId)

            if (userId != null) {
                db.update(userId, update, where, whereArgs)
            }
            db.close()

        }catch (e:Exception) {

            Log.d("MESSAGE STATUS","ERROR: TABLE WITH ID $userId and CODE: " + e.message); db.close()

        }

    }

    fun updateNotifyState(userEmail: String?, newState: Int) {

        val db = writableDatabase

        try{

            val values = ContentValues().apply {

                put("NOTIFY_STATE", newState)

            }

            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, where, whereArgs); Log.d("UPDATE NOTIFY_STATE IN MAIN TABLE","STATE UPDATE TO: $newState")
            db.close()

        }catch(e:Exception) {

            Log.d("UPDATE NOTIFY_STATE IN MAIN TABLE","ERROR: " + e.message); db.close()

        }

    }

    fun updateBlockState(userEmail : String?, newState: Int) {

        val db = writableDatabase

        try{

            val values = ContentValues().apply {

                put("BLOCK_STATE", newState)

            }

            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, where, whereArgs); Log.d("UPDATE BLOCK_STATE IN MAIN TABLE","STATE UPDATE TO: $newState")
            db.close()

        }catch(e:Exception) {

            Log.d("UPDATE BLOCK_STATE IN MAIN TABLE","ERROR: " + e.message); db.close()

        }

    }

    fun updateConnCodeMain(userEmail: String? , connCode : String?) {

        val db = writableDatabase

        try{

            val values = ContentValues().apply {

                put("CONN_KEY", connCode)

            }

            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, where, whereArgs)
            db.close()

        }catch(e:Exception) {

            Log.d("UPDATE CONN_KEY MAIN TABLE","ERROR: " + e.message); db.close()

        }

    }

    fun deleteFromMain(userEmail : String?) {

        val db = writableDatabase

        try {

            val whereArgs = arrayOf(userEmail)
            db.delete("main", "USER_EMAIL = ?", whereArgs)

            db.close()

        }catch(e:Exception){

            Log.d("REMOVE FROM MAIN","ERROR: " + e.message); db.close()

        }

    }

    fun findUserInMain(userEmail : String?): Boolean {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_EMAIL")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    if (!cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL")).isNullOrEmpty()) {

                        return true

                    }

                }

            }

            db.close()

        }catch (e:Exception) {

            Log.d("TABLE MAIN FINDER","ERROR: " + e.message); db.close()
            return false

        }

        return false

    }

    @SuppressLint("Range")
    fun mainLoader(): MutableList<MutableList<String?>> {

        val db = readableDatabase

        var returnArray: MutableList<MutableList<String?>> = MutableList(0) { MutableList(5) {null} }
        val columns = arrayOf("USER_EMAIL" , "CONN_KEY" , "STATUS" , "USER_NAME" , "USER_ID")

        try {

            val where = "STATUS = ?"
            val whereArgs = arrayOf("CONNECTED")
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                cursor.moveToFirst()
                val count: Int = cursor.count
                returnArray = MutableList(count) { MutableList(5) { null } }

                for (rows in 0 until  count step 1) {

                    returnArray[rows][0] = cursor.getString(cursor.getColumnIndex(columns[0]))
                    returnArray[rows][1] = cursor.getString(cursor.getColumnIndex(columns[1]))
                    returnArray[rows][2] = cursor.getString(cursor.getColumnIndex(columns[2]))
                    returnArray[rows][3] = cursor.getString(cursor.getColumnIndex(columns[3]))
                    returnArray[rows][4] = cursor.getString(cursor.getColumnIndex(columns[4]))

                    cursor.moveToNext()

                }

            }
            cursor.close(); db.close()

            return returnArray

        } catch (e:Exception) {

            Log.d("mainUserLoader" , "ERROR WITH EXCEPTION LOG: " + e.message)

            return returnArray

        }

    }

    //message loader is partially finished , needs more work to load messages in order by date and hour
    @SuppressLint("Range")
    fun messageLoader(id: String?) : MutableList<MutableList<String?>> {

        /*

            // TODO: MESAJELE TREBUIE ORDONATE IN FUNCTIE DE DATA SI ORA!

        */

        val db = readableDatabase; Log.d("MESSAGE_LOADER" , "TRIGGERED")
        var messageArray: MutableList<MutableList<String?>> = MutableList(0) { MutableList(3) { null } }

        try {

            val columns = arrayOf("USER , MESSAGE , STATUS , TS, ID")
            if(id != null) {


                val cursor: Cursor = db.query(id , columns, null , null , null , null , null)

                cursor.use {

                    cursor.moveToFirst() // WE MOVE TO FIRST LINE
                    val count: Int = cursor.count
                    messageArray = MutableList(count) { MutableList(5) {null} }

                    cursor.moveToLast() // WE MOVE TO THE LAST LINE
                    for (rows in 0 until count step 1) {

                        messageArray[rows][0] = cursor.getString(cursor.getColumnIndex("USER"))
                        messageArray[rows][1] = cursor.getString(cursor.getColumnIndex("MESSAGE"))
                        messageArray[rows][2] = cursor.getString(cursor.getColumnIndex("STATUS"))
                        messageArray[rows][3] = cursor.getString(cursor.getColumnIndex("TS"))
                        messageArray[rows][4] = cursor.getString(cursor.getColumnIndex("ID"))

                        cursor.moveToPrevious() //ca sa ne ducem la linia din urma

                    }

                }
                cursor.close();

            }
            db.close()

            return messageArray

        }catch (e:Exception) {

            Log.d("MESSAGE LOADER","ERROR WITH EXCEPTION: " + e.message); db.close()
            return messageArray

        }

    }

    @SuppressLint("Range")
    fun messageLoaderStandBy(id: String?) : MutableList<String?> {

        val db = readableDatabase
        var messageList: MutableList<String?> = MutableList(0) { null }

        try {

            val columns = arrayOf("MESSAGE")
            val where = "STATUS = ?"
            val whereArgs = arrayOf("STAND_BY")

            if(id != null) {

                val cursor: Cursor = db.query(id, columns, where, whereArgs, null, null, null)

                cursor.use {

                    cursor.moveToFirst() //ne ducem la prima linie
                    val count = cursor.count

                    messageList = MutableList(count) { null }

                    for (pos in 0 until count step 1) {

                        messageList[pos] = cursor.getString(cursor.getColumnIndex("MESSAGE"))

                        cursor.moveToNext() //ca sa ne ducem la linia urmatoare

                    }

                }

                cursor.close();
            }

            db.close()

            return messageList

        } catch (e:Exception) {

            Log.d("M.LOADER STAND BY","ERROR WITH EXCEPTION: " + e.message); db.close()
            return messageList

        }

    }

    @SuppressLint("Range")
    fun messageLoaderIdStb(id: String?) : MutableList<String?> {

        val db = readableDatabase
        var messageList: MutableList<String?> = MutableList(0) { null }

        try {

            val columns = arrayOf("ID")
            val where = "STATUS = ?"
            val whereArgs = arrayOf("STAND_BY")

            if(id != null) {

                val cursor: Cursor = db.query(id, columns, where, whereArgs, null, null, null)

                cursor.use {

                    cursor.moveToFirst() //ne ducem la prima linie
                    val count = cursor.count

                    messageList = MutableList(count) { null }

                    for (pos in 0 until count step 1) {

                        messageList[pos] = cursor.getString(cursor.getColumnIndex("ID"))

                        cursor.moveToNext() //ca sa ne ducem la linia urmatoare

                    }

                }

                cursor.close();
            }

            db.close()

            return messageList

        } catch (e:Exception) {

            Log.d("ID.LOADER STAND BY","ERROR WITH EXCEPTION: " + e.message); db.close()
            return messageList

        }

    }

    @SuppressLint("Range")
    fun getMessagesTimeStamp(id: String?): MutableList<String?> {

        val db = readableDatabase
        var timeStamps: MutableList<String?> = MutableList(0) { null }

        try {

            val columns = arrayOf("TS")
            val where = "STATUS = ?"
            val whereArgs = arrayOf("STAND_BY")

            if(id != null) {

                val cursor: Cursor = db.query(id, columns, where, whereArgs, null, null, null)

                cursor.use {

                    cursor.moveToFirst() //ne ducem la prima linie
                    val count = cursor.count

                    timeStamps = MutableList(count) { null }

                    for (pos in 0 until count step 1) {

                        timeStamps[pos] = cursor.getString(cursor.getColumnIndex("TS"))

                        cursor.moveToNext() //ca sa ne ducem la linia urmatoare

                    }

                }

                cursor.close();
            }
            db.close()

            return timeStamps

        }catch (e:Exception) {

            Log.d("M.LOADER STAND BY","ERROR WITH EXCEPTION: " + e.message); db.close()
            return timeStamps

        }

    }

    @SuppressLint("Range")
    fun connLoader(): MutableList<MutableList<String?>> {

        val db = readableDatabase
        var connArray: MutableList<MutableList<String?>> = MutableList(0) { MutableList(6) {null} }

        try {

            val columns = arrayOf("USER_EMAIL", "CONN_KEY", "USER_ID", "USER_NAME", "STATUS", "TYPE")
            val where = "TYPE = ?"
            val whereArgs = arrayOf("REQUEST_EXTERNAL")
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                cursor.moveToFirst()
                val count = cursor.count
                connArray = MutableList(count) { MutableList(6) {null} }

                for (rows in 0 until count step 1) {

                    connArray[rows][0] = cursor.getString(cursor.getColumnIndex(columns[0]))
                    connArray[rows][1] = cursor.getString(cursor.getColumnIndex(columns[1]))
                    connArray[rows][2] = cursor.getString(cursor.getColumnIndex(columns[2]))
                    connArray[rows][3] = cursor.getString(cursor.getColumnIndex(columns[3]))
                    connArray[rows][4] = cursor.getString(cursor.getColumnIndex(columns[4]))
                    connArray[rows][5] = cursor.getString(cursor.getColumnIndex(columns[5]))

                    cursor.moveToNext()

                }

            }

            cursor.close(); db.close()

            return  connArray

        } catch (e: Exception) {

            Log.d("CONN_LOADER" , "ERROR WITH EXCEPTION: " + e.message)
            return  connArray

        }

    }

    fun validateUser(userEmail: String?, code: String?): Boolean {

        val db = readableDatabase

        try {

            val columns = arrayOf("CONN_KEY")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor?.use {

                while (cursor.moveToNext()) {

                    val key = cursor.getString(cursor.getColumnIndexOrThrow("CONN_KEY"));
                    Log.d("USER VALIDATED:", "$key == $code")

                    if (key == code) {

                        db.close()
                        return true

                    }

                }

            }

            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d("USER CANNOT BE VALIDATED" ,"ERROR: " + e.message); db.close()
            return false

        }

        return false

    }

    fun getTodoFromMain(userEmail: String?) : String {

        val db = readableDatabase

        try {

            val columns = arrayOf("TODO")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    return cursor.getString(cursor.getColumnIndexOrThrow("TODO"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d("GET TODO FROM MAIN" ,"ERROR WITH EXCEPTION: " + e.message); db.close()
            return "ERROR"

        }

        return "ERROR"

    }

    fun getBlockState(userEmail: String?) : Int {

        val db = readableDatabase

        try {

            val columns = arrayOf("BLOCK_STATE")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    Log.d("GET BLOCK_STATE FROM MAIN" ,"STATE RETRIEVED");
                    return cursor.getInt(cursor.getColumnIndexOrThrow("BLOCK_STATE"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d("GET BLOCK_STATE FROM MAIN" ,"ERROR WITH EXCEPTION: " + e.message); db.close()
            return 0

        }

        return 0

    }

    fun getNotifyState(userEmail: String?) : Int {

        val db = readableDatabase

        try {

            val columns = arrayOf("NOTIFY_STATE")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    Log.d("GET NOTIFY_STATE FROM MAIN" ,"STATE RETRIEVED");
                    return cursor.getInt(cursor.getColumnIndexOrThrow("NOTIFY_STATE"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d("GET NOTIFY_STATE FROM MAIN" ,"ERROR WITH EXCEPTION: " + e.message); db.close()
            return 0

        }

        return 0

    }

    fun getStatusMain(userEmail : String?) : String {

        val db = readableDatabase

        try {

            val columns = arrayOf("STATUS")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    return cursor.getString(cursor.getColumnIndexOrThrow("STATUS"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d("GET STATUS FROM MAIN" ,"ERROR WITH EXCEPTION: " + e.message); db.close()
            return "INVALID"

        }

        return "INVALID"

    }

    fun getUsernameMain(userEmail : String?): String {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_NAME")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)


            cursor.use {

                while (cursor.moveToNext()) {

                    return cursor.getString(cursor.getColumnIndexOrThrow("USER_NAME"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception) {

            Log.d("GET USERNAME FROM MAIN" ,"ERROR: " + e.message); db.close()

            return "NO_USERNAME"

        }

        return "NO_USERNAME"

    }

    fun getIdFromMain(userEmail : String?) : String {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_ID")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    return cursor.getString(cursor.getColumnIndexOrThrow("USER_ID"))

                }

            }
            cursor.close(); db.close()

        } catch (e:Exception){

            Log.d( "GET USER ID FROM MAIN" , "ERROR: " + e.message) ; db.close()
            return "NO_ID"

        }

        return "NO_ID"

    }

    fun getConnCodeFromMain(userEmail: String?) : String {

        val db = readableDatabase

        try {

            val columns = arrayOf("CONN_KEY")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs , null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    return cursor.getString(cursor.getColumnIndexOrThrow("CONN_KEY"))

                }

            }
            cursor.close(); db.close()

        }catch (e:Exception){

            Log.d("GET CONN CODE FORM MAIN","ERROR: " + e.message) ; db.close()
            return "NO_CONN_KEY"

        }

        return "NO_CONN_KEY"
    }

    fun findStandByMain() : MutableList<String?> {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_EMAIL")
            val where = "STATUS = ?"
            val whereArgs = arrayOf("STAND_BY")
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            val backList: MutableList<String?> = mutableListOf()
            cursor.use {

                while (cursor.moveToNext()) {

                    backList.add(cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL")))

                }

            }
            cursor.close(); db.close()

            return backList

        }catch (e:Exception) {

            Log.d("FIND STAND BY MAIN","ERROR: " + e.message); db.close()
            return MutableList(0) { null }

        }

    }

    fun getIdListFromMain() : MutableList<String?> {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_ID")
            val cursor: Cursor = db.query("main", columns, "STATUS = ?", arrayOf("CONNECTED"), null, null, null)

            val backList: MutableList<String?> = mutableListOf()
            cursor.use {

                Log.d("GET MAIN LIST OF ID", "WORKING!")
                while (cursor.moveToNext()) {

                    backList.add(cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")))

                }

            }
            cursor.close(); db.close()

            return backList

        }catch (e:Exception){

            Log.d("GET MAIN LIST OF ID" , "ERROR: " + e.message); db.close()
            return MutableList(0) { null }

        }

    }

    fun findMessageStandBy(userId: String?) : Boolean {

        val db = readableDatabase

        try {

            val columns = arrayOf("MESSAGE")
            val where = "STATUS = ?"
            val whereArgs = arrayOf("STAND_BY")

            if(userId != null) {

                val cursor: Cursor = db.query(userId, columns, where, whereArgs, null, null, null)

                cursor.use {

                    while (cursor.moveToNext()) {

                        return true

                    }

                }
                cursor.close();

            }
            db.close()

            return false

        }catch (e:Exception) {

            Log.d("MESSAGE: FIND STAND BY","ERROR: " + e.message); db.close()
            return false

        }

    }

    fun getEmailUsingId(userId: String?): String {

        val db = readableDatabase

        try {

            val columns = arrayOf("USER_EMAIL")
            val where = "USER_ID = ?"
            val whereArgs = arrayOf(userId)
            val cursor : Cursor = db.query("main" ,columns, where , whereArgs , null , null , null)

            cursor.use{

                while(cursor.moveToNext()){

                    return cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL"))

                }

            }

            cursor.close(); db.close()

        }
        catch (e:Exception) {

            db.close(); Log.d("GET USER FROM MAIN WITH ID","ERROR WITH MESSAGE:" + e.message)

        }

        return "ERROR"

    }

    fun getTypeFromMain(userEmail: String?): String {

        val db = readableDatabase

        try {

            val columns = arrayOf("TYPE")
            val where = "USER_EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor : Cursor = db.query("main" ,columns, where , whereArgs , null , null , null)

            cursor.use{

                while(cursor.moveToNext()){

                    return cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))

                }

            }

            cursor.close(); db.close()

        }
        catch (e:Exception) {

            Log.d("GET TYPE FROM MAIN WITH ID","ERROR WITH MESSAGE:" + e.message); db.close()
            return  "ERROR"

        }

        return "ERROR"

    }

}