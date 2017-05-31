package com.timyr.opencv_demo.test;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;


public class RoadSignImageFragment extends BaseFragment implements View.OnClickListener {

    private ImageView imageView;
    public static final int REQUEST_CODE_GALLERY = 100;
    public static final int REQUEST_CODE_CAMERA = 200;
    private Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.road_sign_image_fragment, container, false);
        //Инициальизируем данные из layout
        imageView = (ImageView) view.findViewById(R.id.roadImageView);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //Галерея
                case REQUEST_CODE_GALLERY:
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmapTest = findObject(BitmapFactory.decodeStream(imageStream, null, opts));
                            imageView.setImageBitmap(bitmapTest);
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
                            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmapTest = findObject(BitmapFactory.decodeStream(imageStream, null, opts));
                            imageView.setImageBitmap(bitmapTest);
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


    public Bitmap findObject(Bitmap bitmap) {
        //Конвертируем Bitmap в Mat
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);
        // Добавляем оттенок серого
        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY
                : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);

        Imgproc.cvtColor(mat, grayMat, colorChannels);
        // Уменьшаем шум, чтобы мы избежать ложного обнаружения
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 2, 2);
        // Накапливающие значение.Влияет на поиск знаков
        double dp = 1.2d;
        // Минимальное расстояние между центром координаты обнаруженных кругов
        double minDist = 100;
        // Минимальные и Максимальные радиусы
        int minRadius = 30, maxRadius = 1000;
        // Param1 = Градиент значение, которое используется для обработки края обнаружения
        // Param2 = Накопитель пороговое значение для Метод cv2.CV_HOUGH_GRADIENT.
        double param1 = 80, param2 = 120;
        // Объект для хранения обнаруженных объектов
        Mat object = new Mat(bitmap.getWidth(),
                bitmap.getHeight(), CvType.CV_8UC1);

        //Поиск объектов на картинке
        Imgproc.HoughCircles(grayMat, object,
                Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1,
                param2, minRadius, maxRadius);
        //Число обнаруженных объектов
        int numberOfObjects = (object.rows() == 0) ? 0 : object.cols();
        //Цикл для выделения найденных объектов
        for (int i = 0; i < numberOfObjects; i++) {
            double[] circleCoordinates = object.get(0, i);
            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
            Point center = new Point(x, y);
            int radius = (int) circleCoordinates[2];
            Imgproc.circle(mat, center, radius, new Scalar(0,
                    255, 0), 4);
            // центр очертание
            Imgproc.rectangle(mat, new Point(x - 5, y - 5),
                    new Point(x + 5, y + 5),
                    new Scalar(0, 128, 255), -1);
        }
        Log.e("my", "numberOfCircles: " + numberOfObjects);
        //Конвертирование в Bitmap
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
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
