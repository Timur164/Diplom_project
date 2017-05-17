package com.timyr.opencv_demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.timyr.opencv_demo.controller.Detector;
import com.timyr.opencv_demo.controller.Sign;
import com.timyr.opencv_demo.controller.Utilities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PhotoActivity extends Activity implements View.OnClickListener {
    private ImageView ivDisplay;
    private LinearLayout layoutResult;
    private Button btDetect;
    private Uri mUri;
    private Mat photoMat;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private Detector detector;
    public static int pickCode = 1;
    public static int captureCode = 2;
    private int REQUEST_TAKE_GALLERY_VIDEO = 101;

    private ArrayList<Sign> listSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        Button btPhoto = (Button) findViewById(R.id.btPhoto);
        Button btVideo = (Button) findViewById(R.id.btVideo);
        btDetect = (Button) findViewById(R.id.btDetect);
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        layoutResult = (LinearLayout) findViewById(R.id.layoutResult);
        btDetect.setVisibility(View.GONE);
        btPhoto.setOnClickListener(this);
        btVideo.setOnClickListener(this);
        btDetect.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    photoMat = new Mat();
                    detector = new Detector(PhotoActivity.this);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public void loadCascadeFile(int detectTypeId) {
        try {
            InputStream is = null;
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = null;

            switch (detectTypeId) {
                case 1:
                    is = getResources().openRawResource(R.raw.prohibitory_signs);
                    cascadeFile = new File(cascadeDir, "prohibitory_signs.xml");
                    break;
                case 2:
                    is = getResources().openRawResource(R.raw.warning_signs);
                    cascadeFile = new File(cascadeDir, "warning_signs.xmll");
                    break;
                default:
                    is = getResources().openRawResource(R.raw.prohibitory_signs);
                    cascadeFile = new File(cascadeDir, "prohibitory_signs.xml");
                    break;
            }

            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();


            // Load the cascade classifier
            CascadeClassifier cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Detect(Mat mGray) {
        Imgproc.equalizeHist(mGray, mGray);
        MatOfRect signs = new MatOfRect();
        listSign = new ArrayList<Sign>();

        detector.Detect(mGray, signs, 1,0);
        Rect[] prohibitionArray = signs.toArray();
        Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        //Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        Draw(prohibitionArray);

        detector.Detect(mGray, signs, 2,0);
        Rect[] dangerArray = signs.toArray();
        // Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        // Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        Draw(dangerArray);

        //get signs from photo
        ivDisplay.setImageBitmap(Utilities.convertMatToBitmap(photoMat));
    }

    public void Draw(Rect[] signsArray) {

        for (int i = 0; i < signsArray.length; i++) {
            Mat subMat = new Mat();
            subMat = photoMat.submat(signsArray[i]);

            ImageView ivv = new ImageView(this);
            ivv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ivv.setImageBitmap(Utilities.convertMatToBitmap(subMat));

            Sign.myMap.put("image" + i, Utilities.convertMatToBitmap(subMat));
            Sign sign = new Sign("unknown", "image" + i);

            listSign.add(sign);
            layoutResult.addView(ivv);
            btDetect.setVisibility(View.GONE);

            layoutResult.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(PhotoActivity.this, RegconitionActivity.class);
                    intent.putParcelableArrayListExtra("key", listSign);
                    startActivity(intent);
                    return false;
                }
            });
        }
        //draw rectangle
        for (int i = 0; i < signsArray.length; i++) {
            Imgproc.rectangle(photoMat, signsArray[i].tl(), signsArray[i].br(), FACE_RECT_COLOR, 3);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btPhoto:
                layoutResult.removeAllViews();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
            case R.id.btVideo:
                //TODO select video;
                Intent intentVideo = new Intent();
                intentVideo.setType("video/*");
                intentVideo.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentVideo, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
                break;
            case R.id.btDetect:
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(mUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BitmapFactory.Options opts = new BitmapFactory.Options();
                Bitmap bmp = BitmapFactory.decodeStream(imageStream, null, opts);
//                String photoPath = Utilities.getRealPathFromURI(mUri,PhotoActivity.this);
//                photoMat= Imgcodecs.imread(photoPath);
                Utils.bitmapToMat(bmp, photoMat);
                Mat mGray = new Mat();
                mGray = photoMat.clone();
                Imgproc.cvtColor(photoMat, mGray, Imgproc.COLOR_BGR2GRAY);
                loadCascadeFile(2);
                Detect(mGray);
//                photoPath = "";
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == pickCode && data != null) {
            mUri = data.getData();
            ivDisplay.setImageURI(mUri);
            btDetect.setVisibility(View.VISIBLE);
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                String filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                String selectedImagePath = getPath(selectedImageUri);
//                if (selectedImagePath != null) {
                    Intent intent = new Intent(PhotoActivity.this,VideoViewDemo.class);
                    intent.putExtra("path", selectedImagePath);
                    startActivity(intent);
//                }
            }
        }
    }

    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

}
