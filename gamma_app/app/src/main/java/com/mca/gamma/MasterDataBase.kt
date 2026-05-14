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

// keystore class
class AndroidLocalStorage(private val context: Context) {

    private val masterKeyAlias = MasterKey.Builder(context).setKeyGenParameterSpec(MasterKeys.AES256_GCM_SPEC).build()

    fun storeAppSettings(lockStat: Int ,profUri: String? ,allNotify: Int ,friendRequests: Int) {
        Log.d("LOCAL STORAGE","SAVE APP_SETTINGS USED")

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appSettings",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit()
            .putInt("LOCK_STATUS", lockStat)
            .putString("PROFILE_URI" , profUri)
            .putInt("ALL_NOTIFICATION", allNotify)
            .putInt("FRIEND_REQUESTS", friendRequests)
            .apply()

    }

    fun storeConnectionData(email: String?, serverAccessCode: String?, username: String?, id: String?) {
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
            .putString("username" , username)
            .putString("id",id)
            .apply()

    }

    fun storeServerAddress(fullServerText: String?) {
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

    fun getServerAddress(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "serverAddress",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("hp", null)

    }

    fun getLocalEmail(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("email", null)

    }

    fun getLocalUsername(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("username", null)

    }

    fun getLocalId(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString("id", null)

    }

    fun getLockStatus(): Int {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appSettings",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getInt("", 0)

    }

    fun getProfileUri(): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appSettings",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("PROFILE_URI", null)

    }

    fun getNotifyState(): Int {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appSettings",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getInt("ALL_NOTIFICATION", 0)

    }

    fun getRequestStatus(): Int {

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "appSettings",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt("FRIEND_REQUESTS", 0)

    }

}


