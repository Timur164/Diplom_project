package com.timyr.opencv_demo.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.controller.BaseFragment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;

public class RoadSignFragment extends BaseFragment implements View.OnClickListener {
    private VideoView videoPlayer;
    private final int REQUEST_TAKE_GALLERY_VIDEO = 11;
    private final int REQUEST_CODE_CAMERA = 200;
    private LinearLayout linearChoice, linearControl;
    private CameraBridgeViewBase cameraView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.road_sign_fragment, container, false);
        //Не дает затухать экрану.
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        videoPlayer = (VideoView) view.findViewById(R.id.videoPlayer);
        linearChoice = (LinearLayout) view.findViewById(R.id.linearChoice);
        linearControl = (LinearLayout) view.findViewById(R.id.linearControl);
        Button btnGalery = (Button) view.findViewById(R.id.btnGalery);
        Button btnCamera = (Button) view.findViewById(R.id.btnCamera);
        Button btnStart = (Button) view.findViewById(R.id.btnStart);
        Button btnPause = (Button) view.findViewById(R.id.btnPause);
        Button btnStop = (Button) view.findViewById(R.id.btnStop);
        btnGalery.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        cameraView = (CameraBridgeViewBase) view.findViewById(R.id.HelloOpenCvView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.e("my", "width: " + width);
                Log.e("my", "height: " + height);
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(Mat inputFrame) {
                Mat image = inputFrame.clone();

                return task_8_Template(image);
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onResume() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getActivity(), mLoaderCallback);
        super.onResume();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("my", "OpenCV loaded successfully");
                    cameraView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            //TODO Касания.
                            return false;
                        }
                    });
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnGalery:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
                videoPlayer.setVisibility(View.VISIBLE);
                cameraView.setVisibility(View.GONE);

                break;
            case R.id.btnCamera:
//                Intent camera = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                startActivityForResult(camera, REQUEST_CODE_CAMERA);
                cameraView.enableView();
                videoPlayer.setVisibility(View.GONE);
                cameraView.setVisibility(View.VISIBLE);
                break;
            case R.id.btnStart:
                videoPlayer.start();
                break;
            case R.id.btnPause:
                videoPlayer.pause();
                break;
            case R.id.btnStop:
                videoPlayer.stopPlayback();
                checkButtons(false);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_GALLERY_VIDEO:
                    Uri selectedImageUri = data.getData();
                    videoPlayer.setVideoURI(selectedImageUri);
                    videoPlayer.start();
                    checkButtons(true);
                    break;
                //ZTE камера устройства.
//                case REQUEST_CODE_CAMERA:
//                    Uri selectedVideoUri = data.getData();
//                    videoPlayer.setVideoURI(selectedVideoUri);
//                    videoPlayer.start();
////                    checkButtons(true);
//                    break;
            }
        }
    }

    private void checkButtons(boolean check) {
        if (check) {
            linearChoice.setVisibility(View.GONE);
            linearControl.setVisibility(View.VISIBLE);
        } else {
            linearChoice.setVisibility(View.VISIBLE);
            linearControl.setVisibility(View.GONE);
        }
    }

    public Mat findTemplate(Mat mat) {
        Bitmap templateBmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.peshehod);
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

        return mat;
    }

    public Mat task_8_Template(Mat mMat) {

        Bitmap bmp_exp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.peshehod);

        Mat template = new Mat();
        Utils.bitmapToMat(bmp_exp, template);

        Mat original = new Mat(mMat.size(), CvType.CV_8UC1);
        Mat exp3 = new Mat(template.size(), CvType.CV_8UC1);

        Imgproc.Canny(mMat, original, 50, 200);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(original, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        int minContours = 500;
        int minHeight = 50;
        int maxHeight = 150;
        List<MatOfPoint> contoursMain = new ArrayList<MatOfPoint>();

        for (int i = 0; i < contours.size(); i++) {
            System.out.println(Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > minContours) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > minHeight) {
                    contoursMain.add(contours.get(i));
//                    Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }

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
//                    Imgproc.rectangle(template, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                }
            }
        }

        int k = 0;
        double matchM = 1000;
        List<MatOfPoint> seqM = new ArrayList<MatOfPoint>();
        for (int i = 0; i < contoursMain.size(); i++) {
            for (int j = 0; j < contours_template.size(); j++) {
                double match0 = Imgproc.matchShapes(contoursMain.get(i), contours_template.get(j), Imgproc.CV_CONTOURS_MATCH_I1, 0);
                if (match0 < matchM) {
                    if (seqM.size() >= 1) {
                        seqM.clear();
                    }
                    matchM = match0;
                    seqM.add(contoursMain.get(i));
                }
            }
        }

        for (int i = 0; i < seqM.size(); i++) {
            Rect rect = Imgproc.boundingRect(seqM.get(i));
            if (rect.height > minHeight && rect.height < maxHeight) {
                k++;
//                Imgproc.drawContours(mMat, seqM, i, new Scalar(36, 201, 197), -1);
                Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
            }
            Log.e("my", "koef: " + k);
        }
        return mMat;
    }

    public Mat test_9_detector(Mat mMat) {

        Bitmap bmp_exp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.peshehod);

        Mat template = new Mat();
        Utils.bitmapToMat(bmp_exp, template);

        Mat imageSceneGray = mMat.clone();
        Mat imageObjectGray = template.clone();
        //        Mat imageSceneGray = new Mat(mMat.size(), CvType.CV_8UC1);
//        Mat imageObjectGray = new Mat(template.size(), CvType.CV_8UC1);
//        Imgproc.cvtColor(mMat, imageSceneGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(template, imageObjectGray, Imgproc.COLOR_BGR2GRAY);

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
            double max_dist = 0;
            double min_dist = 100;
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
        return mMat;
    }
}
