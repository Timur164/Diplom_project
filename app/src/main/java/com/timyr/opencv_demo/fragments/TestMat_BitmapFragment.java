package com.timyr.opencv_demo.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CV_CONTOURS_MATCH_I2;


public class TestMat_BitmapFragment extends BaseFragment {
    private ImageView test_image_2, test_image_1, test_image_3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_2, container, false);
        test_image_1 = (ImageView) view.findViewById(R.id.test_image_1);
        test_image_2 = (ImageView) view.findViewById(R.id.test_image_2);
        test_image_3 = (ImageView) view.findViewById(R.id.test_image_3);
//        task_1_MATexp(); //Создание картинки с помощью MAT
//        task_2_ImageInfo(); // Подгрузка картинка и инфа о ней
//        task_3_PorogPreobrazovanie();  //Пороговое преобразование
//        task_4_Canny();   //детектор границ Кенни (Canny)
//        task_5_Hough_Circle(); //Поиск кругов
//        task_6_Find_Contours(); //Нахождение контуров и операции с ними
//        tasl_7_captureContours();    //Нахождение контуров и выделение их в прямоугольник
        task_8_Template();

        return view;
    }

    private void task_1_MATexp() {
        // задаём высоту и ширину картинки
        int height = 620;
        int width = 440;
        // задаём точку для вывода текста
        Point point = new Point(height / 2, width / 2);
        // Создаёи  картинку
        Mat m = Mat.zeros(new Size(height, width), CvType.CV_8UC3);
        Imgproc.putText(m, "hi there ;)", point, Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);
        test_image_2.setImageBitmap(convertMatToBitmap(m));
    }

    private void task_2_ImageInfo() {
        try {
            //Imgcodecs.CV_LOAD_IMAGE_UNCHANGED -- цвет картинки
            Mat mMat = Utils.loadResource(getActivity(), R.drawable.ttrip, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            Mat cloneMat = mMat.clone();//клонирование
            Log.e("info image", "width: " + cloneMat.width());
            Log.e("info image", " height:" + cloneMat.height());
            Log.e("info image", "глубина depth: " + cloneMat.depth());
            Log.e("info image", "size: " + cloneMat.size());
            Log.e("info image", "channels: " + cloneMat.channels());

            test_image_1.setImageBitmap(convertMatToBitmap(cloneMat));               //изоражение

            test_image_2.setImageBitmap(convertMatToBitmap(cropImage(cloneMat)));   //обрезка изображения

            test_image_3.setImageBitmap(convertMatToBitmap(blur(cloneMat, 2)));    //сглаживает картинку
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Пороговое преобразование
    private void task_3_PorogPreobrazovanie() {
        //Imgcodecs.CV_LOAD_IMAGE_UNCHANGED -- цвет картинки
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ttrip);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Mat mMat = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, mMat);
        Mat crat = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, crat);

        if (!mMat.empty())
            Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);
        if (!crat.empty())
            Imgproc.cvtColor(crat, crat, Imgproc.COLOR_BGR2GRAY);
//        Mat cloneMat_1 = mMat.clone();//клонирование
//        cloneMat_2.convertTo(cloneMat_2,CV_8U);

        //выполняет фиксированное пороговое преобразование для элементов массива.
        Imgproc.threshold(crat, mMat, 0, 255, Imgproc.THRESH_BINARY);
        //выполняет адаптивное пороговое преобразование для элементов массива.
        Imgproc.adaptiveThreshold(crat, mMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 8);

        test_image_1.setImageBitmap(convertMatToBitmap(crat));               //изоражение
        test_image_2.setImageBitmap(convertMatToBitmap(mMat));               //изоражение
    }

    // детектор границ Кенни (Canny)
    public void task_4_Canny() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ttrip);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Mat mMat = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Mat im_canny = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, mMat);
        Utils.bitmapToMat(bmp, im_canny);

        if (!mMat.empty())
            Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);

        int threshold1 = 10;
        int threshold2 = 1000;

        Imgproc.Canny(mMat, im_canny, threshold1, threshold2, 5, false);

        test_image_1.setImageBitmap(convertMatToBitmap(mMat));
        test_image_2.setImageBitmap(convertMatToBitmap(im_canny));
    }


    public void task_5_Hough_Circle() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ttrip);
        test_image_1.setImageBitmap(bmp);               //изоражение

        // Объект для хранения обнаруженных объектов
        Mat object = new Mat(bmp.getWidth(),
                bmp.getHeight(), CvType.CV_8UC1);

        // Накапливающие значение.Влияет на поиск знаков
        double dp = 1.3;
        // Минимальное расстояние между центром координаты обнаруженных кругов
        double minDist = bmp.getWidth() / 5;
        // Минимальные и Максимальные радиусы
        int minRadius = 10, maxRadius = bmp.getWidth() / 2;
        // Param1 = Градиент значение, которое используется для обработки края обнаружения
        // Param2 = Накопитель пороговое значение для Метод cv2.CV_HOUGH_GRADIENT.
        double param1 = 80, param2 = 120;

        Mat mMat = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, mMat);

        if (!mMat.empty())
            Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(mMat, mMat, new Size(9, 9), 2, 2);

        test_image_1.setImageBitmap(convertMatToBitmap(mMat));

