package com.example.swifty_protein.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
//database base per user

class DbHelper (private val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_NAME = "mydatabase.db"
        const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PASSWORD = "password"

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

    override fun onCreate(db: SQLiteDatabase){
        val CREATE_TABLE_USERS = """CREATE TABLE $TABLE_USERS (
           $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
           $COLUMN_NAME TEXT UNIQUE,
           $COLUMN_PASSWORD TEXT)"""
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

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun addUser(name: String, password: String): Long {
        if (name.isBlank() || password.isBlank()) {
            return -1L
        }
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PASSWORD, password)
        }
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }
}