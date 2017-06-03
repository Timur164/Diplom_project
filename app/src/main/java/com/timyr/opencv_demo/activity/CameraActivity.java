package com.timyr.opencv_demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.RoadSignApp;
import com.timyr.opencv_demo.adapters.itemAdapter;
import com.timyr.opencv_demo.controller.Detector;
import com.timyr.opencv_demo.controller.Sign;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private CameraBridgeViewBase mCameraView;
    private ListView listDetectedSigns;
    private RelativeLayout listRelativeLayout;
    private ArrayList<Sign> listSign;
    private Detector detector;
    private Detector detector_signs;

    private Mat mRgba;
    private Mat mGray;

    private double mRelativeFaceSize = RoadSignApp.getInstance().getMinSize();
    private int mAbsoluteFaceSize = 0;
    private boolean checkFps = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_preview);
        Toast.makeText(this, "поверните телефон в горизонтальное положение", Toast.LENGTH_LONG).show();
        Initialize();
    }

    private void Initialize() {
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.mCameraView);
        listDetectedSigns = (ListView) findViewById(R.id.listView1);
        listRelativeLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        RelativeLayout checkfps = (RelativeLayout) findViewById(R.id.checkfps);
        Button buttonDate = (Button) findViewById(R.id.buttonDate);
        Button buttonSettings = (Button) findViewById(R.id.buttonSettings);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, RecognitionActivity.class);
                intent.putParcelableArrayListExtra("key", listSign);
                startActivity(intent);
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("key", "key");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        checkfps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFps = !checkFps;
            }
        });
        mCameraView.setCvCameraViewListener(this);
        listRelativeLayout.setVisibility(View.GONE);
        mCameraView.setMaxFrameSize(RoadSignApp.getInstance().getWidthSize(), RoadSignApp.getInstance().getHeightSize());
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
        if (checkFps) {
            mCameraView.enableFpsMeter();
        } else {
            mCameraView.disableFpsMeter();
        }

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Imgproc.equalizeHist(mGray, mGray);//Выравнивает гистограмму изображения в градациях серого.
        Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 0); //Сглаживание изображение фильтром Гаусса

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = (int) Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect signs = new MatOfRect();
        MatOfRect signs2 = new MatOfRect();

        if (RoadSignApp.getInstance().isShowProSign()) {
            detector.Detect(mGray, signs, 1, mAbsoluteFaceSize);
            Rect[] prohibitionArray = signs.toArray();
            Draw(prohibitionArray);
        }
        if (RoadSignApp.getInstance().isShowWarSign()) {
            detector_signs.Detect(mGray, signs2, 2, mAbsoluteFaceSize);
            Rect[] dangerArray = signs2.toArray();
            Draw(dangerArray);
        }

        if(RoadSignApp.getInstance().isColorFilter()){
            return mRgba;
        }else{
            return mGray;
        }
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
            if(RoadSignApp.getInstance().isColorFilter()){
                subMat = mRgba.submat(facesArray[i]);
            }else{
                subMat = mGray.submat(facesArray[i]);
            }

            if (RoadSignApp.getInstance().isShowProSign()) {
                Sign.myMap.put("Запрещающий знак " + i, Detector.convertMatToBitmap(subMat));
            } else {
                Sign.myMap.put("Предупреждающий знак " + i, Detector.convertMatToBitmap(subMat));
            }

            if(RoadSignApp.getInstance().isColorFilter()){
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 2);
            }else{
                Imgproc.rectangle(mGray, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 2);
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Sign sign;
                    if (RoadSignApp.getInstance().isShowProSign()) {
                        sign = new Sign("unknown", "Запрещающий знак " + ii);
                    } else {
                        sign = new Sign("unknown", "Предупреждающий знак " + ii);
                    }
                    listSign.add(sign);
                    if (RoadSignApp.getInstance().isShowSign()) {
                        listRelativeLayout.setVisibility(View.VISIBLE);
                        itemAdapter adapter = new itemAdapter(listSign, CameraActivity.this);
                        adapter.notifyDataSetChanged();
                        listDetectedSigns.setAdapter(adapter);
                    }
                }
            });

        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.prohibitory_signs:
//                flagSigns = 1;
//                break;
//            case R.id.biennguyhiem:
//                flagSigns = 2;
//                break;
//            default:
//                flagSigns = 1;
//                break;
//        }
//        return true;
//    }
}
