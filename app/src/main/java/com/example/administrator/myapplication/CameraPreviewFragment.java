package com.example.administrator.myapplication;


import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraPreviewFragment extends Fragment {


    public CameraPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i("lifeCycle","FragmentOnCreate");

        super.onCreate(savedInstanceState);
        mActivity = (MainActivity)getActivity();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mCameraManager  = (CameraManager)mActivity.getSystemService(Context.CAMERA_SERVICE);
        queryCameraInfo();                //get the camera id and first resolution
        if(mCameraId == null) {
            Log.e(mActivity.appTag, "can not find suitable camera");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("lifeCycle","FragmentOnCreateView");

        mGLSurfaceView = new MyGLSurfaceView(mActivity,this);
        return  mGLSurfaceView;
    }

    @Override
    public void onDestroyView() {

        Log.i("lifeCycle","FragmentOnDestroyView");

        closeCamera();
    }

    public void onGLViewAvailable() {
        openCamera();
    }
    public void createSurfaceTexture(int glTextureObject) {
        mSurfaceTexture = new SurfaceTexture(glTextureObject);
        mSurfaceTexture.setDefaultBufferSize(mSurfaceTextureResolution.getWidth(), mSurfaceTextureResolution.getHeight());
        mSurface = new Surface(mSurfaceTexture);

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


    private void queryCameraInfo() {
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
            mCameraManager.openCamera(mCameraId, deviceCallback, mMainThreadHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


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


    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder mRequestBuilder;
    private String mCameraId = null;
    private Size mSurfaceTextureResolution;
    private Surface mSurface = null;
    private SurfaceTexture mSurfaceTexture = null;
    private GLSurfaceView mGLSurfaceView;
    private MainActivity mActivity;
    private Handler mMainThreadHandler;
}
