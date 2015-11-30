package com.grafixartist.gallery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
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
    private SQLiteStatement insertLocStmt;
    private static final String INSERT_PHOTO = "INSERT OR IGNORE INTO " + PHOTOS_TABLE + "(path, filename, date, size) values (?, ?, ?, ?)" ;
    private static final String INSERT = "INSERT INTO " + ACCOUNTS_TABLE + "(email, password) values (?, ?)" ;
    private static final String INSERT_PIN = "INSERT OR IGNORE INTO " + PIN_TABLE + "(passcode, originalFK, replacementFK) values (?, ?, ?)" ;
    private static final String INSERT_LOC = "INSERT OR IGNORE INTO " + LOCATION_TABLE + "(coordinates, originalFK, replacementFK) values (?, ?, ?)" ;
    private static final String FIND_ID = "SELECT id FROM " + PHOTOS_TABLE + " pt WHERE pt.path=?";
    private static final String FIND_PIN_REPLACEMENT = "SELECT replacementFK FROM " + PIN_TABLE + " p WHERE p.originalFK=?";
    private static final String FIND_LOC_REPLACEMENT = "SELECT replacementFK FROM " + LOCATION_TABLE + " p WHERE p.originalFK=?";
    private static final String GET_PHOTO_DETAILS = "SELECT path, filename, size, date FROM " + PHOTOS_TABLE + " p WHERE p.ID=?";
    private static final String ENABLE_LOCK_PIN = "UPDATE " + PHOTOS_TABLE + " SET pinLock=1 WHERE path=?";
    private static final String ENABLE_LOCK_LOCATION = "UPDATE " + PHOTOS_TABLE + " SET locationLock=1 WHERE path=?";
    private static final String DISABLE_LOCK_LOCATION = "UPDATE " + PHOTOS_TABLE + " SET locationLock=0 WHERE path=?";
    private static final String DISABLE_LOCK_PIN = "UPDATE " + PHOTOS_TABLE + " SET pinLock=0 WHERE path=?";
    private static final String SET_PIN_REPLACEMENT_PHOTO = "UPDATE " + PIN_TABLE + " SET replacementFK=?, passcode=? WHERE originalFK=?";
    private static final String SET_LOC_REPLACEMENT_PHOTO = "UPDATE " + LOCATION_TABLE + " SET radius = ?, coordinates=?, replacementFK=? WHERE originalFK=?";
    private static final String UNLOCK_LOC_REPLACEMENT_PHOTO = "UPDATE " + LOCATION_TABLE + " SET replacementFK=? WHERE originalFK=?";
    private static final String UNLOCK_PIN_REPLACEMENT_PHOTO = "UPDATE " + PIN_TABLE + " SET replacementFK=? WHERE originalFK=?";

    public DatabaseHelper(Context context) {
        this.context = context;
        GalleryOpenHelper openHelper = new GalleryOpenHelper(this.context);
        // Set up db access
        this.db = openHelper.getWritableDatabase();
        // Set up insert statements for db
        this.insertStmt = this.db.compileStatement(INSERT);
        this.insertPhotoStmt = this.db.compileStatement(INSERT_PHOTO);
        this.insertPinStmt = this.db.compileStatement(INSERT_PIN);
        this.insertLocStmt = this.db.compileStatement(INSERT_LOC);
    }

    public long insert(String email, String password) {
        // Insert statement for creating new account
        this.insertStmt.bindString(1, email);
        this.insertStmt.bindString(2, password);
        return this.insertStmt.executeInsert();
    }

    public long insertPin(String filePath){
        // Insert statement for creating new pin entry
        this.insertPinStmt.bindString(1, "0");
        this.insertPinStmt.bindString(2, String.valueOf(returnID(filePath)));
        this.insertPinStmt.bindString(3, String.valueOf(returnID(filePath)));
        return this.insertPinStmt.executeInsert();
    }

    public long insertLoc(String filePath){
        // Insert statement for creating new location entry
        this.insertLocStmt.bindString(1, "");
        this.insertLocStmt.bindString(2, String.valueOf(returnID(filePath)));
        this.insertLocStmt.bindString(3, String.valueOf(returnID(filePath)));
        return this.insertLocStmt.executeInsert();
    }

    public long insertPhoto(String path, String filename, String date, String size){
        // Insert statement for creating new photo entry
        this.insertPhotoStmt.bindString(1, path);
        this.insertPhotoStmt.bindString(2, filename);
        this.insertPhotoStmt.bindString(3, date);
        this.insertPhotoStmt.bindString(4, size);
        return this.insertPhotoStmt.executeInsert();
    }

    public boolean checkPinLock(String filePath) {
        boolean result = false;
        // Make query in photos table for specified original file path
        Cursor cursor = this.db.query(PHOTOS_TABLE, new String[]{"path, pinLock"}, "path = '" + filePath + "'", null, null, null, null);
        // Check cursor is not null
        if (cursor.moveToFirst()) {
            // If pinUnlock is 1, then return true
            if(cursor.getInt(1) == 1){
                result = true;
            }
        }
        // Don't forget to close cursor
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    public boolean checkLocLock(String filePath) {
        boolean result = false;
        // Make query in photos table checking for location lock
        Cursor cursor = this.db.query(PHOTOS_TABLE, new String[]{"path, locationLock"}, "path = '" + filePath + "'", null, null, null, null);
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

    public void enablePinLock(String filePath, String replacementPath){
        //Set pin lock status to true
        Cursor cursor = db.rawQuery(ENABLE_LOCK_PIN, new String[]{filePath});
        cursor.moveToFirst();
        cursor.close();
        //Update thumbnail
        Cursor cursor2 = db.rawQuery(SET_PIN_REPLACEMENT_PHOTO, new String[]{String.valueOf(returnID(replacementPath)), UUID.randomUUID().toString(), String.valueOf(returnID(filePath))});
        cursor2.moveToFirst();
        cursor2.close();
    }

    public String getPinUUID(String origFilePath) {
        String UUID = "";
        // Make query for UUID Pin given to specified file path
        Cursor cursor = this.db.query(PIN_TABLE, new String[]{"passcode"}, "originalFK = '" + String.valueOf(returnID(origFilePath)) + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            UUID = cursor.getString(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return UUID;
    }

    public void unlockPin(String origFilePath) {
        // Disable the pinLock in photos table
        Cursor cursor1 = db.rawQuery(DISABLE_LOCK_PIN, new String[]{origFilePath});
        cursor1.moveToFirst();
        cursor1.close();

        String id = String.valueOf(returnID(origFilePath));

        // Revert the replacement photo in photos table to original
        Cursor cursor2 = db.rawQuery(UNLOCK_PIN_REPLACEMENT_PHOTO, new String[]{id, id});
        cursor2.moveToFirst();
        cursor2.close();
    }

    public void enableLocationLock(String filePath, String coordinates, String replacementPath, String radius){
        //Set pin lock status to true
        Cursor cursor = db.rawQuery(ENABLE_LOCK_LOCATION, new String[]{filePath});
        cursor.moveToFirst();
        cursor.close();
        //Update thumbnail
        Cursor cursor2 = db.rawQuery(SET_LOC_REPLACEMENT_PHOTO, new String[]{radius, coordinates, String.valueOf(returnID(replacementPath)), String.valueOf(returnID(filePath))});
        cursor2.moveToFirst();
        cursor2.close();
    }

    public Image getReplacementPhoto(String filepath, int type){
        int id = 1;
        //Find id of replacement photo
        switch(type) {
            case(1): {
                // Make query to find replacement photo for a pin locked photo
                Cursor cursor = db.rawQuery(FIND_PIN_REPLACEMENT, new String[]{String.valueOf(returnID(filepath))});
                if(cursor.moveToFirst()){
                    id = cursor.getInt(0);
                }
                cursor.close();
                break;
            }
            case(2): {
                // Make query to find replacement photo for a location locked photo
                Cursor cursor = db.rawQuery(FIND_LOC_REPLACEMENT, new String[]{String.valueOf(returnID(filepath))});
                if(cursor.moveToFirst()){
                    id = cursor.getInt(0);
                }
                cursor.close();
                break;
            }
        }

        // Return details of that replacement photo
        return getPhotoDetails(id);
    }

    private Image getPhotoDetails(int id){
        // Get details of photo from given id
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
        // Make query to fetch id of the given file path in photos table
        Cursor cursor = db.rawQuery(FIND_ID, new String[]{filepath});
        if(cursor.moveToFirst()){
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public boolean checkEmailExists(String email) {
        List<String> list = new ArrayList<>();
        boolean result = false;
        // Make query to check if given email already exists in accounts table
        Cursor cursor = this.db.query(ACCOUNTS_TABLE, new String[] { "email" }, "email = '" + email + "'", null, null, null, "email desc");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        // If list is not empty, return true. Indicates email already exists in db
        if (!list.isEmpty()) {
            result = true;
        }
        return result;
    }

    public List<String> selectAll(String email, String password) {
        List<String> list = new ArrayList<>();
        // Make query to retrieve entry from accounts table using given email and password
        Cursor cursor = this.db.query(ACCOUNTS_TABLE, new String[] { "email", "password" }, "email = '"+ email +"' AND password= '"+ password+"'", null, null, null, "email desc");
        if (cursor.moveToFirst()) {
            do {
                // add to list
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
        // Make query to retrieve the password of the account given an email and password combination
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
        // Update email of specified account to new email
        Cursor c = this.db.rawQuery("UPDATE " + ACCOUNTS_TABLE + " SET email='" + newEmail + "' WHERE email=" + "'" + oldEmail + "'", null);
        c.moveToFirst();
        c.close();
    }

    public void updatePassword(String oldEmail, String newPass) {
        // Update password of the given email's account
        Cursor c = this.db.rawQuery("UPDATE " + ACCOUNTS_TABLE + " SET password='" + newPass + "' WHERE email=" + "'" + oldEmail + "'", null);
        c.moveToFirst();
        c.close();
    }

    public boolean checkUnlockLocation(String coordinates, String origFilePath){
        boolean result = false;

        Location loc = new Location("");

        // Location coordinates is separated by colon
        String[] parseCoordinates = coordinates.split(":");
        String latitude = parseCoordinates[0];
        String longitude = parseCoordinates[1];

        // Set up location with coordiantes given
        loc.setLatitude(Double.parseDouble(latitude));
        loc.setLongitude(Double.parseDouble(longitude));

        int id = returnID(origFilePath);

        // Get location entry of the id specified by file path
        Cursor cursor = this.db.query(LOCATION_TABLE, new String[]{"coordinates, originalFK, radius"}, "originalFK = '" + id + "'", null, null, null, null);

        // Check cursor isn't null
        if(cursor.moveToFirst()){
            // Retrieve coordinates, originalFk, and radius
            String dbCoordinates = cursor.getString(0);
            String originalFK = cursor.getString(1);
            double radius = Double.parseDouble(cursor.getString(2));
            String[] parseDbCoordinates = dbCoordinates.split(":");

            // Create new location from coordinates retrieved from db
            Location db = new Location("");
            db.setLatitude(Double.parseDouble(parseDbCoordinates[0]));
            db.setLongitude(Double.parseDouble(parseDbCoordinates[1]));

            // Determine distance between both locations
            double distance = loc.distanceTo(db);
            // Check distance is less than radius distance specified
            if(distance <= radius){
                // Disable lock location on photo
                Cursor updateLock = this.db.rawQuery(DISABLE_LOCK_LOCATION, new String[]{origFilePath});
                updateLock.moveToFirst();
                updateLock.close();

                // Revert replacement photo back to original photo
                Cursor updateReplacement = this.db.rawQuery(UNLOCK_LOC_REPLACEMENT_PHOTO, new String[]{originalFK, originalFK});
                updateReplacement.moveToFirst();
                updateReplacement.close();

                result = true;
            }
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return result;
    }

    private static class GalleryOpenHelper extends SQLiteOpenHelper {

        GalleryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates tables
            db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE + "(id INTEGER PRIMARY KEY, email TEXT, password TEXT)");
            db.execSQL("CREATE TABLE " + PHOTOS_TABLE + "(id INTEGER PRIMARY KEY, filename TEXT, date TEXT, size TEXT, path TEXT, pinLock INTEGER DEFAULT 0, locationLock INTEGER DEFAULT 0, UNIQUE(path))");
            db.execSQL("CREATE TABLE " + LOCATION_TABLE + "(id INTEGER PRIMARY KEY, coordinates TEXT, radius TEXT, originalFK INTEGER, replacementFK INTEGER, FOREIGN KEY(originalFK) REFERENCES " + PHOTOS_TABLE + "(id), FOREIGN KEY(replacementFK) REFERENCES " + PHOTOS_TABLE + "(id), UNIQUE(originalFK))");
            db.execSQL("CREATE TABLE " + PIN_TABLE + "(id INTEGER PRIMARY KEY, passcode TEXT, originalFK INTEGER, replacementFK INTEGER, FOREIGN KEY(originalFK) REFERENCES " + PHOTOS_TABLE + "(id), FOREIGN KEY(replacementFK) REFERENCES " + PHOTOS_TABLE + "(id), UNIQUE(originalFK))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Upgrade tables
            Log.w("Example", "Upgrading database; this will drop and recreate the tables.");
            db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE);
            onCreate(db);
        }
    }
}
