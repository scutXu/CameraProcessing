package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Looper;
import android.view.Surface;
import android.graphics.SurfaceTexture;
import android.util.Size;
import android.util.Log;
import java.util.Arrays;
import android.os.Handler;
import java.lang.Runnable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityThreadHandler = new Handler(Looper.getMainLooper());

        mGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(mGLSurfaceView);

        //setContentView(R.layout.activity_main);

        mCameraManager  = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        getCameraInfo();                //get the camera id and first resolution
        if(mCameraId == null) {
            Log.e(appTag,"can not find suitable camera");
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
    }


    //methods
    public void createSurfaceTexture(int glTextureObject) {
        mSurfaceTexture = new SurfaceTexture(glTextureObject);
        mSurfaceTexture.setDefaultBufferSize(mSurfaceTextureResolution.getWidth(), mSurfaceTextureResolution.getHeight());
        mSurface = new Surface(mSurfaceTexture);
        openCamera();

        //debug test,got identity matrix
        /*float [] matrix = new float[16];
        mSurfaceTexture.getTransformMatrix(matrix);
        for(int i=0;i<16;++i) {
            Log.i("ttt",Float.toString(matrix[i]));
        }*/
    }
    public void updateSurfaceTexture() {
        mSurfaceTexture.updateTexImage();
    }
    public float getCameraResolutionRatio() {
        return (float)(mSurfaceTextureResolution.getWidth()) / (float)(mSurfaceTextureResolution.getHeight());
    }

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
    private void openCamera() {
        try {
            mCameraManager.openCamera(mCameraId, deviceCallback, mActivityThreadHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //embedded class
    private CameraDevice.StateCallback deviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startCapture();
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
            //mSurfaceTexture.updateTexImage();     should be invoked in the thread that holding the gl context
            mGLSurfaceView.requestRender();
        }
    };

    //members
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder mRequestBuilder;
    private String mCameraId = null;
    private Size mSurfaceTextureResolution;
    private Surface mSurface = null;
    private SurfaceTexture mSurfaceTexture = null;
    private GLSurfaceView mGLSurfaceView;
    private Handler mActivityThreadHandler;
    public static String appTag = "myTag";
}
