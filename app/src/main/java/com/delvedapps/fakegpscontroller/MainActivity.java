package com.delvedapps.fakegpscontroller;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.reflection.injectors.Inject;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.BackRequestMessage;
import com.delvedapps.core.reflection.messages.BackResponseMessage;
import com.delvedapps.core.reflection.messages.FragmentChangeMessage;
import com.delvedapps.core.reflection.messages.NetworkMessage;
import com.delvedapps.core.reflection.models.BaseActivity;
import com.delvedapps.core.util.contstant.Constants;
import com.delvedapps.core.util.root.RootCheck;
import com.delvedapps.core.webservice.WebService;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    @Inject(id = R.id.statusBarBackground)
    private View statusBarBackground;

    @Inject(id = R.id.vShadow)
    private View vShadow;

    @Inject(id = R.id.container_Actionbar)
    private View container_Actionbar;

    @Inject(id = R.id.vFader)
    private View vFader;

    @Inject(id = R.id.vFader)
    private void onFaderPressed() {
    }

    @Inject(id = R.id.vFader2)
    private View vFader2;

    @Inject(id = R.id.vFader2)
    private void onFader2Pressed() {
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_SubFragment,
                R.anim.anim_in,
                R.anim.slideout_to_left));
    }

    @Listen()
    public void onNetworkMessageReceived(NetworkMessage<Object> request) {
        Log.d(TAG, "----------------->  " + String.valueOf(request.object));
        if (!request.response.contentEquals(Constants.VALID_WEB_EVENT)) {
            Singleton.toastMessage(request.response);
        } else {
            switch ((Integer) request.object) {
                case WebService.GET_APP_DATA:
                    break;
                case WebService.GET_APP_COMMANDS:
                    break;
                case WebService.APP_MUST_UPDATE:
                    Log.d(TAG, "----------------->APP_MUST_UPDATEAPP_MUST_UPDATE");
                    Listener.send(new FragmentChangeMessage(new FragmentUpdate(),
                            R.id.container_SubFragment,
                            R.anim.slidein_frombottom,
                            R.anim.slideout_to_bottom));
                    break;
            }
        }
    }

    @Listen
    public void onFragmentChangeMessageReceived(FragmentChangeMessage fcm) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(fcm.inAnimation, fcm.outAnimation).replace(fcm.container, (Fragment) fcm.object).commit();
        if (fcm.container == R.id.container_SubFragment) {
            if (fcm.object instanceof FragmentDummy) {
                Singleton.startFadeAnimation(vFader, false, 300);
                Singleton.startFadeAnimation(vShadow, true, 300);
            } else {
                Singleton.startFadeAnimation(vFader, true, 300);
                Singleton.startFadeAnimation(vShadow, false, 300);
            }
        }
    }


    @Listen()
    public void onBackPressResponseMessage(BackResponseMessage<Integer> response) {
        switch (response.object) {
            case 0:
                finish();
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        Listener.send(new BackRequestMessage<>(0));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Singleton.registerMainActivity(this);
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring savedInstanceState");
        } else {
            initializeActivity();
        }
    }

    @Override
    public int getStatusBarColor(View view) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && view.getBackground() instanceof ColorDrawable) {
            float[] hsv = new float[3];
            Color.colorToHSV(((ColorDrawable) view.getBackground()).getColor(), hsv);
            hsv[2] *= 0.8f;
            return Color.HSVToColor(hsv);
        }
        return Color.BLACK;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivMenu:
                if (vFader2.getVisibility() == View.VISIBLE) {
                    Singleton.startFadeAnimation(vFader2, false, 300);
                    Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                            R.id.container_Menufragment,
                            R.anim.anim_in,
                            R.anim.slideout_to_left));
                } else {
                    Singleton.startFadeAnimation(vFader2, true, 300);
                    Listener.send(new FragmentChangeMessage(new FragmentNavigation(),
                            R.id.container_Menufragment,
                            R.anim.slidein_fromleft,
                            R.anim.anim_out));
                }
                break;
        }
    }

    private void initializeActivity() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        if (!Singleton.isMyServiceRunning(MainService.class))
            startService(new Intent(this, MainService.class));

        getSupportFragmentManager().beginTransaction().add(R.id.container_MainFragment, new FragmentMap()).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container_SubFragment, new FragmentDummy()).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container_Menufragment, new FragmentDummy()).commit();
        if (!RootCheck.checkForSystemApp()) {
            Listener.send(new FragmentChangeMessage(new FragmentErrorDirectory(),
                    R.id.container_SubFragment,
                    R.anim.slidein_frombottom,
                    R.anim.slideout_to_bottom));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton.pause(2000);
                new WebService().webEvent(new int[]{WebService.GET, WebService.GET_APP_DATA}, null);
            }
        }).start();
    }

}
