package com.delvedapps.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.delvedapps.core.util.AbstractAnimation;
import com.delvedapps.core.util.contstant.Constants;

public final class Singleton extends Application {
    private static final String TAG = Singleton.class.getSimpleName();
    private static Context CONTEXT;
    private static Activity ACTIVITY;
    public static String APP_VERSION;
    public static String DEVICE_ID;
    public static String PACKAGE_NAME;
    public static int DEVICE_HEIGHT;

    public static boolean AFTER_JELLYBEAN;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application Singleton Created");
        CONTEXT = getApplicationContext();
        DEVICE_ID = Settings.Secure.getString(CONTEXT.getContentResolver(), Settings.Secure.ANDROID_ID);
        DEVICE_HEIGHT = CONTEXT.getResources().getDisplayMetrics().heightPixels;
        PACKAGE_NAME = CONTEXT.getPackageName();
        AFTER_JELLYBEAN = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
        try {
            APP_VERSION = String.valueOf(CONTEXT.getPackageManager().getPackageInfo(CONTEXT.getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            APP_VERSION = Constants.APP_VERSION_UNKNOWN;
        }
    }

    public static Context getAppContext() {
        return CONTEXT;
    }

    public static Activity getMainActivity() {
        return ACTIVITY;
    }

    public static void registerMainActivity(Activity activity) {
        ACTIVITY = activity;
    }

    public static void toastMessage(final String message) {
        if (ACTIVITY != null && CONTEXT != null)
            ACTIVITY.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CONTEXT, message, Toast.LENGTH_SHORT).show();
                }
            });
    }

    public static void pause(int pauseTime) {
        try {
            Thread.sleep(pauseTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startFadeAnimation(final View view, final boolean in, int duration) {
        view.setVisibility(View.VISIBLE);
        Animation animation;
        if (in) {
            animation = new AlphaAnimation(0, 1);
        } else {
            animation = new AlphaAnimation(1, 0);
        }
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(duration);
        animation.setAnimationListener(new AbstractAnimation() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!in)
                    view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(animation);
    }

    public static boolean isParsable(String string, String type) {
        boolean parsable = true;
        try {
            if (type.contentEquals("int"))
                Integer.parseInt(string);
            else if (type.contentEquals("float"))
                Float.parseFloat(string);
            else if (type.contentEquals("double"))
                Double.parseDouble(string);
        } catch (NumberFormatException e) {
            parsable = false;
        }
        return parsable;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) CONTEXT.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