class MasterDb(context: Context) : SQLiteOpenHelper(context,"user0backup.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun mainTable() {

        val db = writableDatabase

        val createTableSql = """
            CREATE TABLE IF NOT EXISTS main (
                id INTEGER PRIMARY KEY,
                EMAIL TEXT NOT NULL,
                USERNAME TEXT NOT NULL,
                ID TEXT NOT NULL,
                TRANSFER_KEY TEXT NOT NULL,
                BLOCK INT,
                NOTIFICATION INT,
                STATUS TEXT NOT NULL,
                PROFILE_URI TEXT NOT NULL
            );
        """

        db.execSQL(createTableSql)
        db.close()
    }

    //executed each time a new connection with another user is made
    fun createTableForUser(userId: String) {

        val db = writableDatabase

        Log.d("TABLE USERS" , "TABLE CREATED FOR USER_ID: $userId")
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS $userId (
                USER_EMAIL TEXT NOT NULL,
                USER_MESSAGE TEXT NOT NULL,
                USER_FILE_URI TEXT NOT NULL,
                FILE_TYPE TEXT NOT NULL,
                STATUS INT,
                MESSAGE_ID TEXT NOT NULL,
                MESSAGE_TIMESTAMP TEXT NOT NULL
            );
        """

        db.execSQL(createTableSql)
        db.close()

    }

    fun insertMain(email: String? , username: String?, id: String?, transferKey: String? , block: Int , notification: Int , status: String? , profileUri: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("EMAIL" , email)
                put("USERNAME" , username)
                put("ID" , id)
                put("TRANSFER_KEY" , transferKey)
                put("BLOCK" , block)
                put("NOTIFICATION" , notification)
                put("STATUS" , status)
                put("PROFILE_URI" , profileUri)

            }

            db.insert("main", null, values)
            db.close()

        } catch (e:Exception) {

            Log.d(" INSERT TABLE MAIN ","ERROR: " + e.message); db.close()

        }

    }

    fun insertUser(userId: String , email: String? , message: String? , fileUri: String? , status: Int , fileType: String? , messageId: String, messageTimeStamp: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("USER_EMAIL" , email)
                put("USER_MESSAGE" , message)
                put("USER_FILE_URI" , fileUri)
                put("STATUS" , status)
                put("FILE_TYPE" , fileType)
                put("MESSAGE_ID" , messageId)
                put("MESSAGE_TIMESTAMP" , messageTimeStamp)

            }

            db.insert(userId, null, values)
            db.close()

        } catch (e:Exception) {

            Log.d(" INSERT TABLE USER ","ERROR: " + e.message); db.close()

        }

    }

    fun updateUsernameMain(userEmail : String?, username: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("USERNAME", username)


            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE USERNAME MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateKeyMain(userEmail : String?, transferKey: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("TRANSFER_KEY", transferKey)

            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE KEY MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateBlockMain(userEmail : String?, block: Int) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("BLOCK", block)


            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE BLOCK MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateNotificationMain(userEmail : String?, notification: Int) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("NOTIFICATION", notification)

            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE NOTIFY MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateStatusMain(userEmail : String?, status: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("STATUS", status)

            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE STATUS MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateProfileUriMain(userEmail : String?, profileUri: String?) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("PROFILE_URI", profileUri)


            }

            val selection = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)

            db.update("main", values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE PROFILE URI MAIN" , "ERROR: " + e.message); db.close()

        }

    }

    fun updateStatusUserMessage(userId : String, messageId: String , status: Int) {

        val db = writableDatabase

        try {

            val values = ContentValues().apply {

                put("STATUS", status)

            }

            val selection = "MESSAGE_ID = ?"
            val whereArgs = arrayOf(messageId)

            db.update(userId, values, selection, whereArgs)
            db.close()

        } catch(e:Exception) {

            Log.d("UPDATE MESSAGE STATUS USER" , "ERROR: " + e.message); db.close()

        }

    }

    fun checkTransferKeyMain(userEmail : String? , transferKey: String?): Boolean {

        val db = readableDatabase

        try {

            val columns = arrayOf("TRANSFER_KEY")
            val where = "TRANSFER_KEY = ? AND EMAIL = ?"
            val whereArgs = arrayOf(transferKey , userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    if (!cursor.getString(cursor.getColumnIndexOrThrow("")).isNullOrEmpty()) {

                        return true

                    }

                }

            }

            db.close()

        }catch (e:Exception) {

            Log.d("  ","ERROR: " + e.message); db.close()
            return false

        }

        return false

    }

    fun findEmailMain(userEmail : String?): Boolean {

        val db = readableDatabase

        try {

            val columns = arrayOf("EMAIL")
            val where = "EMAIL = ?"
            val whereArgs = arrayOf(userEmail)
            val cursor: Cursor = db.query("main", columns, where, whereArgs, null, null, null)

            cursor.use {

                while (cursor.moveToNext()) {

                    if (!cursor.getString(cursor.getColumnIndexOrThrow("")).isNullOrEmpty()) {

                        return true

                    }

                }

            }

            db.close()

        }catch (e:Exception) {

            Log.d("  ","ERROR: " + e.message); db.close()
            return false

        }

        return false

    }

    @SuppressLint("Range")
    fun loadMain(): MutableList<MutableList<Any?>> {

        val db = readableDatabase

        var returnArray: MutableList<MutableList<Any?>> = MutableList(0) { MutableList(0) {null} }
        val columns = arrayOf("EMAIL , USERNAME , ID , TRANSFER_KEY , BLOCK , NOTIFICATION, STATUS , PROFILE_URI")

        try {

            val cursor: Cursor = db.query("main", columns, null, null, null, null, null)

            cursor.use {

                cursor.moveToFirst()
                val count: Int = cursor.count
                returnArray = MutableList(count) { MutableList(8) { null } }

                for (rows in 0 until  count step 1) {

                    returnArray[rows][0] = cursor.getString(cursor.getColumnIndex(columns[0]))
                    returnArray[rows][1] = cursor.getString(cursor.getColumnIndex(columns[1]))
                    returnArray[rows][2] = cursor.getString(cursor.getColumnIndex(columns[2]))
                    returnArray[rows][3] = cursor.getString(cursor.getColumnIndex(columns[3]))
                    returnArray[rows][4] = cursor.getString(cursor.getColumnIndex(columns[4]))
                    returnArray[rows][5] = cursor.getString(cursor.getColumnIndex(columns[5]))
                    returnArray[rows][6] = cursor.getString(cursor.getColumnIndex(columns[6]))
                    returnArray[rows][7] = cursor.getString(cursor.getColumnIndex(columns[7]))


                    cursor.moveToNext()

                }

            }
            cursor.close(); db.close()

            return returnArray

        } catch (e:Exception) {

            Log.d(" LOAD_MAIN_TABLE " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return returnArray

        }

    }

    @SuppressLint("Range")
    fun loadUser(userId: String): MutableList<MutableList<String?>> {

        val db = readableDatabase

        var returnArray: MutableList<MutableList<String?>> = MutableList(0) { MutableList(0) {null} }
        val columns = arrayOf("USER_EMAIL, USER_MESSAGE, USER_FILE_URI, FILE_TYPE, MESSAGE_ID, MESSAGE_TIMESTAMP")

        try {

            val cursor: Cursor = db.query(userId, columns, null, null, null, null, null)

            cursor.use {

                cursor.moveToFirst()
                val count: Int = cursor.count
                returnArray = MutableList(count) { MutableList(6) { null } }

                for (rows in 0 until  count step 1) {

                    returnArray[rows][0] = cursor.getString(cursor.getColumnIndex(columns[0]))
                    returnArray[rows][1] = cursor.getString(cursor.getColumnIndex(columns[1]))
                    returnArray[rows][2] = cursor.getString(cursor.getColumnIndex(columns[2]))
                    returnArray[rows][3] = cursor.getString(cursor.getColumnIndex(columns[3]))
                    returnArray[rows][4] = cursor.getString(cursor.getColumnIndex(columns[4]))
                    returnArray[rows][5] = cursor.getString(cursor.getColumnIndex(columns[5]))

                    cursor.moveToNext()

                }

            }
            cursor.close(); db.close()

            return returnArray

        } catch (e:Exception) {

            Log.d(" LOAD_USER_CHAT " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return returnArray

        }

    }

    @SuppressLint("Range")
    fun getProfileUriMain(userEmail: String): String? {

        val db = readableDatabase

        val columns = arrayOf("PROFILE_URI")

        try {

            val cursor: Cursor = db.query("main", columns, null, null, null, null, null)

            cursor.use {

                while (it.moveToNext()){

                    return cursor.getString(cursor.getColumnIndex("PROFILE_URI"))

                }

            }
            cursor.close(); db.close()

            return null

        } catch (e:Exception) {

            Log.d(" GET PROFILE_URI FROM MAIN " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return null

        }

    }

    @SuppressLint("Range")
    fun getNotificationMain(userEmail: String): Any? {

        val db = readableDatabase

        val columns = arrayOf("NOTIFICATION")

        try {

            val cursor: Cursor = db.query("main", columns, null, null, null, null, null)

            cursor.use {

                while (it.moveToNext()){

                    return cursor.getInt(cursor.getColumnIndex("NOTIFICATION"))

                }

            }
            cursor.close(); db.close()

            return null

        } catch (e:Exception) {

            Log.d(" GET NOTIFICATION FROM MAIN " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return null

        }

    }

    @SuppressLint("Range")
    fun getBlockMain(userEmail: String): Any? {

        val db = readableDatabase

        val columns = arrayOf("BLOCK")

        try {

            val cursor: Cursor = db.query("main", columns, null, null, null, null, null)

            cursor.use {

                while (it.moveToNext()){

                    return cursor.getInt(cursor.getColumnIndex("BLOCK"))

                }

            }
            cursor.close(); db.close()

            return null

        } catch (e:Exception) {

            Log.d(" GET BLOCK FROM MAIN " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return null

        }

    }

    @SuppressLint("Range")
    fun getStatusMain(userEmail: String): String? {

        val db = readableDatabase

        val columns = arrayOf("STATUS")

        try {

            val cursor: Cursor = db.query("main", columns, null, null, null, null, null)

            cursor.use {

                while (it.moveToNext()){

                     return cursor.getString(cursor.getColumnIndex("STATUS"))

                }

            }
            cursor.close(); db.close()

            return null

        } catch (e:Exception) {

            Log.d(" GET STATUS FROM MAIN " , "ERROR WITH EXCEPTION LOG: " + e.message)

            return null

        }

    }

}