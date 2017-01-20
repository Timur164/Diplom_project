package com.timyr.opencv_demo.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

import java.io.File;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        Button roadSignButton = (Button) view.findViewById(R.id.road_sign_button);
        Button roadSignImageButton = (Button) view.findViewById(R.id.road_sign_image_button);
        Button pedestrianImageButton = (Button) view.findViewById(R.id.pedestrian_image_button);
        roadSignButton.setOnClickListener(this);
        roadSignImageButton.setOnClickListener(this);
        pedestrianImageButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.road_sign_button:
//                openApp(getActivity(),"com.timyr.p0083_menu261566_test");
                RoadSignFragment roadSignFragment = new RoadSignFragment();
                showFragmentWithBackStack(roadSignFragment);
                break;
            case R.id.road_sign_image_button:
                RoadSignImageFragment roadSignImageFragment = new RoadSignImageFragment();
                showFragmentWithBackStack(roadSignImageFragment);
                break;
            case R.id.pedestrian_image_button:
                PedestrianFragment pedestrianFragment = new PedestrianFragment();
                showFragmentWithBackStack(pedestrianFragment);
                break;
        }
    }

    //ZTE открытие новго приложения.
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            File sdCard = Environment.getExternalStorageDirectory();
            String fileStr = sdCard.getAbsolutePath() + "/Download";// + "ixat-release.apk";
            File file = new File(fileStr, "ixat-release.apk");
            Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");

            context.startActivity(promptInstall);
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }
}
