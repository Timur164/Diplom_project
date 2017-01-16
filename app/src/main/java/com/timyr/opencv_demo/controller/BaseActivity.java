package com.timyr.opencv_demo.controller;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.timyr.opencv_demo.R;

public class BaseActivity extends AppCompatActivity {

    private MaterialDialog dialog;

    //show Toast
    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //show fragement
    public void showFragment(Fragment fragment) {
        try {
            if (null != fragment) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            }
        } catch (Exception e) {
        }
    }

    //show fragment with adding to back stack
    public void showFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    public void showProgressDialog(int title, int content) {
        try {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(BaseActivity.this)
                    .title(title)
                    .content(content)
                    .progress(true, 0)
                    .cancelable(false)
                    .progressIndeterminateStyle(false);
            dialog = builder.build();
            dialog.show();
            Handler handler = null;
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    hideProgressDialog();
                }
            }, 10000);
        } catch (Exception e) {
            hideProgressDialog();
        }
    }

    public void hideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }


}
