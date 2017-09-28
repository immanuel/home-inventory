package com.immanuel.homeinventory;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

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

@SuppressWarnings("deprecation")
public class ScannerActivity extends AppCompatActivity {

    public static final String CODE_KEY = "com.immanuel.homeinventory.CODE";

    private int cameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mFrameLayout;
    private PreviewCallback mPreviewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

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
                String message;
                try {
                    //result = reader.decode(bitmap);
                    result = reader.decode(bitmap, hints);
                    message = result.getText();
                    Log.d("Result", result.getText());
                } catch (ReaderException re) {
                    message = "Not found";
                    Log.d("Exception", "no result");
                    System.err.println(re);
                }

                Intent intent = new Intent();
                intent.putExtra(CODE_KEY, message);
                setResult(RESULT_OK, intent);

                finish();
            }
        };
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

}
