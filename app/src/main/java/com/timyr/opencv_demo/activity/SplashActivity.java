package com.timyr.opencv_demo.activity;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_fragment);
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
