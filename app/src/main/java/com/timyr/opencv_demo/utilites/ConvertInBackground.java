package com.timyr.opencv_demo.utilites;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;



public class ConvertInBackground extends AsyncTask<Void, Void, Void> {
    private ImageView imageView;
    private Bitmap bitmap;

    public ConvertInBackground(ImageView imageView, Bitmap bitmap) {
        this.imageView = imageView;
        this.bitmap = bitmap;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Create new OpenCV variable with the same width and height of the bitmap variable
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8U);

        //Convert the Bitmap to Mat
        Utils.bitmapToMat(bitmap, mat);

        //Convert image to gray scale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);

        //Return the image from mat to Bitmap after convert it to gray scale using OpenCV libs
        Utils.matToBitmap(mat, bitmap);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        //Update the imageView with the new gray scale image
        imageView.setImageBitmap(bitmap);

    }
}