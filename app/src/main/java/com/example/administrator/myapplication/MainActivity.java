package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(null == savedInstanceState) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container,new CameraPreviewFragment());
            transaction.commit();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    public static String appTag = "myTag";
}
