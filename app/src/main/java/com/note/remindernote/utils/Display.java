

package com.note.remindernote.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class Display {

    private Display() {
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getUsableSize(Context mContext) {
        Point displaySize = new Point();
        try {
            WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                android.view.Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getSize(displaySize);
                }
            }
        } catch (Exception e) {
        }
        return displaySize;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getScreenDimensions(Context mContext) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        android.view.Display display = wm.getDefaultDisplay();
        Point size = new Point();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        size.x = metrics.widthPixels;
        size.y = metrics.heightPixels;
        return size;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getNavigationBarHeightKitkat(Context mContext) {
        return getScreenDimensions(mContext).y - getUsableSize(mContext).y;
    }


}
