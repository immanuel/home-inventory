package com.immanuel.homeinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

/**
 * Created by immanuel on 10/1/17.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Inventory.db";

    public static final String TABLE_ITEMS = "items";
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_NAME = "name";

    public static final String TABLE_INVENTORY = "inventory";
    public static final String COL_INVENTORY_ITEM_ID = "item_id";
    public static final String COL_ADDED = "added";
    public static final String COL_REMOVED = "removed";
    public static final String COL_IS_AVAILABLE = "is_available";
    public static final String COL_ROWID = "rowid";

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

        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
                + COL_INVENTORY_ITEM_ID + " TEXT,"
                + COL_ADDED + " INTEGER,"
                + COL_IS_AVAILABLE + " INTEGER,"
                + COL_REMOVED + " INTEGER"
                + ")";
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
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
        db.insertWithOnConflict(TABLE_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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

    public void addItem(String itemID) {
        //TODO: check if db is null

        ContentValues values = new ContentValues();
        values.put(COL_INVENTORY_ITEM_ID, itemID);
        values.put(COL_IS_AVAILABLE, 1);
        values.put(COL_ADDED, Calendar.getInstance().getTimeInMillis());
        values.put(COL_REMOVED, 0);

        // Inserting Row
        db.insert(TABLE_INVENTORY, null, values);

    }

    public int removeItem(String itemID, boolean fifo) {
        // Identify the row to delete

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                COL_ROWID,
                COL_ADDED
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = COL_IS_AVAILABLE + " = 1 and "
                + COL_INVENTORY_ITEM_ID + " = ?";
        String[] selectionArgs = { itemID };
        String sortOrder;
        if(fifo){
            sortOrder = COL_ADDED + " ASC";
        }
        else{
            sortOrder = COL_ADDED + " DESC";
        }

        Cursor cursor = db.query(
                TABLE_INVENTORY,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if(cursor.moveToNext()){
            long rowID = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COL_ROWID)
            );

            String[] deletionArgs = {Long.toString(rowID)};
            return db.delete(TABLE_INVENTORY, COL_ROWID + " = ?", deletionArgs);
        }

        return 0;
    }
}
