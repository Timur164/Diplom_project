package com.timyr.opencv_demo.activity;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.timyr.opencv_demo.R;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.VideoView;

import com.timyr.opencv_demo.controller.BaseActivity;
import com.timyr.opencv_demo.controller.Detector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


//import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Timur on 11.05.2017.
 */

public class VideoViewDemoActivity extends BaseActivity {

    VideoCapture videoCapture;

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.video_activity);
//        String path = getIntent().getStringExtra("path");
//        ImageView imageView = (ImageView) findViewById(R.id.image);
//
//
//        if (path != null) {
////            VideoView videoView = (VideoView) findViewById(R.id.video_view);
////            videoView.setVideoPath(path);
////            videoView.start();
//            FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
//            mmr.setDataSource(path);
//            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
//            for (int i = 0; i < 10000; i++) {
//                Bitmap b = mmr.getFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
//                imageView.setImageBitmap(b);
//            }
//            byte[] artwork = mmr.getEmbeddedPicture();
//            mmr.release();
//
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        final ImageView imageView = (ImageView) findViewById(R.id.image);

        String path = getIntent().getStringExtra("path");
        File videoFile = new File(path);
        final Uri videoFileUri = Uri.parse(videoFile.toString());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            try {
                MediaCodec codec = MediaCodec.createByCodecName(videoFileUri.getPath());
                MediaFormat format = codec.getOutputFormat();
                int width = format.getInteger(MediaFormat.KEY_WIDTH);
                if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
                    width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
                }
                int height = format.getInteger(MediaFormat.KEY_HEIGHT);
                if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
                    height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



//        final FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//        retriever.setDataSource(videoFile.getAbsolutePath());
//        final ArrayList<Bitmap> rev = new ArrayList<Bitmap>();
//
//        MediaPlayer mp = MediaPlayer.create(getBaseContext(), videoFileUri);
//        final int millis = mp.getDuration();
//        for (int i = 0; i < millis; i += 1000) {
//            Bitmap bitmap = retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
//            rev.add(bitmap);
//        }
//
//        Log.e("my", "rev.size : " + rev.size());
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                for (int i = 0; i < rev.size(); i++) {
//                    imageView.setImageBitmap(rev.get(i));
//                    imageView.invalidate();
//
//                }
//            }
//        });


    }


    public void saveFrames(ArrayList<Bitmap> saveBitmapList) throws IOException {

        String folder = Environment.getExternalStorageDirectory().toString();
        File saveFolder = new File(folder + "/Movies/new /");
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }


        int i = 1;
        for (Bitmap b : saveBitmapList) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

            File f = new File(saveFolder, ("frame" + i + ".jpg"));

            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            fo.flush();
            fo.close();

            i++;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
}
