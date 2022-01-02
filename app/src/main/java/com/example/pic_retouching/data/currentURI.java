package com.example.pic_retouching.data;

import android.content.Context;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class currentURI {
    // encapsulate different operations towards CURRENT table into a class
    private Database database;

    public currentURI (Context context){
        this.database = new Database(context);
    }

    public void insert(int position, String picUri){
        SQLiteDatabase db = database.getWritableDatabase();

        String insert_sql = "INSERT INTO " + Database.URI +
                " (position,image) VALUES (?,?)";
        Object[] obj = {position, picUri};

        db.execSQL(insert_sql, obj);
        db.close();
    }// insert into CURRENT table without state

    public void insertAll(int position, String picUri, int isEdit){
        SQLiteDatabase db = database.getWritableDatabase();

        String insert_sql = "INSERT INTO " + Database.URI +
                " (position,image,isEdit) VALUES (?,?,?)";
        Object[] obj = {position, picUri, isEdit};

        db.execSQL(insert_sql, obj);
        db.close();
    }// insert into CURRENT table without state


    public void update(int position, String picUri, int isEdit){
        SQLiteDatabase db = database.getWritableDatabase();

        String update_sql = "UPDATE " + Database.URI + " SET image = '" + picUri +
                "', isEdit = " + isEdit + " WHERE position = " + position;

//        try {
//            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
//            field.setAccessible(true);
//            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
//        } catch (Exception e) {
//            e.printStackTrace();
//        }// broaden the cursor window to 100 MB

        db.execSQL(update_sql);
        db.close();
    }

    public void delete(int position){
        SQLiteDatabase db = database.getWritableDatabase();

        String delete_sql = " DELETE FROM " + Database.URI +
                " WHERE position = " + position;

        db.execSQL(delete_sql);
        db.close();
    }// delete one piece of row of CURRENT table according to the  position

    @RequiresApi(api = Build.VERSION_CODES.P)
    public Object[] query(int position) {
        SQLiteDatabase db = database.getReadableDatabase();

        String query_sql = " SELECT * FROM " + Database.URI +
                " WHERE position = " + position;

        Cursor cursor = db.rawQuery(query_sql, null);
        if (cursor.getCount() == 0){
            return null;
        }
        cursor.moveToFirst();
        String uri = cursor.getString(1);
        Integer edit = cursor.getInt(2);
        cursor.close();
        db.close();

        return new Object[]{uri, edit};
    }// query to check if there is this row

    public ArrayList<String> queryEdit(int edit){
        SQLiteDatabase db = database.getReadableDatabase();
        ArrayList<String> paths = new ArrayList<>();
        String query_sql = " SELECT * FROM " + Database.URI +
                " WHERE isEdit = " + edit;

        Cursor cursor = db.rawQuery(query_sql, null);
        while (cursor.moveToNext()){
            paths.add(cursor.getString(1));
        }
        cursor.close();
        db.close();

        return paths;
    }

    public void clearCurrent(){
        SQLiteDatabase db = database.getWritableDatabase();

        String delete_sql = " DELETE FROM " + Database.URI;

        db.execSQL(delete_sql);
        db.close();
    }

}
