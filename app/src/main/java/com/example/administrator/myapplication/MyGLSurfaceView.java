package com.example.administrator.myapplication;

/**
 * Created by Administrator on 2016/1/16.
 */

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class MyGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    MyGLSurfaceView(MainActivity context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
        mMainActivity = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // TODO Auto-generated method stub

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);


        createPrograms();

        createTexture();




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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // TODO Auto-generated method stub
        float ratio = mMainActivity.getCameraResolutionRatio();
        if(ratio > 0.000001f) {
            if(width > height) {
                height = (int)(((float)(width)) / ratio);
            }
            else {
                width = (int)(height * ratio);
            }
        }
        GLES20.glViewport(0, 0, width, height);

        //debug test
        //Log.i("ttt",Integer.toString(width) + "---" + Integer.toString(height));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mMainActivity.updateSurfaceTexture();

        // TODO Auto-generated method stub
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnableVertexAttribArray(mPosAttrib);
        GLES20.glVertexAttribPointer(mPosAttrib, 2, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
    }


    private  static int createShader(int type,String code) {
        Log.i(MainActivity.appTag, code);
        int shader = GLES20.glCreateShader(type);
        int [] compileStatus = new int[1];
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] != GLES20.GL_TRUE) {
            Log.e(MainActivity.appTag,"compile shader fail");
            String info = GLES20.glGetShaderInfoLog(shader);
            Log.e(MainActivity.appTag,code);
            Log.e(MainActivity.appTag,info);
            GLES20.glDeleteShader(shader);
        }
        return  shader;
    }
    private void createPrograms() {
        Resources res = mMainActivity.getResources();

        //int resId = res.getIdentifier("normal", "raw", context.getPackageName());

        //create vertex shader
        InputStream input = res.openRawResource(R.raw.vertex_shader);
        String text = Utils.getString(input);
        try {
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        int vertexShader = createShader(GLES20.GL_VERTEX_SHADER,text);

        //create fragment shaders
        input = res.openRawResource(R.raw.normal);
        text = Utils.getString(input);
        try {
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        int fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER,text);

        //create programs
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program,vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);


        GLES20.glUseProgram(program);
        mPosAttrib = GLES20.glGetAttribLocation(program, "pos");
        mTexUniform = GLES20.glGetUniformLocation(program, "tex");
        GLES20.glUniform1i(mTexUniform, 0);
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
        mMainActivity.createSurfaceTexture(mBackgroundTexture);

        //debug test texture
        /*float [] pixels = {
                1,0,0,
                0,1,0,
                0,0,1,
                1,1,0
        };
        ByteBuffer aaa = ByteBuffer.allocateDirect(pixels.length * 4);
        aaa.order(ByteOrder.nativeOrder());
        FloatBuffer ttt = aaa.asFloatBuffer();
        ttt.put(pixels).position(0);
        GL10.glTexImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, GLES20.GL_RGB, 2, 2, 0, GLES20.GL_RGB, GLES20.GL_FLOAT, ttt);*/
    }

    private int mBackgroundTexture;
    private int [] mShaderPrograms;
    private FloatBuffer mVertices;
    private int mPosAttrib;
    private int mTexUniform;
    MainActivity mMainActivity;
}
