package com.example.administrator.myapplication;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageDisplayFragment extends Fragment {


    public ImageDisplayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageBitmap(mBitmap);
        return imageView;
    }


    public void setDisplayBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private Bitmap mBitmap;
}
