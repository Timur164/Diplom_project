package com.timyr.opencv_demo;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;

import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.timyr.opencv_demo.activity.CameraActivity;
import com.timyr.opencv_demo.fragments.ContactFragment;
import com.timyr.opencv_demo.fragments.PhotoFragment;
import com.timyr.opencv_demo.activity.SplashActivity;
import com.timyr.opencv_demo.fragments.SettingsFragment;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends ActionBarActivity implements Drawer.OnDrawerItemClickListener {
    private Dialog progressDialog;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (!checkPermissionsLocation()) {
            requestPermissions();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new PhotoFragment(), "PhotoFragment").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.splash_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_image).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_free_play).withIcon(FontAwesome.Icon.faw_camera),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_bell),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withIdentifier(1)
                ).withOnDrawerItemClickListener(this)
                .build();

        progressDialog = new Dialog(this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_progress_bar, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.colorBorderGrey);
        progressDialog.setContentView(view);

        Intent cameraIntent = new Intent(this, SplashActivity.class);
        startActivity(cameraIntent);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getStringExtra("key")!=null) {
            if (data.getStringExtra("key") != null) {
                toolbar.setTitle(getResources().getString(R.string.drawer_item_settings));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SettingsFragment settingsFragment = new SettingsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "key");
                        settingsFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    }
                }, 100);
            }
        } else {
            PhotoFragment demoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentByTag("PhotoFragment");
            demoFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
        switch (position) {
            case 1:
                toolbar.setTitle(getResources().getString(R.string.photoGallery));
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new PhotoFragment(), "PhotoFragment").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                break;
            case 2:
//               toolbar.setTitle(getResources().getString(R.string.camera));
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(cameraIntent, 200);
                break;
            case 3:
                toolbar.setTitle(getResources().getString(R.string.drawer_item_settings));
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                break;
            case 5:
                toolbar.setTitle(getResources().getString(R.string.drawer_item_contact));
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ContactFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                break;
        }
    }
}
