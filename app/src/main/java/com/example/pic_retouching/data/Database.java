package com.example.pic_retouching.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "data.db";
    private static final int DB_VERSION = 1;
    public static final String URI = "uri";
    public static final String DRAFT = "draft";

    private static SQLiteOpenHelper mHelper;

    public Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }


    public static synchronized SQLiteOpenHelper getInstance(Context context){
        if(mHelper == null){
            mHelper = new Database(context, DB_NAME, null, DB_VERSION);
        }
        return mHelper;
    }// only one of the Database objects you created can be executable (synchronized)
    // easy to update the Database


    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the table containing current images
        //String drop_previous = "DROP TABLE " + CURRENT;

        String create_draft = "CREATE TABLE IF NOT EXISTS " + DRAFT + " ( " +
                "position INTEGER UNIQUE NOT NULL," +
                "image VARCHAR(80) NOT NULL," +
                "isEdit INT DEFAULT 1);";

        String create_uri = "CREATE TABLE IF NOT EXISTS " + URI + " ( " +
                "position INT PRIMARY KEY NOT NULL," +
                "image VARCHAR(80) NOT NULL," +
                "isEdit INT DEFAULT 0);";

        //db.execSQL(drop_previous);
        db.execSQL(create_draft);
        db.execSQL(create_uri);
    }
    // init database

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
