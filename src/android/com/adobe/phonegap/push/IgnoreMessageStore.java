package com.adobe.phonegap.push;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IgnoreMessageStore extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "IgnoreMessageDB";
    private static final String MESSAGE_ID = "messageid";
    private static final String TABLE_IGNORE = "messageid";
    private static final String KEY_ID = "id";

    public IgnoreMessageStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_ID_TABLE = "CREATE TABLE "+TABLE_IGNORE+" ( " +
                KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                MESSAGE_ID + " TEXT )";
 
        // create books table
        db.execSQL(CREATE_ID_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_IGNORE);
 
        // create fresh books table
        this.onCreate(db);
    }

    public void addMessage(String messageId){
                //for logging
        Log.d("addMessage", messageId);
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(MESSAGE_ID, messageId); // get title 
 
        // 3. insert
        db.insert(TABLE_IGNORE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
    }

    public void deleteMessage(String messageId) {
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_IGNORE  , //table name
                KEY_ID+" = ?",  // selections
                new String[] { messageId }); //selections args
 
        // 3. close
        db.close();
 
        //log
        Log.d("deleteMessage", messageId);
    }

    public boolean exists(String messageId) {

        final String[] COLUMNS = {KEY_ID};

        final SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_IGNORE, // a. table
                        COLUMNS, // b. column names
                        MESSAGE_ID + " = ?", // c. selections
                        new String[]{String.valueOf(messageId)}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        return cursor != null;
    }
}