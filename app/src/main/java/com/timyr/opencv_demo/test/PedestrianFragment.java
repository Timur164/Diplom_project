package com.timyr.opencv_demo.test;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PedestrianFragment extends BaseFragment implements View.OnClickListener {

    private ImageView imageView;
    public static final int REQUEST_CODE_GALLERY = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pedestrian_fragment, container, false);
        imageView = (ImageView) view.findViewById(R.id.roadImageView);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                startActivityForResult(gallery, REQUEST_CODE_GALLERY);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GALLERY:
                    Uri selectedImageUri = data.getData();

//                    imageView.setImageBitmap(selectedImageUri);
                    try {
                        InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        Bitmap bitmapTest = peopleDetect(selectedImage);
                        imageView.setImageBitmap(bitmapTest);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e("my", "error set image: " + e.getMessage());
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
                    Log.i("my", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public Bitmap peopleDetect(Bitmap bitmaps) {
        Bitmap bitmap = null;
        float execTime;
        try {
            // Закачиваем фотографию

//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            InputStream imageStream = getActivity().getContentResolver().openInputStream(url);
            bitmap = bitmaps;
            long time = System.currentTimeMillis();
            // Создаем матрицу изображения для OpenCV и помещаем в нее нашу фотографию
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            // Переконвертируем матрицу с RGB на градацию серого
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 4);
            HOGDescriptor hog = new HOGDescriptor();
            //Получаем стандартный определитель людей и устанавливаем его нашему дескриптору
            MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
            hog.setSVMDetector(descriptors);
            // Определяем переменные, в которые будут помещены результаты поиска ( locations - прямоугольные области, weights - вес (можно сказать релевантность) соответствующей локации)
            MatOfRect locations = new MatOfRect();
            MatOfDouble weights = new MatOfDouble();
            // Собственно говоря, сам анализ фотографий. Результаты запишутся в locations и weights
            hog.detectMultiScale(mat, locations, weights);
            execTime = ((float) (System.currentTimeMillis() - time)) / 1000f;
            //Переменные для выделения областей на фотографии
            Point rectPoint1 = new Point();
            Point rectPoint2 = new Point();
            Scalar fontColor = new Scalar(0, 0, 0);
            Point fontPoint = new Point();
            // Если есть результат - добавляем на фотографию области и вес каждой из них
            if (locations.rows() > 0) {
                List<Rect> rectangles = locations.toList();
                int i = 0;
                List<Double> weightList = weights.toList();
                for (Rect rect : rectangles) {
                    float weigh = weightList.get(i++).floatValue();

                    rectPoint1.x = rect.x;
                    rectPoint1.y = rect.y;
                    fontPoint.x = rect.x;
                    fontPoint.y = rect.y - 4;
                    rectPoint2.x = rect.x + rect.width;
                    rectPoint2.y = rect.y + rect.height;
                    final Scalar rectColor = new Scalar(0, 0, 0);
                    // Добавляем на изображения найденную информацию
                    Imgproc.rectangle(mat, rectPoint1, rectPoint2, rectColor, 2);
                    Imgproc.putText(mat,
                            String.format("%1.2f", weigh),
                            fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
                            2, Core.LINE_AA, false);

                }
            }
            fontPoint.x = 15;
            fontPoint.y = bitmap.getHeight() - 20;
            // Добавляем дополнительную отладочную информацию
            Imgproc.putText(mat,
                    "Processing time:" + execTime + " width:" + bitmap.getWidth() + " height:" + bitmap.getHeight() ,
                    fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
                    2, Core.LINE_AA, false);
            Utils.matToBitmap(mat, bitmap);
            URL url = new URL("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
