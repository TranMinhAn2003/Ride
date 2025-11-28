package com.rideincab.user.common.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqLiteDb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "UserDetailsDB"

        private const val TABLE_TRIPS = "UserTripsInfo"
        private const val KEY_DOCUMENT_ID = "User_trip_doc_id"
        private const val KEY_DOCUMENT = "User_trip_doc"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_LOCAL_STORAGE_TABLE = ("CREATE TABLE " + TABLE_TRIPS + "(" + KEY_DOCUMENT_ID + " TEXT PRIMARY KEY,"
                + KEY_DOCUMENT + " BLOB" + ")")

        db?.execSQL(CREATE_LOCAL_STORAGE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS)
        onCreate(db)
    }

    fun getDocument(documentID: String): Cursor {
        val db = writableDatabase
        return db.rawQuery("SELECT " + KEY_DOCUMENT + " FROM " + TABLE_TRIPS + " WHERE " + KEY_DOCUMENT_ID + " = ?", arrayOf(documentID))
    }

    fun insertWithUpdate(documentID: String?, document: String?) {
        val db = writableDatabase
        val content = ContentValues()
        content.put(KEY_DOCUMENT_ID, documentID)
        content.put(KEY_DOCUMENT, document)
        writableDatabase.insertWithOnConflict(TABLE_TRIPS, null, content, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

}