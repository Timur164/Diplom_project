package com.timyr.opencv_demo.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.timyr.opencv_demo.R;
import com.timyr.opencv_demo.RoadSignApp;

public class Detector {
    private Activity activity;
    private CascadeClassifier cascadeClassifier;

    public Detector(Activity activity) {
        this.activity = activity;
    }

    private int flags = 3;
    int typeCheck = 0;

    //	ScaleFactor - параметр, определяющий, насколько уменьшен размер изображения на каждой шкале изображения.(1.1)
//	MinNeighbors - Параметр, указывающий, сколько соседей должно быть у каждого прямоугольника-кандидата, чтобы сохранить его.(2-3)
//	Flags - Параметр с тем же значением для старого каскада, что и в функции cvHaarDetectObjects. Он не используется для нового каскада.(0)
    //minSize – Минимально возможный размер объекта. Объекты меньшего размера игнорируются. (30,30)
    public void Detect(Mat mGray, MatOfRect signs, int type, int mAbsoluteFaceSize) {
        if (typeCheck != type) {
            loadCascadeFile(type);
            typeCheck = type;
        }
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, signs, RoadSignApp.getInstance().getScaleFactor(), RoadSignApp.getInstance().getMinNeighbors(), flags, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size(480, 480));
        }
    }

    public void loadCascadeFile(int type) {
        try {
            InputStream is = null;
            File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = null;
            switch (type) {
                case 1:
                    is = activity.getResources().openRawResource(R.raw.prohibitory_signs);
                    cascadeFile = new File(cascadeDir, "prohibitory_signs.xml");
                    break;
                case 2:
                    is = activity.getResources().openRawResource(R.raw.warning_signs);
                    cascadeFile = new File(cascadeDir, "warning_signss.xml");
                    break;
                default:
                    is = activity.getResources().openRawResource(R.raw.prohibitory_signs);
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
            cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Bitmap convertMatToBitmap(Mat src) {
        Bitmap bm = Bitmap.createBitmap(src.cols(),
                src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bm);
        //Imgproc.cvtColor(src, dst, code, dstCn)
        return bm;
    }
}
