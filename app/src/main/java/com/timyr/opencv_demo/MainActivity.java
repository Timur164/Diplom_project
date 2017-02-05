package com.timyr.opencv_demo;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.timyr.opencv_demo.controller.BaseActivity;
import com.timyr.opencv_demo.fragments.SplashFragment;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends BaseActivity {
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (!checkPermissionsLocation()) {
            requestPermissions();
        }
        progressDialog = new Dialog(this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_progress_bar, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.colorBorderGrey);
        progressDialog.setContentView(view);
        SplashFragment homeFragment = new SplashFragment();
        showFragment(homeFragment);
    }

    //Проверка на подключенность библиотеки.
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
                        Manifest.permission.CAMERA}, 11);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Метод для отображения прогресс диалога.
    public void showProgressDialog(boolean check) {
        if (check) {
            progressDialog.show();
        } else {
            progressDialog.hide();
        }
    }
}
