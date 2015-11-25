package com.grafixartist.gallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    private SQLiteStatement insertPhotoStmt;
    private SQLiteStatement insertPinStmt;
    private static final String INSERT_PHOTO = "INSERT OR IGNORE INTO " + PHOTOS_TABLE + "(path, filename, date, size) values (?, ?, ?, ?)" ;
    private static final String INSERT = "INSERT INTO " + ACCOUNTS_TABLE + "(email, password) values (?, ?)" ;
    private static final String INSERT_PIN = "INSERT OR IGNORE INTO " + PIN_TABLE + "(passcode, originalFK, replacementFK) values (?, ?, ?)" ;
    private static final String FIND_ID = "SELECT id FROM " + PHOTOS_TABLE + " pt WHERE pt.path=?";
    private static final String FIND_PIN_REPLACEMENT = "SELECT * FROM " + PIN_TABLE + " p WHERE p.originalFK=?";
    private static final String GET_PHOTO_DETAILS = "SELECT path, filename, size, date FROM " + PHOTOS_TABLE + " p WHERE p.ID=?";
    private static final String ENABLE_LOCK_PIN = "UPDATE " + PHOTOS_TABLE + " SET pinLock=1 WHERE path=?";
    private static final String ENABLE_LOCATION_PIN = "UPDATE " + PHOTOS_TABLE + " SET locationLock=1 WHERE path=?";
    private static final String SET_REPLACEMENT_PHOTO = "UPDATE " + PIN_TABLE + " SET replacementFK=?, passcode=? WHERE originalFK=?";


    public DatabaseHelper(Context context) {
        this.context = context;
        GalleryOpenHelper openHelper = new GalleryOpenHelper(this.context);
        this.db = openHelper.getWritableDatabase();
        this.insertStmt = this.db.compileStatement(INSERT);
        this.insertPhotoStmt = this.db.compileStatement(INSERT_PHOTO);
        this.insertPinStmt = this.db.compileStatement(INSERT_PIN);
    }

    public long insert(String email, String password) {
        this.insertStmt.bindString(1, email);
        this.insertStmt.bindString(2, password);
        return this.insertStmt.executeInsert();
    }

    public long insertPin(String filePath){
        this.insertPinStmt.bindString(1, String.valueOf(0));
        this.insertPinStmt.bindString(2, String.valueOf(returnID(filePath)));
        this.insertPinStmt.bindString(3, String.valueOf(returnID(filePath)));
        return this.insertPinStmt.executeInsert();
    }

    public long insertPhoto(String path, String filename, String date, String size){
        this.insertPhotoStmt.bindString(1, path);
        this.insertPhotoStmt.bindString(2, filename);
        this.insertPhotoStmt.bindString(3, date);
        this.insertPhotoStmt.bindString(4, size);
        return this.insertPhotoStmt.executeInsert();
    }

    public void deleteAll() {
        this.db.delete(ACCOUNTS_TABLE, null, null);
    }

    public boolean checkPinLock(String filePath) {
        // Check if pin lock
        boolean result = false;
        Cursor cursor = this.db.query(PHOTOS_TABLE, new String[]{"path, pinLock"}, "path = '" + filePath + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            if(cursor.getInt(1) == 1){
                result = true;
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    public void enableLocationLock(String filePath, String coordinates, String replacementPath){
        //Set pin lock status to true
        Cursor cursor = db.rawQuery(ENABLE_LOCATION_PIN, new String[]{filePath});
        cursor.moveToFirst();
        cursor.close();
        //Update thumbnail
        Cursor cursor2 = db.rawQuery(SET_REPLACEMENT_PHOTO, new String[]{String.valueOf(returnID(replacementPath)), UUID.randomUUID().toString(), String.valueOf(returnID(filePath))});
        cursor2.moveToFirst();
        cursor2.close();
    }

    public void enablePinLock(String filePath, String replacementPath){
        //Set pin lock status to true
        Cursor cursor = db.rawQuery(ENABLE_LOCK_PIN, new String[]{filePath});
        cursor.moveToFirst();
        cursor.close();
        //Update thumbnail
        Cursor cursor2 = db.rawQuery(SET_REPLACEMENT_PHOTO, new String[]{String.valueOf(returnID(replacementPath)), UUID.randomUUID().toString(), String.valueOf(returnID(filePath))});
        cursor2.moveToFirst();
        cursor2.close();
    }

    public Image getReplacementPhoto(String filepath){
        int id = 1;
        //Find id of replacement photo
        Cursor cursor = db.rawQuery(FIND_PIN_REPLACEMENT, new String[]{String.valueOf(returnID(filepath))});
        if(cursor.moveToFirst()){
            id = cursor.getInt(0);
        }
        cursor.close();
        return getPhotoDetails(id);
    }

    private Image getPhotoDetails(int id){
        Cursor cursor = db.rawQuery(GET_PHOTO_DETAILS, new String[]{String.valueOf(id)});
        Image temp = new Image();
        if(cursor.moveToFirst()) {
            temp = new Image(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        }
        cursor.close();
        return temp;
    }

    private int returnID(String filepath){
        int id = 0;
        Cursor cursor = db.rawQuery(FIND_ID, new String[]{filepath});
        if(cursor.moveToFirst()){
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public boolean checkLocLock(String filePath) {
        // Check if pin lock
        boolean result = false;
        Cursor cursor = this.db.query(PHOTOS_TABLE, new String[] { "path, locationLock" }, "path = '" + filePath + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            if(cursor.getInt(1) == 1){
                result = true;
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
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

    public String selectFirst(String email, String password) {
        String pass = "";
        Cursor cursor = this.db.query(ACCOUNTS_TABLE, new String[] { "email", "password" }, "email = '"+ email +"' AND password= '"+ password+"'", null, null, null, "email desc");
        if (cursor.moveToFirst()) {
                pass = cursor.getString(1);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return pass;
    }

    public void updateEmail(String oldEmail, String newEmail) {
        Cursor c = this.db.rawQuery("UPDATE " + ACCOUNTS_TABLE + " SET email='" + newEmail + "' WHERE email=" + "'" + oldEmail + "'", null);
        c.moveToFirst();
        c.close();
    }

    public void updatePassword(String oldEmail, String newPass) {
        Cursor c = this.db.rawQuery("UPDATE " + ACCOUNTS_TABLE + " SET password='" + newPass + "' WHERE email=" + "'" + oldEmail + "'", null);
        c.moveToFirst();
        c.close();
    }

    private static class GalleryOpenHelper extends SQLiteOpenHelper {

        GalleryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE + "(id INTEGER PRIMARY KEY, email TEXT, password TEXT)");
            db.execSQL("CREATE TABLE " + PHOTOS_TABLE + "(id INTEGER PRIMARY KEY, filename TEXT, date TEXT, size TEXT, path TEXT, pinLock INTEGER DEFAULT 0, locationLock INTEGER DEFAULT 0, UNIQUE(path))");
            db.execSQL("CREATE TABLE " + LOCATION_TABLE + "(id INTEGER PRIMARY KEY, coordinates TEXT, radius TEXT, originalFK INTEGER, replacementFK INTEGER, FOREIGN KEY(originalFK) REFERENCES " + PHOTOS_TABLE + "(id), FOREIGN KEY(replacementFK) REFERENCES " + PHOTOS_TABLE + "(id))");
            db.execSQL("CREATE TABLE " + PIN_TABLE + "(id INTEGER PRIMARY KEY, passcode TEXT, originalFK INTEGER, replacementFK INTEGER, FOREIGN KEY(originalFK) REFERENCES " + PHOTOS_TABLE + "(id), FOREIGN KEY(replacementFK) REFERENCES " + PHOTOS_TABLE + "(id))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w("Example", "Upgrading database; this will drop and recreate the tables.");
            db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE);
            onCreate(db);
        }
    }
}
