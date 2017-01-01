package com.wolfie.odile.model.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Helper extends SQLiteOpenHelper {

    private static final String TAG = "Helper";

    public Helper(Context context) {
        super(context, MetaData.DATABASE_NAME, null, MetaData.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + MetaData.PHRASE_TABLE + " ("
                    + MetaData.PHRASE_ID + " INTEGER PRIMARY KEY,"
                    + MetaData.PHRASE_GROUP + " TEXT NOT NULL,"
                    + MetaData.PHRASE_RUSSIAN + " TEXT NOT NULL,"
                    + MetaData.PHRASE_ENGLISH + " TEXT NOT NULL,"
                    + MetaData.PHRASE_TRANSLIT + " TEXT NOT NULL,"
                    + MetaData.PHRASE_PATH + " TEXT);");
        } catch (SQLException e) {
            Log.d(TAG, "SQLite exception: " + e.getLocalizedMessage());
        }
    }

    public void dropTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE " + MetaData.PHRASE_TABLE);
        } catch (SQLException e) {
            Log.d(TAG, "SQLite exception: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion == 1 && newVersion == 2) {
            // Execute sql to migrate from oldVersion to newVersion
        }
        onCreate(db);
    }
}
