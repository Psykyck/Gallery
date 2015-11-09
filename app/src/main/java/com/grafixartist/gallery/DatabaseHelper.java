package com.grafixartist.gallery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DATABASE_NAME = "Gallery.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Accounts";
    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;
    private static final String INSERT = "insert into " + TABLE_NAME + "(name, password) values (?, ?)" ;

    public DatabaseHelper(Context context) {
        this.context = context;
        GalleryOpenHelper openHelper = new GalleryOpenHelper(this.context);
        this.db = openHelper.getWritableDatabase();
        this.insertStmt = this.db.compileStatement(INSERT);
    }

    public long insert(String email, String password) {
        this.insertStmt.bindString(1, email);
        this.insertStmt.bindString(2, password);
        return this.insertStmt.executeInsert();
    }

    public void deleteAll() {
        this.db.delete(TABLE_NAME, null, null);
    }

    public boolean checkUsernameExists(String email) {
        List<String> list = new ArrayList<>();
        boolean result = false;
        Cursor cursor = this.db.query(TABLE_NAME, new String[] { "email" }, "email = '" + email + "'", null, null, null, "email desc");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (!list.isEmpty()) {
            result = true;
        }
        return result;
    }

    public List<String> selectAll(String email, String password) {
        List<String> list = new ArrayList<>();
        Cursor cursor = this.db.query(TABLE_NAME, new String[] { "email", "password" }, "email = '"+ email +"' AND password= '"+ password+"'", null, null, null, "email desc");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
                list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    private static class GalleryOpenHelper extends SQLiteOpenHelper {

        GalleryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, email TEXT, password TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w("Example", "Upgrading database; this will drop and recreate the tables.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}