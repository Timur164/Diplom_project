package com.timyr.opencv_demo;

import android.app.Activity;
import android.app.Application;

public class RoadSignApp extends Application {
    private static RoadSignApp roadSignApp;
    private int widthSize = 900;
    private int heightSize = 500;
    private double minSize = 0.1;
    private double scaleFactor = 1.1;
    private int minNeighbors = 4;
    private boolean colorFilter = true;
    private boolean showSign = false;
    private boolean showWarSign=true;
    private boolean showProSign=false;


    public static RoadSignApp getApp(Activity activity) {
        return (RoadSignApp) activity.getApplication();
    }


    public static RoadSignApp getInstance(){
        if (null == roadSignApp){
            roadSignApp = new RoadSignApp();
        }
        return roadSignApp;
    }

    public int getWidthSize() {
        return widthSize;
    }

    public void setWidthSize(int widthSize) {
        this.widthSize = widthSize;
    }

    public int getHeightSize() {
        return heightSize;
    }

    public void setHeightSize(int heightSize) {
        this.heightSize = heightSize;
    }

    public double getMinSize() {
        return minSize;
    }

    public void setMinSize(double minSize) {
        this.minSize = minSize;
    }

    public boolean isShowSign() {
        return showSign;
    }

    public void setShowSign(boolean showSign) {
        this.showSign = showSign;
    }

    public boolean isShowWarSign() {
        return showWarSign;
    }

    public void setShowWarSign(boolean showWarSign) {
        this.showWarSign = showWarSign;
    }

    public boolean isShowProSign() {
        return showProSign;
    }

    public void setShowProSign(boolean showProSign) {
        this.showProSign = showProSign;
    }

    public static RoadSignApp getRoadSignApp() {
        return roadSignApp;
    }

    public static void setRoadSignApp(RoadSignApp roadSignApp) {
        RoadSignApp.roadSignApp = roadSignApp;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getMinNeighbors() {
        return minNeighbors;
    }

    public void setMinNeighbors(int minNeighbors) {
        this.minNeighbors = minNeighbors;
    }

    public boolean isColorFilter() {
        return colorFilter;
    }

    public void setColorFilter(boolean colorFilter) {
        this.colorFilter = colorFilter;
    }
}
