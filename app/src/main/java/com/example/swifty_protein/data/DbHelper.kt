package com.example.swifty_protein.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.mindrot.jbcrypt.BCrypt
//database base per user

class DbHelper (private val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_NAME = "mydatabase.db"
        const val DATABASE_VERSION = 3

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PASSWORD = "password_hash"

        const val COLUMN_IS_LOGGED = "is_logged"

        const val TABLE_LIGANDS = "ligands"
        const val COLUMN_PROTEIN_ID = "id"
        const val COLUMN_PROTEIN_NAME = "name"
        const val COLUMN_PROTEIN_FORMULA = "formula"
        const val COLUMN_PROTEIN_WEIGHT = "weight"
        const val COLUMN_SMILES = "smiles"
        const val COLUMN_INCHI = "inchi"
        const val COLUMN_INCHIKEY = "inchikey"
        const val COLUMN_RAW_CIF_DATA = "raw_cif_data"

        const val TABLE_FAVORITES = "user_favorites"
        const val COLUMN_FAV_USER_ID = "user_id"
        const val COLUMN_FAV_LIGAND_ID = "protein_id"

    }

    data class AuthResult(
        val success: Boolean,
        val userId: Long? = null,
        val message: String? = null
    )

    override fun onCreate(db: SQLiteDatabase){
        val CREATE_TABLE_USERS = """CREATE TABLE $TABLE_USERS (
           $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
           $COLUMN_NAME TEXT UNIQUE,
           $COLUMN_PASSWORD TEXT,
           $COLUMN_IS_LOGGED INTEGER DEFAULT 0)"""
        val CREATE_TABLE_PROTEINS = """CREATE TABLE $TABLE_LIGANDS (
            $COLUMN_PROTEIN_ID TEXT PRIMARY KEY,
            $COLUMN_PROTEIN_NAME VARCHAR(255),
            $COLUMN_PROTEIN_FORMULA VARCHAR(100),
            $COLUMN_PROTEIN_WEIGHT FLOAT,
            $COLUMN_SMILES TEXT,
            $COLUMN_INCHI TEXT,
            $COLUMN_INCHIKEY TEXT,
            $COLUMN_RAW_CIF_DATA TEXT)"""
        val CREATE_TABLE_FAVORITES = """CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_FAV_USER_ID INTEGER,
            $COLUMN_FAV_LIGAND_ID TEXT,
            PRIMARY KEY ($COLUMN_FAV_USER_ID, $COLUMN_FAV_LIGAND_ID),
            FOREIGN KEY ($COLUMN_FAV_USER_ID) REFERENCES $TABLE_USERS ($COLUMN_ID),
            FOREIGN KEY ($COLUMN_FAV_LIGAND_ID) REFERENCES $TABLE_LIGANDS ($COLUMN_PROTEIN_ID)
            )"""

        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_PROTEINS)
        db.execSQL(CREATE_TABLE_FAVORITES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LIGANDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(userSuppliedPassword: String, storedHash: String): Boolean {
        return BCrypt.checkpw(userSuppliedPassword, storedHash)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun getUser(name: String, password: String): Long {
        val username = name.trim()
        if (username.isBlank() || password.isBlank()) {
            return -1L //TODO aggiungere messaggio di errore nel caso in cui campi vuoti
        }

        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_PASSWORD),
            "$COLUMN_NAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        return try {
            if (!cursor.moveToFirst()) {
                -1L
            } else {
                val userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                if (verifyPassword(password, storedHash)) userId else -1L
            }
        } finally {
            cursor.close()
        }
    }

    fun addUser(name: String, password: String): AuthResult {
        val username = name.trim()
        if (username.isBlank() || password.isBlank()) {
            return AuthResult(success = false, message = "Username and Password required")
        }

        val db = this.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NAME, username)
                put(COLUMN_PASSWORD, hashPassword(password))
            }
            val id = db.insertOrThrow(TABLE_USERS, null, values)
            AuthResult(success = true, userId = id)
        } catch (_: SQLiteConstraintException) {
            AuthResult(success = false, message = "Username not valid")
        } catch (_: Exception) {
            AuthResult(success = false, message = "Error in account creation")
        } finally {
            db.close()
        }
    }
}