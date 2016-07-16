package com.shravyagarlapati.android.simpletodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shravyagarlapati on 7/9/16.
 */
public class ToDoItemDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "simpleToDoDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Info
    private static final String TABLE_TODO = "simpleToDoTable";
    private static final String KEY_TODOITEM= "toDoItems";

    private static ToDoItemDatabaseHelper sInstance;
    String TAG = "DATABASE TAG";

    public static synchronized ToDoItemDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ToDoItemDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public ToDoItemDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // These is where we need to write create table statements.
    // This is called when database is created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL for creating the tables
        try{
            String CREATE_TODO_TABLE = "CREATE TABLE " + TABLE_TODO +
                    "(" + KEY_TODOITEM + " TEXT" + ")";
            db.execSQL(CREATE_TODO_TABLE);
        }
        catch (Exception e){
            Log.d(TAG, "Error while Creating a database");
        }
    }

    // Insert or update an item in the database
    public long addOrUpdateToDoItem(ToDo toDo) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TODOITEM, toDo.itemValue);

            // First try to update the item in case the item already exists in the database
            int rows = db.update(TABLE_TODO, values, KEY_TODOITEM + "= ?", new String[]{toDo.itemValue});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s",
                        KEY_TODOITEM, TABLE_TODO);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(toDo.itemValue)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_TODO, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update Items");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    // Get all posts in the database
    public List<ToDo> getAllToDoItems() {
        List<ToDo> toDo = new ArrayList<>();
        String TODO_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_TODO);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODO_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    ToDo newVal = new ToDo();
                    newVal.itemValue = cursor.getString(cursor.getColumnIndex(KEY_TODOITEM));
                    toDo.add(newVal);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return toDo;
    }


    // Delete all todo items in the database
    public void deleteAllToDoItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_TODO, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

    //Delete a toDO item
    public void deleteToDoItem(String item) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_TODO, KEY_TODOITEM + "= '" + item +"'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete the item");
        } finally {
            db.endTransaction();
        }
    }

    // This method is called when database is upgraded like
    // modifying the table structure,
    // adding constraints to database, etc
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // SQL for upgrading the tables
        if(oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
            onCreate(db);
        }
    }
}
