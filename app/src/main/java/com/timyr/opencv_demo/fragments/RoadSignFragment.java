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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
        cameraView = (JavaCameraView) view.findViewById(R.id.HelloOpenCvView);
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
                Mat image = inputFrame.diag();

                Mat ret_mat = new Mat();
                Core.add(image, new Scalar(0, 0, 0, 0), ret_mat); //change brightness of video frame

                return inputFrame;
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


    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return peopleDetect(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
            }
        }
    }

    public Bitmap peopleDetect(String path) {
        Bitmap bitmap = null;
        float execTime;
        try {
            // Закачиваем фотографию
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, opts);
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
            // Определяем переменные, в которые будут помещены результаты поиска ( locations - прямоуголные области, weights - вес (можно сказать релевантность) соответствующей локации)
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
//                    Core.ret(mat, rectPoint1, rectPoint2, rectColor, 2);
//                    Core.tex(mat,
//                            String.format("%1.2f", weigh),
//                            fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
//                            2, Core.LINE_AA, false);

                }
            }
            fontPoint.x = 15;
            fontPoint.y = bitmap.getHeight() - 20;
            // Добавляем дополнительную отладочную информацию
//            Core.putText(mat,
//                    "Processing time:" + execTime + " width:" + bitmap.getWidth() + " height:" + bitmap.getHeight() ,
//                    fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
//                    2, Core.LINE_AA, false);
            Utils.matToBitmap(mat, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
