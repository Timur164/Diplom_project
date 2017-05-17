package com.timyr.opencv_demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.timyr.opencv_demo.adapters.itemAdapter;
import com.timyr.opencv_demo.controller.Detector;
import com.timyr.opencv_demo.controller.Sign;
import com.timyr.opencv_demo.controller.Utilities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.FpsMeter;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private CameraBridgeViewBase mCameraView;
    private ListView listDetectedSigns;
    private RelativeLayout listRelativeLayout;
    private CascadeClassifier cascadeClassifier;
    private ArrayList<Sign> listSign;
    private Detector detector;
    private Detector detector_signs;

    private Mat mRgba;
    private Mat mGray;

    private int flagSigns = 1;
    private float mRelativeFaceSize = 0.1f;
    private int mAbsoluteFaceSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_preview);
        Initialze();
    }

    private void Initialze() {
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.mCameraView);
        listDetectedSigns = (ListView) findViewById(R.id.listView1);
        listRelativeLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        Button buttonDate = (Button) findViewById(R.id.buttonDate);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, RegconitionActivity.class);
                intent.putParcelableArrayListExtra("key", listSign);
                startActivity(intent);
            }
        });
        mCameraView.setCvCameraViewListener(this);
        listRelativeLayout.setVisibility(View.GONE);
        mCameraView.setMaxFrameSize(1280, 720);
        listSign = new ArrayList<Sign>();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this,
                mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mCameraView.disableView();
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    detector = new Detector(CameraActivity.this);
                    detector.loadCascadeFile(1);
                    detector_signs = new Detector(CameraActivity.this);
                    detector_signs.loadCascadeFile(2);
                    mCameraView.enableFpsMeter();
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Imgproc.equalizeHist(mGray, mGray);//Выравнивает гистограмму изображения в градациях серого.
        Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 0);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }


        MatOfRect signs = new MatOfRect();
//        MatOfRect signs2 = new MatOfRect();

        detector.Detect(mGray, signs, flagSigns, mAbsoluteFaceSize);
        Rect[] prohibitionArray = signs.toArray();
        Draw(prohibitionArray);

//        detector_signs.Detect(mGray, signs2,2,mAbsoluteFaceSize);
//        Rect[] dangerArray = signs2.toArray();
//        Draw(dangerArray);

        return mRgba;
    }

    public void Draw(Rect[] facesArray) {
        if (facesArray.length <= 0) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    listRelativeLayout.setVisibility(View.GONE);
                }
            });

        }
        for (int i = 0; i < facesArray.length; i++) {
            final int ii = i;
            Mat subMat = new Mat();
            subMat = mRgba.submat(facesArray[i]);

            if (flagSigns == 1) {
                Sign.myMap.put("Запрещающий знак " + i, Utilities.convertMatToBitmap(subMat));
            } else {
                Sign.myMap.put("Предупреждающий знак " + i, Utilities.convertMatToBitmap(subMat));
            }

            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 2);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Sign sign;
                    if (flagSigns == 1) {
                        sign = new Sign("unknown", "Запрещающий знак " + ii);
                    } else {
                        sign = new Sign("unknown", "Предупреждающий знак " + ii);
                    }
                    listSign.add(sign);
//                    listRelativeLayout.setVisibility(View.VISIBLE);
//                    itemAdapter adapter= new itemAdapter(listSign, CameraActivity.this);
//                    adapter.notifyDataSetChanged();
//                    listDetectedSigns.setAdapter(adapter);
                }
            });

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prohibitory_signs:
                flagSigns = 1;
                break;
            case R.id.biennguyhiem:
                flagSigns = 2;
                break;
            default:
                flagSigns = 1;
                break;
        }
        return true;
    }
}
