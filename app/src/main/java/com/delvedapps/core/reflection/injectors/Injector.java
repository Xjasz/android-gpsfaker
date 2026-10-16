package com.delvedapps.core.reflection.injectors;

import android.util.Log;
import android.view.View;

import com.delvedapps.core.reflection.controllers.BaseController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Injector {
    private static final String TAG = Injector.class.getSimpleName();

    public static void inject(final BaseController controller) {
        final View view = controller.getView();
        if (view == null) {
            throw new RuntimeException("Injected view was NULL!!!");
        } else {
            injectField(controller, view);
            injectMethod(controller, view);
        }
    }

    private static void injectField(BaseController controller, final View view) {
        for (Field field : controller.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) != null) {
                try {
                    field.setAccessible(true);
                    field.set(controller, view.findViewById(field.getAnnotation(Inject.class).id()));
                    Log.i(TAG, String.format("injected field: %s", field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void injectMethod(final BaseController controller, final View view) {
        for (final Method method : controller.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(Inject.class) != null) {
                final int id = method.getAnnotation(Inject.class).id();
                view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, String.valueOf(view.getResources().getResourceName(id)) + " was clicked");
                        controller.hideSoftKeyboard();
                        try {
                            method.setAccessible(true);
                            method.invoke(controller);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Log.i(TAG, String.format("injected method: %s", method.getName()));
            }
        }
    }
}

