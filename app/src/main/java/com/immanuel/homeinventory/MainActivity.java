package com.immanuel.homeinventory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startScanner(View view) {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, 1);
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
