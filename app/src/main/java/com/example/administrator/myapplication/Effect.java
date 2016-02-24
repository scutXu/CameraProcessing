package com.example.administrator.myapplication;

import android.opengl.GLES20;

import java.util.Map;

/**
 * Created by Administrator on 2016/2/24.
 */
public class Effect {
    private int mGLProgram;
    private int mVertexShader;
    private int mFragmentShader;
    private Map<String,Float> mFloatUniforms;
    private Map<String,Integer> mIntUniforms;

    public void setCustomUniforms() {
    }
    public void createEffectByText(String text) {
        EffectManager effectManager = EffectManager.getInstance();
        String [] lines = text.split("\n");
        for(int i=0;i<lines.length;++i) {
            String [] words = lines[i].split(" ");
            if(words.length < 2) {
                return;
            }
            switch (words[0]) {
                case "vs":
                    if(effectManager.hasVertexShader(words[1])) {
                        mVertexShader = effectManager.getVertexShader(words[1]);
                    }
                    else {
                        mVertexShader = effectManager.createShader(GLES20.GL_VERTEX_SHADER,words[1]);
                    }
                    break;
                case "fs":
                    if(effectManager.hasFragmentShader(words[1])) {
                        mFragmentShader = effectManager.getFragmentShader(words[1]);
                    }
                    else {
                        mFragmentShader = effectManager.createShader(GLES20.GL_FRAGMENT_SHADER,words[1]);
                    }
                    break;
                case "float":
                    break;
                case "int":
                    break;
            }
        }
        mGLProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGLProgram,mVertexShader);
        GLES20.glAttachShader(mGLProgram, mFragmentShader);
        GLES20.glLinkProgram(mGLProgram);
    }
    public int getProgram() {
        return mGLProgram;
    }
}
