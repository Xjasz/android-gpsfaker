package com.delvedapps.core.reflection.models;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.delvedapps.core.reflection.controllers.BaseController;
import com.delvedapps.core.reflection.injectors.Injector;
import com.delvedapps.core.reflection.listeners.Listener;


public abstract class BaseFragment extends Fragment implements BaseController {
    private final String TAG = getClass().getSimpleName();
    private ViewGroup viewGroup;

    public abstract int getLayout();

    @Override
    public View getView() {
        return viewGroup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        viewGroup = (ViewGroup) inflater.inflate(getLayout(), container, false);
        Injector.inject(this);
        Listener.register(this);
        View v = getStatusBarBackground();
        if (v != null)
            setStatusBarColor(v, getStatusBarColor(null));
        return viewGroup;
    }

    public View getStatusBarBackground() {
        return getView().findViewWithTag("StatusBar");
    }

    public int getStatusBarColor(View view) {
        return Color.BLACK;
    }

    public void setStatusBarColor(View view, int color) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
        if (this != null && getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
            ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            if (getView() != null) {
                getView().requestFocus();
            }
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, String.valueOf(Thread.currentThread().getStackTrace()[2].getMethodName()));
        hideSoftKeyboard();
        Listener.unregister(this);
        super.onDestroyView();
    }
}
