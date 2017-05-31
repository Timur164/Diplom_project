package com.timyr.opencv_demo.test.utilites;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CannyController extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experement, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        String inputFileName = "simm_01";
        String inputExtension = "jpg";
        String inputDir = getActivity().getCacheDir().getAbsolutePath();  // use the cache directory for i/o
        String outputDir = "/Download";
        String outputExtension = "png";
//        String inputFilePath = inputDir + File.separator + inputFileName + "." + inputExtension;
        String inputFilePath = "/Download/stop.png";

        Log.e(this.getClass().getSimpleName(), "loading " + inputFilePath + "...");
        Bitmap temlateFile = BitmapFactory.decodeResource(getResources(), R.drawable.ttrip);
        imageView.setImageBitmap(temlateFile);

        Mat image = new Mat(temlateFile.getWidth(), temlateFile.getHeight(),
                CvType.CV_8UC1);
//        Mat image = Imgcodecs.imread(inputFilePath);
        Log.e(this.getClass().getSimpleName(), "width of " + inputFileName + ": " + image.width());
        // if width is 0 then it did not read your image.


        // for the canny edge detection algorithm, play with these to see different results
        int threshold1 = 50;
        int threshold2 = 1000;

        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
        Imgproc.Canny(image, im_canny, threshold1, threshold2);
        Log.e(this.getClass().getSimpleName(),"im_canny: "+im_canny.width());
        String cannyFilename = outputDir + File.separator + inputFileName + "_canny-" + threshold1 + "-" + threshold2 + "." + outputExtension;
        Log.e(this.getClass().getSimpleName(), "Writing " + cannyFilename);
        Imgcodecs.imwrite(cannyFilename, im_canny);
        Bitmap bmp = null;
        try {
//            Mat mMat = Utils.loadResource(getActivity(),R.drawable.child , Imgcodecs.CV_LOAD_IMAGE_COLOR);
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            Imgproc.cvtColor(im_canny, im_canny, Imgproc.COLOR_RGB2BGRA);
            bmp = Bitmap.createBitmap(im_canny.cols(), im_canny.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(im_canny, bmp);
        } catch (CvException e) {
            Log.d("Exception 1", e.getMessage());
        }
        if (bmp != null)
            imageView.setImageBitmap(bmp);
        else
            Log.e(this.getClass().getSimpleName(), "bitmap is null");


        return view;
    }
}
