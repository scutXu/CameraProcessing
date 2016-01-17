package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.view.TextureView;
import android.view.Surface;
import android.graphics.SurfaceTexture;
import android.util.Size;
import android.util.Log;
import java.util.Arrays;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new MyGLSurfaceView(this));

        //setContentView(R.layout.activity_main);

        //create preview
        /*mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        setContentView(mTextureView);

        mCameraManager  = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        getCameraInfo();
        if(mCameraId == null) {
            Log.e(appTag,"can not find suitable camera");
        }
        else {
            try {
                mCameraManager.openCamera(mCameraId, deviceCallback, null);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/
    }
    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
    }


    //methods
    private void getCameraInfo() {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mSurfaceTextureResolution = (map.getOutputSizes(SurfaceTexture.class))[0];
                    mCameraId = cameraId;
                    return;
                }
            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void startCapture() {
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface), sessionStateCallback, null);
            mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mRequestBuilder.addTarget(mSurface);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.close();
            mCameraDevice.close();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //callbacks
    private CameraDevice.StateCallback deviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            if(mSurface !=null) {
                startCapture();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
        }
    };
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // TODO Auto-generated method stub
            mSurfaceTexture = surface;
            mSurfaceTexture.setDefaultBufferSize(mSurfaceTextureResolution.getWidth(),mSurfaceTextureResolution.getHeight());
            mSurface = new Surface(mSurfaceTexture);
            if(mCameraDevice != null) {
                startCapture();
            }
        }
    };
    private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            // TODO Auto-generated method stub
            mCaptureSession = session;
            try {
                mCaptureSession.setRepeatingRequest(mRequestBuilder.build(),captureCallback,null);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            // TODO Auto-generated method stub

        }
    };
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted (CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

        }
    };

    //members
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder mRequestBuilder;
    private String mCameraId = null;
    private Size mSurfaceTextureResolution;
    private TextureView mTextureView;
    private Surface mSurface = null;
    private SurfaceTexture mSurfaceTexture = null;

    public static String appTag = "myTag";
}
