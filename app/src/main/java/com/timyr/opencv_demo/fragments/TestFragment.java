package com.timyr.opencv_demo.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;
import com.timyr.opencv_demo.controller.MatchingDemo;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class TestFragment extends BaseFragment implements View.OnClickListener {
    private ImageView startImageView, resultImageView, templateImageView;
    public static final int REQUEST_CODE_GALLERY = 100;
    public static final int REQUEST_CODE_CAMERA = 200;
    private Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.road_sign_image_fragment, container, false);
        //Инициальизируем данные из layout
        startImageView = (ImageView) view.findViewById(R.id.startImageView);
        templateImageView = (ImageView) view.findViewById(R.id.templateImageView);
        resultImageView = (ImageView) view.findViewById(R.id.resultImageView);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(this);
        return view;
    }

    protected void onCreateDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.attention);
        adb.setMessage(R.string.choose_photo_loaded);
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton(R.string.galery, myClickListener);
        adb.setNegativeButton(R.string.camera, myClickListener);
        adb.setNeutralButton(R.string.cancel, myClickListener);
        adb.show();
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    Intent gallery = new Intent(Intent.ACTION_PICK);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, REQUEST_CODE_GALLERY);
                    break;
                // негативная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    uri = generateFileUri();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
            }
        }
    };

    //Слушатель нажатий
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                onCreateDialog();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Вызываем асинхронный загрузчик библиотеки
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getActivity(), mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //Галерея
                case REQUEST_CODE_GALLERY:
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                            startImageView.setImageBitmap(bmp);
                            Bitmap tempBmp=bmp;
                            for(int i=0;i<3;i++){
                                tempBmp=findTemplate(tempBmp);
                            }
                            resultImageView.setImageBitmap(tempBmp);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.e("my", "error set image: " + e.getMessage());
                        }
                    } else {
                        toast(getString(R.string.photo_not_correct));
                    }
                    break;
                //Камера
                case REQUEST_CODE_CAMERA:
                    Uri imageCameraUri = uri;
                    if (imageCameraUri != null) {
                        try {
                            InputStream imageStream = getActivity().getContentResolver().openInputStream(imageCameraUri);
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream, null, opts);
                            startImageView.setImageBitmap(bmp);
                            resultImageView.setImageBitmap(findTemplate(bmp));

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.e("my", "error set image: " + e.getMessage());
                        }
                    } else {
                        toast(getString(R.string.photo_not_correct));
                    }
                    break;
            }
        }
    }

    public Bitmap findTemplate(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);

        Bitmap templateBmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.peshehod);
        templateImageView.setImageBitmap(templateBmp);
        Mat templ = new Mat();
        Utils.bitmapToMat(templateBmp, templ);

        // / Create the result matrix
        int result_cols = mat.cols() - templ.cols() + 1;
        int result_rows = mat.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        int match_method = Imgproc.TM_SQDIFF_NORMED;

        // Do the Matching and Normalize
        Imgproc.matchTemplate(mat, templ, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        //  Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        if (matchLoc.x != 0 && matchLoc.y != 0) {
            // / Show me what you got
            Imgproc.rectangle(mat, matchLoc, new Point(matchLoc.x + templ.cols(),
                    matchLoc.y + templ.rows()), new Scalar(0, 255, 0));
        }

        return convertMatToBitmap(mat);
    }

    //Конверт Mat в Bitmap
    private Bitmap convertMatToBitmap(Mat mat) {
        try {
            Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bmp);
            return bmp;
        } catch (Exception e) {
            Log.e("my", "bugs convertMatToBitmap");
            Bitmap bmps = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.error);
            return bmps;
        }
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
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", newFile);
        return uri;
    }
}
