package com.timyr.opencv_demo.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.SmartObjectRecognitionActivity;
import com.timyr.opencv_demo.controller.BaseFragment;
import com.timyr.opencv_demo.controller.CannyController;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        Button roadSignButton = (Button) view.findViewById(R.id.road_sign_button);
        Button roadSignImageButton = (Button) view.findViewById(R.id.road_sign_image_button);
        Button pedestrianImageButton = (Button) view.findViewById(R.id.pedestrian_image_button);
        Button manyDetectorsButton = (Button) view.findViewById(R.id.capabilities_OpenCv_button);
        Button test_btn = (Button) view.findViewById(R.id.test_btn);
        Button test_btn_2 = (Button) view.findViewById(R.id.test_btn_2);
        Button test_btn_video = (Button) view.findViewById(R.id.test_btn_video);
        roadSignButton.setOnClickListener(this);
        roadSignImageButton.setOnClickListener(this);
        pedestrianImageButton.setOnClickListener(this);
        manyDetectorsButton.setOnClickListener(this);
        test_btn.setOnClickListener(this);
        test_btn_2.setOnClickListener(this);
        test_btn_video.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.road_sign_button:
                RoadSignFragment roadSignFragment = new RoadSignFragment();
                showFragmentWithBackStack(roadSignFragment);
                break;
            case R.id.road_sign_image_button:
                RoadSignImageFragment roadSignImageFragment = new RoadSignImageFragment();
                showFragmentWithBackStack(roadSignImageFragment);
                break;
            case R.id.pedestrian_image_button:
                CannyController pedestrianFragment = new CannyController();
                showFragmentWithBackStack(pedestrianFragment);
                break;
            case R.id.capabilities_OpenCv_button:
                Intent intent = new Intent(getActivity(), SmartObjectRecognitionActivity.class);
                startActivity(intent);
                break;
            case R.id.test_btn:
                TestFragment testFragment = new TestFragment();
                showFragmentWithBackStack(testFragment);
                break;
            case R.id.test_btn_2:
                TestMat_BitmapFragment testFragment_2 = new TestMat_BitmapFragment();
                showFragmentWithBackStack(testFragment_2);
                break;
            case R.id.test_btn_video:
                TestVideoFragment testVideoFragment = new TestVideoFragment();
                showFragmentWithBackStack(testVideoFragment);
                break;
        }
    }
}
