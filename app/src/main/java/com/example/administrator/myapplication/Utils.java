package com.example.administrator.myapplication;

import android.media.Image;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Administrator on 2016/1/16.
 */
public class Utils {
    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "gbk");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static int getUnsignedByte(byte num) {
        return num & 0x0FF;
    }

    public static void yuv420888ToArgb888(Image yuvImage,IntBuffer argbBuffer) {
        Image.Plane yPlane = yuvImage.getPlanes()[0];
        Image.Plane uPlane = yuvImage.getPlanes()[1];
        Image.Plane vPlane = yuvImage.getPlanes()[2];


        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();

        int yRowStride = yPlane.getRowStride();
        int uvRowStride = uPlane.getRowStride();

        int y , u , v;
        int [] rgb = new int[3];
        int count = 0;
        int [] buf = argbBuffer.array();


        for(int i=0;i<yuvImage.getHeight();++i) {
            for(int j=0;j<yuvImage.getWidth();++j) {
                buf[count] = 0;
                y = getUnsignedByte(yBuffer.get(yRowStride * i + j));
                u = getUnsignedByte(uBuffer.get(uvRowStride * (i>>1) + (j>>1)));
                v = getUnsignedByte(vBuffer.get(uvRowStride * (i>>1) + (j>>1)));
                rgb[0] = (int)(1.164 * (y - 16)  + (1.596 * (v - 128)));
                rgb[1] = (int)(1.164 * (y - 16) - (0.813 * (v - 128)) - (0.392 * (v - 128)));
                rgb[2] = (int)(1.164 * (y - 16) + (2.017 * (u - 128)));
                /*rgb[0] = y;
                rgb[1] = y;
                rgb[2] = y;*/
                for(int k=0;k<3;++k) {
                    rgb[k] = rgb[k] < 0 ? 0 :(rgb[k] > 255 ? 255 : rgb[k]);
                    int value = rgb[k] << (k * 8);
                    buf[count] = buf[count] | value;
                }
                buf[count] = buf[count] | (0x0FF << 24);
                ++count;
            }
        }
    }
}
