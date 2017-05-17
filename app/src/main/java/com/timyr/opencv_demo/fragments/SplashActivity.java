package com.timyr.opencv_demo.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseActivity;
import com.timyr.opencv_demo.controller.BaseFragment;

public class SplashActivity extends BaseActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_fragment);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HomeFragment homeFragment = new HomeFragment();
                showFragment(homeFragment);
            }
        },2000);
    }
}
