package com.timyr.opencv_demo.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.activity.RegconitionActivity;
import com.timyr.opencv_demo.controller.BaseFragment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PhotoFragment extends BaseFragment implements View.OnClickListener {
    private ImageView ivDisplay;
    private RelativeLayout layoutResult;
    TextView textNoDetect;
    private Button btDetect;
    private Uri mUri;
    private Mat photoMat;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private Detector detector;
    public static int pickCode = 1;
    public static int captureCode = 2;

    private boolean checkDetect = true;
    private ArrayList<Sign> listSign;
    private int sizeImage = 1500;
    private Uri uri;
    private boolean checkCamera = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_layout, container, false);
        Button btPhoto = (Button) view.findViewById(R.id.btPhoto);
        Button btCapture = (Button) view.findViewById(R.id.btCapture);
        btDetect = (Button) view.findViewById(R.id.btDetect);
        ivDisplay = (ImageView) view.findViewById(R.id.ivDisplay);
        layoutResult = (RelativeLayout) view.findViewById(R.id.layoutResult);
        textNoDetect = (TextView) view.findViewById(R.id.textNoDetect);
//        btDetect.setVisibility(View.GONE);
        btPhoto.setOnClickListener(this);
        btCapture.setOnClickListener(this);
        btDetect.setOnClickListener(this);
        btDetect.setBackgroundResource(android.R.drawable.btn_default);
        btPhoto.setBackgroundResource(android.R.drawable.btn_default);
        btCapture.setBackgroundResource(android.R.drawable.btn_default);
        return view;
    }

    @Override
    public void onResume() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, getActivity(), mLoaderCallback);
        super.onResume();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    photoMat = new Mat();
                    detector = new Detector(getActivity());
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btPhoto:
                layoutResult.removeAllViews();
                uri = null;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                checkCamera = false;
                startActivityForResult(intent, pickCode);
                break;
            case R.id.btCapture:
                layoutResult.removeAllViews();
                Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                uri = generateFileUri();
                checkCamera = true;
                intentCapture.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intentCapture, captureCode);
                break;
            case R.id.btDetect:
                if (checkDetect) {
                    checkDetect = false;
                    if (mUri == null) {
                        toast("Вы не выбрали фотографию");
                        break;
                    }
                    InputStream imageStream = null;
                    try {
                        imageStream = getActivity().getContentResolver().openInputStream(mUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    Bitmap bmp = BitmapFactory.decodeStream(imageStream, null, opts);
                    bmp = getResizedBitmap(bmp, sizeImage);
                    Utils.bitmapToMat(bmp, photoMat);
                    Mat mGray = new Mat();
                    mGray = photoMat.clone();
                    Imgproc.cvtColor(photoMat, mGray, Imgproc.COLOR_BGR2GRAY);
                    loadCascadeFile(2);
                    Detect(mGray);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mUri = data.getData();
            InputStream imageStream = null;
            try {
                textNoDetect.setText(getString(R.string.photo_detect));

                imageStream = getActivity().getContentResolver().openInputStream(mUri);

                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                selectedImage = getResizedBitmap(selectedImage, sizeImage);
                ivDisplay.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (uri != null) {
            mUri = uri;
            InputStream imageStream = null;
            try {
                textNoDetect.setText(getString(R.string.photo_detect));

                imageStream = getActivity().getContentResolver().openInputStream(mUri);

                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                selectedImage = getResizedBitmap(selectedImage, sizeImage);
                ivDisplay.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void Detect(Mat mGray) {
        Imgproc.equalizeHist(mGray, mGray);
        MatOfRect signs = new MatOfRect();
        layoutResult.removeAllViews();
        listSign = new ArrayList<Sign>();

        detector.Detect(mGray, signs, 1, 0);
        Rect[] prohibitionArray = signs.toArray();
        Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        Draw(prohibitionArray, 1);

        detector.Detect(mGray, signs, 2, 0);
        Rect[] dangerArray = signs.toArray();
        Draw(dangerArray, 2);

        //get signs from photo
        ivDisplay.setImageBitmap(Utilities.convertMatToBitmap(photoMat));
        checkDetect = true;
        btDetect.setBackgroundResource(android.R.drawable.btn_default);
    }

    public void Draw(Rect[] signsArray, int type) {
        if (signsArray.length <= 0) {
            textNoDetect.setText(getString(R.string.sign_no_detect));
            textNoDetect.setVisibility(View.VISIBLE);
        } else {
            textNoDetect.setVisibility(View.GONE);
        }

        for (int i = 0; i < signsArray.length; i++) {
            Mat subMat = new Mat();
            subMat = photoMat.submat(signsArray[i]);

            ImageView ivv = new ImageView(getActivity());
            ivv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ivv.setImageBitmap(Utilities.convertMatToBitmap(subMat));
            Sign sign;
            if (type == 1) {
                Sign.myMap.put("Запрещающие знаки" + i, Utilities.convertMatToBitmap(subMat));
                sign = new Sign("unknown", "Запрещающие знаки" + i);
            } else {
                Sign.myMap.put("Предупреждающие знаки" + i, Utilities.convertMatToBitmap(subMat));
                sign = new Sign("unknown", "Предупреждающие знаки" + i);
            }
            textNoDetect.setVisibility(View.GONE);
            listSign.add(sign);
            layoutResult.addView(ivv);
            layoutResult.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (mUri == null) {
                        toast("Знаки не найдены");
                        return false;
                    }
                    Intent intent = new Intent(getActivity(), RegconitionActivity.class);
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

    public void loadCascadeFile(int detectTypeId) {
        try {
            InputStream is = null;
            File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
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

    // Запись в файл фотки сделанной на камеру
    private Uri generateFileUri() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;
        File path = new File(Environment.getExternalStorageDirectory(), "CameraTest");
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(newFile);
    }
}
