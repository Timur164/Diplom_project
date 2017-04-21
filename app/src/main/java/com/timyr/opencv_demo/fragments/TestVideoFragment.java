package com.timyr.opencv_demo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

/**
 * Created by Timur on 15.04.2017.
 */

public class TestVideoFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_video_fragment, container, false);


        return view;
    }
}
