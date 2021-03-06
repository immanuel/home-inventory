package com.immanuel.homeinventory;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.support.design.widget.Snackbar;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.duration;

@SuppressWarnings("deprecation")
public class ScannerActivity extends AppCompatActivity implements AddNewItemDialogFragment.AddNewItemDialogListener, View.OnClickListener{

    public static final String CODE_KEY = "com.immanuel.homeinventory.CODE";
    // TODO: convert to enum and constants class
    private static final int MODE_ADD = 0;
    private static final int MODE_REMOVE = 1;

    private InventoryDBHelper mInventoryDBHelper;

    private int cameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mFrameLayout;
    private PreviewCallback mPreviewCallback;

    private int scannerMode;
    private long recentlyRemovedItemID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.scanner_toolbar);
        setSupportActionBar(myToolbar);

        scannerMode = getIntent().getIntExtra("SCANNER_MODE", MODE_ADD);

        if(mInventoryDBHelper == null) {
            mInventoryDBHelper = new InventoryDBHelper(this);
        }

        mInventoryDBHelper.openDB();
    }

    @Override
    protected void onDestroy() {
        mInventoryDBHelper.closeDB();
        super.onDestroy();
    }

    public void returnCode(View view) {
        //Intent intent = new Intent();
        //EditText editText = (EditText) findViewById(R.id.editText2);
        //String message = editText.getText().toString();
        //intent.putExtra(CODE_KEY, message);
        //setResult(RESULT_OK, intent);

        mCamera.setOneShotPreviewCallback(mPreviewCallback);

        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            // Get the first rear-facing camera
            cameraId = 0;
            int numCameras = Camera.getNumberOfCameras();
            while(cameraId < numCameras){
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    break;
                }
                cameraId++;
            }
            mCamera = Camera.open(cameraId);
        } catch (RuntimeException e) {
            // TODO: Handle caase when camera cannot be opened
            System.err.println(e);
            return;
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, cameraId);
        mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        mFrameLayout.addView(mPreview);

        mPreviewCallback = new PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                        data,
                        previewSize.width, previewSize.height,
                        0, 0,
                        previewSize.width, previewSize.height,
                        false);
                Log.d("Got picture", previewSize.width + "x" + previewSize.height);
                Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
                hints.put(DecodeHintType.TRY_HARDER, true);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                Result result;
                try {
                    //result = reader.decode(bitmap);
                    String message;
                    result = reader.decode(bitmap, hints);
                    message = result.getText();
                    Log.d("Result", message);

                    String itemName = mInventoryDBHelper.getItemName(message);
                    if(itemName == null){
                        // Show dialog to ask for name
                        DialogFragment newItemDialog = new AddNewItemDialogFragment();
                        // Supply new item ID as an argument.
                        Bundle args = new Bundle();
                        args.putString("newItemID", message);
                        newItemDialog.setArguments(args);
                        newItemDialog.show(getSupportFragmentManager(), "new_item_dialog_fragment");

                    }
                    else{
                        if(scannerMode == MODE_ADD) {
                            mInventoryDBHelper.addItem(message);
                            Snackbar.make(
                                    findViewById(R.id.scanner_coordinator_layout),
                                    getResources().getString(R.string.scan_success, itemName),
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        else if(scannerMode == MODE_REMOVE) {
                            long removedItemID = mInventoryDBHelper.removeItem(message, true);
                            if(removedItemID > 0){
                                //Show message with undo button
                                recentlyRemovedItemID = removedItemID;
                                Snackbar.make(
                                        findViewById(R.id.scanner_coordinator_layout),
                                        getResources().getString(R.string.remove_success, itemName),
                                        Snackbar.LENGTH_SHORT)
                                        .setAction(R.string.undo, ScannerActivity.this)
                                        .show();
                            }
                            else{
                                Snackbar.make(
                                        findViewById(R.id.scanner_coordinator_layout),
                                        getResources().getString(R.string.remove_failure, itemName),
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }

                } catch (ReaderException re) {
                    Log.d("Exception", "no result");
                    Snackbar.make(findViewById(R.id.scanner_coordinator_layout), R.string.scan_error, Snackbar.LENGTH_SHORT)
                            .show();
                    System.err.println(re);
                }

            }
        };
    }

    public void onDialogAddClick(String newItemID, String newItemName){
        mInventoryDBHelper.addNewItem(newItemID, newItemName);

        if(scannerMode == MODE_ADD) {
            mInventoryDBHelper.addItem(newItemID);
            Snackbar.make(
                    findViewById(R.id.scanner_coordinator_layout),
                    getResources().getString(R.string.scan_success, newItemName),
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop preview and release camera
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        mFrameLayout.removeView(mPreview);
        mPreview = null;

        mPreviewCallback = null;
    }

    @Override
    public void onClick(View v) {
        // Code to undo the user's last action

        if(recentlyRemovedItemID > 0){
            mInventoryDBHelper.undoRemoval(recentlyRemovedItemID);
            recentlyRemovedItemID = -1;
        }
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
                scannerMode = MODE_ADD;
                return true;

            case R.id.remove_items:
                scannerMode = MODE_REMOVE;
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
