package org.unipi.mpsp2343.smartalert;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.unipi.mpsp2343.smartalert.dto.LocationDto;
import org.unipi.mpsp2343.smartalert.dto.SavedAlert;
import org.unipi.mpsp2343.smartalert.dto.User;

import java.util.ArrayList;
import java.util.List;

//This class provided an interface for the various activities and services to interact with local storage
public class LocalStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myDB.db";
    private static final int DATABASE_VERSION = 1;
    Context context;

    public LocalStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Two tables are created, one to keep the logged in user's information
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                    "authToken TEXT, " +
                    "userId TEXT, " +
                    "email TEXT, " +
                    "refreshToken TEXT, " +
                    "expiresIn INTEGER, " +
                    "role TEXT);");
            //and one to keep of history of the alerts received from the server
            db.execSQL("CREATE TABLE IF NOT EXISTS SavedAlert (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "eventType INTEGER, " +
                    "lat REAL, " +
                    "lon REAL, " +
                    "timestamp TEXT);");
        } catch (Exception e) {
            Log.e("LocalStorage", "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade as needed
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS SavedAlert");
        onCreate(db);
    }

    //<editor-fold name="User operations">
    //Saves the logged in user's data in a local db
    public void saveUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("authToken", user.getAuthToken());
        values.put("userId", user.getUserId());
        values.put("email", user.getEmail());
        values.put("refreshToken", user.getRefreshToken());
        values.put("expiresIn", user.getExpiresIn());
        values.put("role", user.getRole());
        db.delete("User", null, null);
        db.insert("User", null, values);
        db.close();
    }

    //Retrieves the user's data from the local db
    public User getUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                "User",
                new String[]{"authToken", "userId", "email", "refreshToken", "expiresIn", "role"},
                null,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setAuthToken(cursor.getString(cursor.getColumnIndexOrThrow("authToken")));
            user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("userId")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setRefreshToken(cursor.getString(cursor.getColumnIndexOrThrow("refreshToken")));
            user.setExpiresIn(cursor.getLong(cursor.getColumnIndexOrThrow("expiresIn")));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            cursor.close();
            db.close();
            return user;
        } else {
            db.close();
            return null;
        }
    }

    //Deletes the user's data saved in local storage
    public void deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("User", null, null);
        db.close();
    }
    //</editor-fold>

    //<editor-fold name="Alert operations">
    //Saves a received alert to local storage
    public void saveAlert(SavedAlert savedAlert) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("eventType", savedAlert.getEventType());
        values.put("lat", savedAlert.getLocation().getLat());
        values.put("lon", savedAlert.getLocation().getLon());
        values.put("timestamp", savedAlert.getTimestamp());
        db.insert("SavedAlert", null, values);
        db.close();
    }

    //Retrieves all saved alerts from local storage
    @SuppressLint("Range")
    public List<SavedAlert> getAllAlerts() {
        List<SavedAlert> alertList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("SavedAlert",
                new String[]{"id", "eventType", "lat", "lon", "timestamp"},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                SavedAlert alert = new SavedAlert();
                alert.setEventType(cursor.getInt(cursor.getColumnIndex("eventType")));
                alert.setLocation(new LocationDto(cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lon"))));
                alert.setTimestamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                alertList.add(alert);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return alertList;
    }
    //</editor-fold>
}
