package com.example.administrator.myapplication;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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

        Log.i("lifeCycle", "FragmentOnCreateView");

        View fragmentView = inflater.inflate(R.layout.preview_fragment,container,false);

        FrameLayout previewContainer = (FrameLayout)(fragmentView.findViewById(R.id.preview_container));
        mGLSurfaceView = new MyGLSurfaceView(mActivity,this);
        mGLSurfaceView.setOnClickListener(clickListener);
        mGLSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        previewContainer.addView(mGLSurfaceView);

        String [] effectNames = EffectManager.getInstance().getAllEffectNames();
        RadioGroup radioGroup = (RadioGroup)(fragmentView.findViewById(R.id.radioGroup1));
        for(int i=0;i<effectNames.length;++i) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(effectNames[i]);
            radioGroup.addView(radioButton);
            if(i == 0) {
                radioButton.setChecked(true);
            }
        }
        radioGroup.setOnCheckedChangeListener(checkedChangeListener);
        return  fragmentView;
    }

    @Override
    public void onDestroyView() {

        Log.i("lifeCycle","FragmentOnDestroyView");
        super.onDestroyView();
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
        //Log.i("abc",Integer.toString(mSurfaceTextureResolution.getWidth()) + " " + Integer.toString(mSurfaceTextureResolution.getHeight()));
        return (float)(mSurfaceTextureResolution.getWidth()) / (float)(mSurfaceTextureResolution.getHeight());
    }


    private void queryCameraInfo() {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    //debug test
                    int [] outputFormats = map.getOutputFormats();
                    for(int i=0;i<outputFormats.length;++i) {
                        Log.i(MainActivity.appTag,Integer.toString(outputFormats[i]));
                    }

                    if(!map.isOutputSupportedFor(mCameraImageFormat)) {
                        Log.e(MainActivity.appTag,"pixel format not support");
                    }
                    Size imageResolution = (map.getOutputSizes(mCameraImageFormat))[0];
                    mImageReader = ImageReader.newInstance(imageResolution.getWidth(), imageResolution.getHeight(),
                            mCameraImageFormat, 2);
                    mImageReader.setOnImageAvailableListener(imageAvailableListener, null);
                    Size [] choices = map.getOutputSizes(SurfaceTexture.class);
                    boolean found = false;
                    for(int i=0;i<choices.length;++i) {
                        Size size = choices[i];
                        if(imageResolution.getWidth() * size.getHeight() == imageResolution.getHeight() * size.getWidth()) {
                            mSurfaceTextureResolution = size;
                            found = true;
                            Log.i(MainActivity.appTag,"Resolution:" + Integer.toString(imageResolution.getWidth()) + " " + Integer.toString(imageResolution.getHeight())
                                    + " " + Integer.toString(size.getWidth()) + " " + Integer.toString(size.getHeight()) );
                            break;
                        }
                    }
                    if(!found) {
                        mSurfaceTextureResolution = choices[0];
                        Log.i(MainActivity.appTag,"can not find suitable resolution");
                    }
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
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface,mImageReader.getSurface()), sessionStateCallback, null);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mPreviewRequestBuilder.addTarget(mSurface);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
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

    private void captureImage() {
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.capture(mCaptureRequestBuilder.build(),stillCaptureCallback,null);
        }
        catch (CameraAccessException e) {
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
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),captureCallback,null);
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
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(MainActivity.appTag,"onClick");
            captureImage();
        }
    };
    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = (RadioButton) (group.findViewById(checkedId));
            mGLSurfaceView.useEffect(radioButton.getText().toString());
        }
    };
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            ByteBuffer argbBuffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
            Utils.yuv420888ToArgb888(image, argbBuffer);
            Bitmap bitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(),mBitmapFormat);
            bitmap.copyPixelsFromBuffer(argbBuffer);
            image.close();
            ImageDisplayFragment imageDisplayFragment = new ImageDisplayFragment();
            imageDisplayFragment.setDisplayBitmap(bitmap);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, imageDisplayFragment);
            transaction.addToBackStack("");
            transaction.commit();
        }
    };
    private CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted (CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

        }
    };

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private String mCameraId = null;
    private Size mSurfaceTextureResolution;
    private Surface mSurface = null;
    private SurfaceTexture mSurfaceTexture = null;
    private MyGLSurfaceView mGLSurfaceView;
    private MainActivity mActivity;
    private Handler mMainThreadHandler;
    private ImageReader mImageReader;
    private int mCameraImageFormat = ImageFormat.YUV_420_888;         //recommended format for receiving camera data
    private Bitmap.Config mBitmapFormat = Bitmap.Config.ARGB_8888;
}
