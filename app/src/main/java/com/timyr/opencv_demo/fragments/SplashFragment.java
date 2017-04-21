package com.timyr.opencv_demo.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

public class SplashFragment extends BaseFragment{


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash_fragment, container, false);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HomeFragment homeFragment = new HomeFragment();
                showFragment(homeFragment);
            }
        },2000);
        return view;
    }

//    private void loading(final View view) {
//        final TextView tvPoint = (TextView) view.findViewById(R.id.tvPoint);
//        handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                assert tvPoint != null;
//                String loading = tvPoint.getText().toString();
//                if (loading.length() < 4) {
//                    loading += ".";
//                }
//                if (loading.length() == 4) {
//                    loading = "";
//                }
//                tvPoint.setText(loading);
//                loading(view);
//            }
//        }, 500);
//    }

}
