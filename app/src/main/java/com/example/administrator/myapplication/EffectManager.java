package com.example.administrator.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/2/24.
 */
public class EffectManager {
    private static EffectManager ourInstance = new EffectManager();

    public static EffectManager getInstance() {
        return ourInstance;
    }

    private EffectManager() {

    }
    public void setContext(Context context) {
        mContext = context;
    }
    public void readConfigFile() {
        Resources res = mContext.getResources();
        InputStream input = res.openRawResource(R.raw.config);
        String text = Utils.getString(input);
        try {
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        mEffectNameList = text.split("\n");
    }
    public void loadEffects() {
        Resources res = mContext.getResources();

        mVertexShaderMap = new HashMap<String,Integer>();
        mFragmentShaderMap = new HashMap<String,Integer>();
        mEffectMap = new HashMap<String,Effect>();


        for(int i=0;i<mEffectNameList.length;++i) {
            String effectName = mEffectNameList[i];
            if(!mEffectMap.containsKey(effectName)) {
                int effectId = res.getIdentifier(effectName,"raw",mContext.getPackageName());
                InputStream effectInput = res.openRawResource(effectId);
                String effectText = Utils.getString(effectInput);
                try {
                    effectInput.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                Effect effect = new Effect();
                effect.createEffectByText(effectText);
                mEffectMap.put(effectName,effect);
            }
        }
    }
    public int createShader(int type,String file) {
        Resources res = mContext.getResources();
        int shaderId = res.getIdentifier(file, "raw", mContext.getPackageName());
        InputStream input = res.openRawResource(shaderId);
        String text = Utils.getString(input);
        try {
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        int shader = GLES20.glCreateShader(type);
        int [] compileStatus = new int[1];
        GLES20.glShaderSource(shader, text);
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] != GLES20.GL_TRUE) {
            Log.e(MainActivity.appTag, "compile shader fail");
            String info = GLES20.glGetShaderInfoLog(shader);
            Log.e(MainActivity.appTag,text);
            Log.e(MainActivity.appTag,info);
            GLES20.glDeleteShader(shader);
        }
        if(type == GLES20.GL_VERTEX_SHADER) {
            mVertexShaderMap.put(file,shader);
        }
        else {
            mFragmentShaderMap.put(file,shader);
        }
        return  shader;
    }
    public void releaseEffects() {
    }
    public String [] getAllEffectNames() {
        return mEffectNameList;
    }
    Effect getEffect(String name) {
        return mEffectMap.get(name);
    }
    public boolean hasVertexShader(String name) {
        return mVertexShaderMap.containsKey(name);
    }

    public boolean hasFragmentShader(String name) {
        return mFragmentShaderMap.containsKey(name);
    }
    public int getVertexShader(String name) {
        return mVertexShaderMap.get(name);
    }
    public int getFragmentShader(String name) {
        return mFragmentShaderMap.get(name);
    }

    private Map<String,Integer> mVertexShaderMap;
    private Map<String,Integer> mFragmentShaderMap;
    private Map<String,Effect> mEffectMap;
    private Context mContext = null;
    private String [] mEffectNameList;
}

