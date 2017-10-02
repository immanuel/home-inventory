package com.immanuel.homeinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by immanuel on 10/1/17.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Inventory.db";

    public static final String TABLE_ITEMS = "items";
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_NAME = "name";

    private SQLiteDatabase db;

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + COL_ITEM_ID + " TEXT PRIMARY KEY,"
                + COL_ITEM_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        // Create tables again
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void openDB(){
        db = this.getWritableDatabase();
    }

    public void closeDB(){
        db.close();
    }

    public void addNewItem(String itemID, String itemName) {

        //TODO: check if db is null

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_ID, itemID);
        values.put(COL_ITEM_NAME, itemName);

        // Inserting Row
        db.insert(TABLE_ITEMS, null, values);
    }

    public String getItemName(String itemID) {

        //TODO: check if db is null
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                COL_ITEM_NAME,
                COL_ITEM_ID
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = COL_ITEM_ID + " = ?";
        String[] selectionArgs = { itemID };

        Cursor cursor = db.query(
                TABLE_ITEMS,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if(cursor.moveToNext()){
            return cursor.getString(
                    cursor.getColumnIndexOrThrow(COL_ITEM_NAME)
            );
        }
        else{
            return null;
        }
    }
}
