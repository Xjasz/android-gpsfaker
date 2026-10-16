package com.delvedapps.core.reflection.models;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.delvedapps.core.reflection.controllers.BaseController;
import com.delvedapps.core.reflection.injectors.Injector;
import com.delvedapps.core.reflection.listeners.Listener;

public abstract class BaseActivity extends FragmentActivity implements BaseController {
    private final String TAG = getClass().getSimpleName();

    @Override
    public View getView() {
        return this.findViewById(android.R.id.content);
    }

    public abstract int getLayout();

    public View getStatusBarBackground() {
        return getView().findViewWithTag("StatusBar");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        setContentView(getLayout());
        Injector.inject(this);
        Listener.register(this);
        View v = getStatusBarBackground();
        setStatusBarColor(v, getStatusBarColor(null));
    }

    public int getStatusBarColor(View view) {
        return Color.BLACK;
    }

    public void setStatusBarColor(View view, int color) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            view.getLayoutParams().height = getStatusBarHeight();
            view.setVisibility(View.VISIBLE);
            view.setBackgroundColor(color);
        }
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return getResources().getDimensionPixelSize(resourceId);
        return 0;
    }

    @Override
    public void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard");
        if (this != null && this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null) {
            ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            if (getView() != null) {
                getView().requestFocus();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        hideSoftKeyboard();
        Listener.unregister(this);
        super.onDestroy();
    }
}
