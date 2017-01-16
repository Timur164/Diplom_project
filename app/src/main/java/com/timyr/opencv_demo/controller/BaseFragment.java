package com.timyr.opencv_demo.controller;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.timyr.opencv_demo.R;


public class BaseFragment extends Fragment {
    private MaterialDialog dialog;

    public void toast(String message) {
        try {
            if (message == null) {
                message = "null";
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
        }
    }

    public void showProgressDialog(int title, int content) {
        try {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
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
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
    }

    //show fragement
    public void showFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    //show fragment with adding to back stack
    public void showFragmentWithBackStack(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }
}
