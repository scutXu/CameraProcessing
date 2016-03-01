package com.example.administrator.myapplication;

/**
 * Created by Administrator on 2016/1/16.
 */

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class MyGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    MyGLSurfaceView(MainActivity context,CameraPreviewFragment fragment) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
        mFragment = fragment;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // TODO Auto-generated method stub

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        float [] vertices = new float[] {
                -1,-1,
                1,-1,
                1,1,
                -1,1
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertices = bb.asFloatBuffer();
        mVertices.put(vertices).position(0);

        createTexture();

        EffectManager.getInstance().loadEffects();

        useEffect("normal");
        //useEffect("monochrome");



        mFragment.onGLViewAvailable();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // TODO Auto-generated method stub
        //Log.i("ttt",Integer.toString(width) + "---" + Integer.toString(height));

        int newWidth = width, newHeight = height;
        float ratio = mFragment.getCameraResolutionRatio();
        if(ratio > 0.000001f) {
            if(width < height) {
                newHeight = (int)(((float)(width)) / ratio);
            }
            else {
                newWidth = (int)(height * ratio);
            }
        }
        GLES20.glViewport((width - newWidth) / 2, (height - newHeight) / 2, newWidth, newHeight);

        //debug test
        //Log.i("ttt",Integer.toString(width) + "---" + Integer.toString(height));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mFragment.updateSurfaceTexture();

        // TODO Auto-generated method stub
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if(mEffectDirty) {
            refreshEffect();
            mEffectDirty = false;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
    }

    private void refreshEffect() {
        Effect effect = EffectManager.getInstance().getEffect(mCurrentEffect);
        int program = effect.getProgram();
        GLES20.glUseProgram(program);
        int posAttrib = GLES20.glGetAttribLocation(program, "pos");
        int texUni = GLES20.glGetUniformLocation(program, "tex");
        int normalizedPixelWidthUni = GLES20.glGetUniformLocation(program,"dWidth");
        int normalizedPixelHeightUni = GLES20.glGetUniformLocation(program,"dHeight");

        GLES20.glUniform1i(texUni, 0);
        Size textureSize = mFragment.getSurfaceTextureResolution();
        if(normalizedPixelHeightUni >= 0) {
            GLES20.glUniform1f(normalizedPixelHeightUni,1.0f / textureSize.getHeight());
        }
        if(normalizedPixelWidthUni >= 0) {
            GLES20.glUniform1f(normalizedPixelWidthUni,1.0f / textureSize.getWidth());
        }
        GLES20.glEnableVertexAttribArray(posAttrib);
        GLES20.glVertexAttribPointer(posAttrib, 2, GLES20.GL_FLOAT, false, 0, mVertices);
    }

    public void useEffect(String name) {
        if(mCurrentEffect != name) {
            mCurrentEffect = name;
            mEffectDirty = true;
        }
    }


    private void createTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);   //unnecessary to invoke if there is only one texture unit
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        mBackgroundTexture = textures[0];
        mFragment.createSurfaceTexture(mBackgroundTexture);

    }


    private int mBackgroundTexture;
    private FloatBuffer mVertices;
    private  CameraPreviewFragment mFragment;
    private String mCurrentEffect;
    private boolean mEffectDirty;
}
