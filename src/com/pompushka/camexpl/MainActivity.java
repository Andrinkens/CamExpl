package com.pompushka.camexpl;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView tv;
	private Camera myCam;
	private CameraPreview mPreview;
	private FilterView fPreview;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        /*
        if (checkCameraHardware(getApplicationContext())) 
        	tv.setText("There are "+ String.valueOf(Camera.getNumberOfCameras()) + " cameras");
        else tv.setText("There is no cam");
        
        CameraInfo cameraInfo = null;
        Camera.getCameraInfo(1, cameraInfo);
        if (cameraInfo.facing == cameraInfo.CAMERA_FACING_BACK){
        	tv.setText("Камера 1 задняя");
        	myCam = getCameraInstance();
        }

        myCam.release();*/
        
        // Create an instance of Camera
        myCam = getCameraInstance(1);
        
        fPreview = new FilterView(this);
        FrameLayout previewf = (FrameLayout) findViewById(R.id.filter_preview);
        previewf.addView(fPreview);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, myCam, fPreview);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        myCam.release();
    }
    
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(int camera){
        Camera c = null;
        try {
            c = Camera.open(camera); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
