package com.task.player;

import android.app.Activity;
import android.util.DisplayMetrics;

public class DeviceUtils {

    static int getWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    static int getHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int getAspectRatioHeight(int itemWidth) {
        return ((itemWidth * 9) / 16);
    }

    public static int getAspectRatioWidth(int itemHeight) {
        return ((itemHeight * 16) /9);
    }
}