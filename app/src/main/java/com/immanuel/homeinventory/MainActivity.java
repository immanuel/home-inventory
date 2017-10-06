package com.immanuel.homeinventory;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private InventoryDBHelper mInventoryDBHelper;
    private SimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if(mInventoryDBHelper == null) {
            mInventoryDBHelper = new InventoryDBHelper(this);
        }

        mInventoryDBHelper.openDB();

        Cursor cursor = mInventoryDBHelper.getAvailableItems();
        // The desired columns to be bound
        String[] columns = new String[] {
                mInventoryDBHelper.COL_ITEM_NAME
        };
        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.textViewItemName
        };
        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        mCursorAdapter = new SimpleCursorAdapter(
                this, R.layout.item_layout,
                cursor,
                columns,
                to,
                0);
        ListView listView = (ListView) findViewById(R.id.itemListView);
        // Assign adapter to ListView
        listView.setAdapter(mCursorAdapter);
    }

    @Override
    protected void onDestroy() {
        mInventoryDBHelper.closeDB();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor cursor = mInventoryDBHelper.getAvailableItems();
        mCursorAdapter.changeCursor(cursor);

    }

    public void startScanner(View view) {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_items:
                Intent intent = new Intent(this, ScannerActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                // Get the Intent that started this activity and extract the string
                String message = data.getStringExtra(ScannerActivity.CODE_KEY);

                // Capture the layout's TextView and set the string as its text
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(message);
            }
        }
    }
}
