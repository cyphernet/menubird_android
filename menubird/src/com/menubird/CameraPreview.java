package com.menubird;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;

import android.hardware.Camera.Size;

import android.os.Build;
import android.util.Log;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;


import java.io.IOException;
import java.util.List;


// ----------------------------------------------------------------------


/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;
    MainActivity activity;

    Preview(MainActivity context) {
        super(context);
        activity = context;


        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
       setCamera(camera);
       try {
           camera.setPreviewDisplay(mHolder);
       } catch (IOException exception) {
           Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
       }
       Camera.Parameters parameters = camera.getParameters();
       parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
       requestLayout();

       camera.setParameters(parameters);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

 
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        Log.v("w", String.valueOf(w));
        Log.v("h", String.valueOf(h));
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
        	
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	
    	 try {
             mCamera.stopPreview();
         } catch (Exception e){
           // ignore: tried to stop a non-existent preview
         }
    	 
    	 
    	 
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        int pic_width = 0;
		int pic_height = 0;
        
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
        	// I assume the first is the largest
            
            pic_width = size.width;
            pic_height = size.height;
            break;
        }

        parameters.setPictureSize(pic_width, pic_height);
        
        requestLayout();

  
        
        try {
            
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            setCameraDisplayOrientation(activity, activity.defaultCameraId, mCamera);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public static void setCameraDisplayOrientation(MainActivity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
