package com.immanuel.homeinventory;

import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.List;

@SuppressWarnings("deprecation")
public class ScannerActivity extends AppCompatActivity {

    public static final String CODE_KEY = "com.immanuel.homeinventory.CODE";

    private int cameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
    }

    public void returnCode(View view) {
        Intent intent = new Intent();
        EditText editText = (EditText) findViewById(R.id.editText2);
        String message = editText.getText().toString();
        intent.putExtra(CODE_KEY, message);
        setResult(RESULT_OK, intent);
        finish();
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
    }

}
