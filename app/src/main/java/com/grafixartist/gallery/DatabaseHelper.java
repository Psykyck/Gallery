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
    private static final String ACCOUNTS_TABLE = "Accounts";
    private static final String PHOTOS_TABLE = "Photos";
    private static final String LOCATION_TABLE = "Location";
    private static final String PIN_TABLE = "Pin";
    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;
    private static final String INSERT = "insert into " + ACCOUNTS_TABLE + "(email, password) values (?, ?)" ;

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
        this.db.delete(ACCOUNTS_TABLE, null, null);
    }

    public boolean checkPinLock(String filePath) {
        // Check if pin lock
        List<String> list = new ArrayList<>();
        boolean result = false;
        Cursor cursor = this.db.query(PHOTOS_TABLE, new String[] { "path, pinlock" }, "path = '" + filePath + "'", null, null, null, "path desc");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
                list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (!list.isEmpty()) {

        }
        return result;
    }

    public boolean checkUsernameExists(String email) {
        List<String> list = new ArrayList<>();
        boolean result = false;
        Cursor cursor = this.db.query(ACCOUNTS_TABLE, new String[] { "email" }, "email = '" + email + "'", null, null, null, "email desc");
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
        Cursor cursor = this.db.query(ACCOUNTS_TABLE, new String[] { "email", "password" }, "email = '"+ email +"' AND password= '"+ password+"'", null, null, null, "email desc");
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
            db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE + "(id INTEGER PRIMARY KEY, email TEXT, password TEXT)");
            db.execSQL("CREATE TABLE " + PHOTOS_TABLE + "(id INTEGER PRIMARY KEY, filename TEXT, date TEXT, size TEXT, path TEXT, pinlock INTEGER, locationlock INTEGER)");
            db.execSQL("CREATE TABLE " + LOCATION_TABLE + "(id INTEGER, coordinates TEXT, radius TEXT, FOREIGN KEY(id) REFERENCES " + PHOTOS_TABLE + "(id))");
            db.execSQL("CREATE TABLE " + PIN_TABLE + "(id INTEGER, passcode TEXT, FOREIGN KEY(id) REFERENCES " + PHOTOS_TABLE + "(id))");
            db.execSQL("ALTER TABLE " + LOCATION_TABLE + " ADD COLUMN replacementfk INTEGER REFERENCES " + PHOTOS_TABLE + "(id)");
            db.execSQL("ALTER TABLE " + PIN_TABLE + " ADD COLUMN replacementfk INTEGER REFERENCES " + PHOTOS_TABLE + "(id)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w("Example", "Upgrading database; this will drop and recreate the tables.");
            db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE);
            onCreate(db);
        }
    }
}