//        Imgproc.HoughCircles(mMat, object,
//                Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1,
//                param2, minRadius, maxRadius);
        Imgproc.HoughCircles(mMat, object,
                Imgproc.CV_HOUGH_GRADIENT, dp, minDist);

        int numberOfObjects = (object.rows() == 0) ? 0 : object.cols();

        for (int i = 0; i < numberOfObjects; i++) {
            double[] circleCoordinates = object.get(0, i);
            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
            Point center = new Point(x, y);
            int radius = (int) circleCoordinates[2];
            Imgproc.circle(mMat, center, radius, new Scalar(0, 255, 0), 4);
//            // центр очертание
//            Imgproc.rectangle(im_canny, new Point(x - 5, y - 5),
//                    new Point(x + 5, y + 5),
//                    new Scalar(0, 128, 255), -1);
        }
        test_image_2.setImageBitmap(convertMatToBitmap(mMat));
    }

    //Нахождение контуров и операции с ними
    public void task_6_Find_Contours() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ttrip);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Mat mMat = new Mat();
        Utils.bitmapToMat(bmp, mMat);
        Mat gray = new Mat();
        Imgproc.cvtColor(mMat, gray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.Canny(gray, gray, 50, 200);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        // find contours:
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(mMat, contours, contourIdx, new Scalar(0, 0, 255), 3); //Рисовка контуров
        }

        test_image_1.setImageBitmap(convertMatToBitmap(mMat));
        test_image_2.setImageBitmap(convertMatToBitmap(gray));

    }

    //Нахождение контуров и выделение их в прямоугольник
    private void tasl_7_captureContours() {

        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ttrip);
        test_image_1.setImageBitmap(bmp);

        // Consider the image for processing
        Mat mMat = new Mat();
        Utils.bitmapToMat(bmp, mMat);
        Mat imageHSV = new Mat(mMat.size(), CvType.CV_8UC1);
        Mat imageBlurr = new Mat(mMat.size(), CvType.CV_8UC1);
        Mat imageA = new Mat(mMat.size(), CvType.CV_USRTYPE1);
        Imgproc.cvtColor(mMat, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5, 5), 0);
        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        test_image_1.setImageBitmap(convertMatToBitmap(imageA));
        int minContours = 500;
//        Imgproc.drawContours(imageBlurr, contours, 1, new Scalar(0,0,255));
        for (int i = 0; i < contours.size(); i++) {
            System.out.println(Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > 28) {
                    //System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);
                    Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }
        // Draw all the contours such that they are filled in.
//        Mat contourImg = new Mat(original.size(), original.type());
//        for (int i = 0; i < contours.size(); i++) {
//            Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 255, 255), -1);
//        }
        test_image_2.setImageBitmap(convertMatToBitmap(mMat));

    }

    public void task_8_Template() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.girl);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Bitmap bmp_exp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.rot);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Mat mMat = new Mat();
        Utils.bitmapToMat(bmp, mMat);
        Mat template = new Mat();
        Utils.bitmapToMat(bmp_exp, template);

        Mat original = new Mat(mMat.size(), CvType.CV_8UC1);
        Mat exp3 = new Mat(template.size(), CvType.CV_8UC1);

        Imgproc.Canny(mMat, original, 50, 200);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(original, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        int minContours = 100;
        int minHeight=30;
        for (int i = 0; i < contours.size(); i++) {
            System.out.println(Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > minHeight) {
                    Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }
        test_image_1.setImageBitmap(convertMatToBitmap(mMat));


        Imgproc.Canny(template, exp3, 50, 200);
        List<MatOfPoint> contours_exp = new ArrayList<MatOfPoint>();
        Imgproc.findContours(exp3, contours_exp, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // Draw all the contours such that they are filled in.
        for (int i = 0; i < contours_exp.size(); i++) {
            System.out.println(Imgproc.contourArea(contours_exp.get(i)));
            if (Imgproc.contourArea(contours_exp.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours_exp.get(i));
                System.out.println(rect.height);
                if (rect.height > minHeight) {
                    Imgproc.rectangle(template, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }

        test_image_2.setImageBitmap(convertMatToBitmap(template));
        Mat result = new Mat(mMat.size(), CvType.CV_32F);
        int match_method = Imgproc.TM_SQDIFF_NORMED;

        Imgproc.matchTemplate(mMat, template, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        Imgproc.rectangle(mMat, matchLoc, new Point(matchLoc.x + template.cols(),
                matchLoc.y + template.rows()), new Scalar(255, 0, 0));

        test_image_3.setImageBitmap(convertMatToBitmap(mMat));


    }


    //ROI Обрезка (выделение) нужного фрагмента
    public Mat cropImage(Mat mat) {
        int x = 40;
        int y = 20;
        int width = 150;
        int height = 150;
        Rect roi = new Rect(x, y, width, height);
        Mat sub = mat.submat(roi);
        Imgproc.cvtColor(sub, sub, Imgproc.COLOR_RGBA2GRAY); //серый фильтр
        sub.copyTo(mat.submat(roi));
        return sub;
    }

    //Размытие
    public Mat blur(Mat input, int numberOfTimes) {
        Mat sourceImage = new Mat();
        Mat destImage = input.clone();
        for (int i = 0; i < numberOfTimes; i++) {
            sourceImage = destImage.clone();
            Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));
//            Imgproc.GaussianBlur(sourceImage, destImage, new Size(3, 3), 0);
        }
        return destImage;
    }

    //Конверт Mat в Bitmap
    private Bitmap convertMatToBitmap(Mat mat) {
        try {
            Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bmp);
            return bmp;
        } catch (Exception e) {
            Log.e("my","bugs convertMatToBitmap");
            Bitmap bmps = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.error);
            return bmps;
        }
    }
}
