package com.timyr.opencv_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.timyr.opencv_demo.controller.BaseActivity;
import com.timyr.opencv_demo.fragments.HomeFragment;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_FINE_LOCATION = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (!checkPermissionsLocation()) {
            requestPermissions();
        }
        HomeFragment homeFragment = new HomeFragment();
        showFragment(homeFragment);
    }

    //ZTE Проверка на подключенность библиотеки.
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("my", "OpenCV initialization failed");
        } else {
            Log.i("my", "OpenCV initialization succeeded");
        }
    }

    private boolean checkPermissionsLocation() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.CAMERA},
                REQUEST_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
