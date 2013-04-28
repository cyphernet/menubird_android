package com.menubird;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


public class MainActivity extends Activity {

	 private Preview mPreview;
	 Camera mCamera;
	 int numberOfCameras;
	 int cameraCurrentlyLocked;
	 private MainActivity mainActivity;
	 WebView wv;

	 // The first rear facing camera
	 int defaultCameraId;

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 
		// Hide the window title.
				 requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);		 
		
		 setContentView(R.layout.activity_main);
		 mainActivity=this;
	
		 // Create a RelativeLayout container that will hold a SurfaceView,
		 // and set it as the content of our activity.
		 mPreview = new Preview(this);	 

	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	    preview.addView(mPreview);

	        wv = (WebView) findViewById(R.id.web_view);
	        WebSettings webSettings = wv.getSettings();
	        webSettings.setJavaScriptEnabled(true);
	        wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	        wv.setBackgroundColor(0x00000000);
	        wv.addJavascriptInterface(new WebAppInterface(this), "Android");
	        
	        wv.loadUrl("file:///android_asset/index.html");
	
		 // Find the total number of cameras available
		 numberOfCameras = Camera.getNumberOfCameras();
	
		 // Find the ID of the default camera
		 CameraInfo cameraInfo = new CameraInfo();
		 	for (int i = 0; i < numberOfCameras; i++) {
		 		Camera.getCameraInfo(i, cameraInfo);
		 		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
		 			defaultCameraId = i;
		 		}
		 	}
		 	

	 }
	 
	public void take_picture()  {
		mCamera.takePicture(null, null, mPicture);
	}

	public void call_javascript(String js) {
		wv.loadUrl("javascript:"+js);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
	        wv.goBack();
	        wv.loadUrl("javascript:back()");
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate our menu which can gather user input for switching camera
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	    	Bitmap srcBmp=BitmapFactory.decodeByteArray(data,0,data.length);
	    	Matrix mtx = new Matrix();
	        mtx.postRotate(90);
	        
	    	Bitmap dstBmp = Bitmap.createBitmap(
	    		     srcBmp, 
	    		     300,
	    		     90,
	    		     srcBmp.getWidth()-2800, 
	    		     srcBmp.getHeight()-300,
	    		     mtx, false
	    	);
	    	
	    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    	dstBmp.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	    	HttpPost httpPost = new HttpPost("http://198.46.152.175:8080/ocr");

	    	byte[] dest_data = outStream.toByteArray();
	    	
	    	try {
	            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	            entity.addPart("file", new ByteArrayBody(dest_data, "image.jpg"));
	            entity.addPart("key", new StringBody("value"));

	            httpPost.setEntity(entity);

	            //HttpResponse response = httpClient.execute(httpPost, localContext);
	            new HttpAsyncRequest(mainActivity).execute(httpPost); 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
	    	mCamera.startPreview();
	    }
	};

	
	/*
	protected static final String TAG = "MainActivity";
	private Camera mCamera;
    private CameraPreview mPreview;
    private MainActivity mainActivity;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mainActivity=this;
		// Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                }
            }
        );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	    	Bitmap srcBmp=BitmapFactory.decodeByteArray(data,0,data.length);
	    	Bitmap dstBmp = Bitmap.createBitmap(
	    		     srcBmp, 
	    		     srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
	    		     0,
	    		     srcBmp.getHeight(), 
	    		     srcBmp.getHeight()
	    		     );
	    	
	    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    	dstBmp.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	    	HttpPost httpPost = new HttpPost("http://198.46.152.175:8080/ocr");
	    	
	    	try {
	            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	            entity.addPart("file", new ByteArrayBody(data, "image.jpg"));
	            entity.addPart("key", new StringBody("value"));

	            httpPost.setEntity(entity);

	            //HttpResponse response = httpClient.execute(httpPost, localContext);
	            new HttpAsyncRequest(mainActivity).execute(httpPost); 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
	    	mCamera.startPreview();
	    }
	};
*/
}
