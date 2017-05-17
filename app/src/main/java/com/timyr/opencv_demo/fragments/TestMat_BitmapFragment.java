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

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
//        task_8_Template();  //сравнение контуров и сравнение изображений
        test_9_detector();

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
        int threshold2 = 100;

        Imgproc.Canny(mMat, im_canny, threshold1, threshold2, 3, false);

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
        Imgproc.drawContours(imageBlurr, contours, 1, new Scalar(0, 0, 255));
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

        Imgproc.moments(mMat);
        // Draw all the contours such that they are filled in.
//        Mat contourImg = new Mat(mMat.size(), mMat.type());
//        for (int i = 0; i < contours.size(); i++) {
//            Imgproc.drawContours(contourImg, contours, i, new Scalar(0, 0, 255), -1);
//        }
        test_image_2.setImageBitmap(convertMatToBitmap(mMat));

    }

    public void task_8_Template() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.exp_peshehod_2);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Bitmap bmp_exp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.peshehod);
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

        int minContours = 500;
        int minHeight = 100;
        List<MatOfPoint> contoursMain = new ArrayList<MatOfPoint>();

        for (int i = 0; i < contours.size(); i++) {
            System.out.println(Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > minHeight) {
                    contoursMain.add(contours.get(i));
                    Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }
        test_image_1.setImageBitmap(convertMatToBitmap(mMat));


        Imgproc.Canny(template, exp3, 50, 200);
        List<MatOfPoint> contours_exp = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contours_template = new ArrayList<MatOfPoint>();
        Imgproc.findContours(exp3, contours_exp, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // Draw all the contours such that they are filled in.
        for (int i = 0; i < contours_exp.size(); i++) {
            System.out.println(Imgproc.contourArea(contours_exp.get(i)));
            if (Imgproc.contourArea(contours_exp.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours_exp.get(i));
                System.out.println(rect.height);
                if (rect.height > minHeight) {
                    contours_template.add(contours_exp.get(i));
                    Imgproc.rectangle(template, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }
        test_image_2.setImageBitmap(convertMatToBitmap(template));
        int k = 0;
        for (int i = 0; i < contoursMain.size(); i++) {
            for (int j = 0; j < contours_template.size(); j++) {
                if (Imgproc.matchShapes(contoursMain.get(i), contours_template.get(j), Imgproc.CV_CONTOURS_MATCH_I3, 0) >= 1) {
                    k++;
                    Imgproc.drawContours(mMat, contoursMain, i, new Scalar(0, 0, 255), -1);
                }
            }
        }
        Log.e("my", "koef: " + k);

        test_image_3.setImageBitmap(convertMatToBitmap(mMat));


//        Mat result = new Mat(mMat.size(), CvType.CV_32F);
//        int match_method = Imgproc.TM_SQDIFF_NORMED;

//        Imgproc.matchTemplate(mMat, template, result, match_method);
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//
//        // Localizing the best match with minMaxLoc
//        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//
//        Point matchLoc;
//        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//        } else {
//            matchLoc = mmr.maxLoc;
//        }
//
//        Imgproc.rectangle(mMat, matchLoc, new Point(matchLoc.x + template.cols(),
//                matchLoc.y + template.rows()), new Scalar(255, 0, 0));

//        test_image_3.setImageBitmap(convertMatToBitmap(mMat));

    }

    public void test_9_detector() {
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.girl);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Bitmap bmp_exp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.rot);
        test_image_1.setImageBitmap(bmp);               //изоражение

        Mat mMat = new Mat();
        Utils.bitmapToMat(bmp, mMat);
        Mat template = new Mat();
        Utils.bitmapToMat(bmp_exp, template);

//        Mat imageSceneGray = new Mat(mMat.size(), CvType.CV_8UC1);
        Mat imageSceneGray = mMat.clone();
        Mat imageObjectGray = template.clone();
//        Mat imageObjectGray = new Mat(template.size(), CvType.CV_8UC1);
//        Imgproc.cvtColor(mMat, imageSceneGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(template, imageObjectGray, Imgproc.COLOR_BGR2GRAY);

        test_image_1.setImageBitmap(convertMatToBitmap(imageSceneGray));
        test_image_2.setImageBitmap(convertMatToBitmap(imageObjectGray));
        try {
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.BRISK);
            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

            // -- Step 1: Detect the keypoints using Detector
            MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
            MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();
            detector.detect(imageObjectGray, keypoints_object);
            detector.detect(imageSceneGray, keypoints_scene);

            // -- Step 2: Calculate descriptors (feature vectors)
            Mat descriptors_object = new Mat();
            Mat descriptors_scene = new Mat();
            extractor.compute(imageObjectGray, keypoints_object, descriptors_object);
            extractor.compute(imageSceneGray, keypoints_scene, descriptors_scene);

            // -- Step 3: Matching descriptor vectors using matcher
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors_object, descriptors_scene, matches);

            List<DMatch> matchesList = matches.toList();
            double max_dist = 5000;
            double min_dist = 50;
            // -- Quick calculation of max and min distances between keypoints
            for (int i = 0; i < descriptors_object.rows(); i++) {
                double dist = matchesList.get(i).distance;
                if (dist < min_dist) {
                    min_dist = dist;
                }
                if (dist > max_dist) {
                    max_dist = dist;
                }
            }

            // -- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
            Vector<DMatch> good_matches = new Vector<DMatch>();
            for (int i = 0; i < descriptors_object.rows(); i++) {
                if (matchesList.get(i).distance < 3 * min_dist) {
                    good_matches.add(matchesList.get(i));
                }
            }

            List<Point> objListGoodMatches = new ArrayList<Point>();
            List<Point> sceneListGoodMatches = new ArrayList<Point>();

            List<KeyPoint> keypoints_objectList = keypoints_object.toList();
            List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

            for (int i = 0; i < good_matches.size(); i++) {
                // -- Get the keypoints from the good matches
                objListGoodMatches.add(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
                sceneListGoodMatches.add(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
                Imgproc.circle(mMat, new Point(sceneListGoodMatches.get(i).x, sceneListGoodMatches.get(i).y), 3, new Scalar(255, 0, 0, 255));

            }
            String text = "Good Matches Count: " + good_matches.size();
            Imgproc.putText(mMat, text, new Point(0, 60), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(0, 0, 255, 255));

            MatOfPoint2f objListGoodMatchesMat = new MatOfPoint2f();
            objListGoodMatchesMat.fromList(objListGoodMatches);
            MatOfPoint2f sceneListGoodMatchesMat = new MatOfPoint2f();
            sceneListGoodMatchesMat.fromList(sceneListGoodMatches);

            // findHomography needs 4 corresponding points
            if (good_matches.size() > 3) {


                Mat H = Calib3d.findHomography(objListGoodMatchesMat, sceneListGoodMatchesMat, Calib3d.RANSAC, 5 /* RansacTreshold */);

                Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
                Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

                obj_corners.put(0, 0, new double[]{0, 0});
                obj_corners.put(1, 0, new double[]{imageObjectGray.cols(), 0});
                obj_corners.put(2, 0, new double[]{imageObjectGray.cols(), imageObjectGray.rows()});
                obj_corners.put(3, 0, new double[]{0, imageObjectGray.rows()});

                Core.perspectiveTransform(obj_corners, scene_corners, H);

                Imgproc.line(mMat, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(mMat, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(mMat, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(mMat, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 2);

            }
        } catch (Exception e) {
            Log.e("my", e.getMessage());
        }
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
            Log.e("my", "bugs convertMatToBitmap");
            Bitmap bmps = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.error);
            return bmps;
        }
    }
}
