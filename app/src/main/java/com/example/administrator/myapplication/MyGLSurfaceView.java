package com.example.administrator.myapplication;

/**
 * Created by Administrator on 2016/1/16.
 */

import android.content.Context;
import android.content.res.Resources;
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

    MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // TODO Auto-generated method stub

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        //create texture);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mBackgroundTexture = textures[0];

        createPrograms();

        float [] vertices = new float[] {
                -1,-1,
                1,-1,
                1,1,
                -1,1
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertices = bb.asFloatBuffer();
        mVertices.put(vertices);
        mVertices.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // TODO Auto-generated method stub
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // TODO Auto-generated method stub
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnableVertexAttribArray(mUniformPosLoc);
        GLES20.glVertexAttribPointer(mUniformPosLoc, 2, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

    }


    private  static int createShader(int type,String code) {
        Log.i(MainActivity.appTag,code);
        int shader = GLES20.glCreateShader(type);
        int [] compileStatus = new int[1];
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compileStatus,0);
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
        Context context = getContext();
        Resources res = context.getResources();

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
        /*text = "precision mediump float;\n" +
                "void main() {\n" +
                "gl_FragColor = vec4(1,0,0,1);\n" +
                "}\n";*/
        int fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER,text);

        //create programs
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program,vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);


        GLES20.glUseProgram(program);
        mUniformPosLoc = GLES20.glGetAttribLocation(program, "pos");

    }
    public void selectEffect(String effect) {

    }

    private int mBackgroundTexture;
    private int [] mShaderPrograms;
    private FloatBuffer mVertices;
    private int mUniformPosLoc;
}
