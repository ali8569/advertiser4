package ir.markazandroid.advertiser.view.gold.camera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.activity.BaseActivity;

public class GoldCameraActivity extends BaseActivity {

    public static final String IMAGE_PATH = "ir.markazandroid.advertiser.view.gold.camera.GoldCameraActivity.IMAGE_PATH";
    private static final String TAG = "GoldCameraActivity";

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView counterTV;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_camera);

        counterTV = findViewById(R.id.counter_text);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        //mCamera.startFaceDetection();
        mCamera.setDisplayOrientation(270);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        timer = new CountDownTimer(6000, 1000) {
            int d = 5;

            @Override
            public void onTick(long millisUntilFinished) {
                counterTV.setText(d + "");
                d--;
            }

            @Override
            public void onFinish() {
                mCamera.takePicture(null, null, mPicture);
            }
        }.start();

    }


    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware() {
        // this device has a camera
// no camera on this device
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = openFrongCamera();// attempt to get a Camera instance
            if (c == null) c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static Camera openFrongCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return Camera.open(i);
            }
        }
        return null;
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {

        Intent result = new Intent();
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.e(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            result.putExtra(GoldCameraActivity.IMAGE_PATH, pictureFile.getPath());
            setResult(RESULT_OK, result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error accessing file: " + e.getMessage());
        } finally {
            finish();
        }
    };

    private File getOutputMediaFile() {
        String path = getIntent().getStringExtra(IMAGE_PATH);
        if (path == null) return new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        return new File(path);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        mCamera.release();
    }
}
