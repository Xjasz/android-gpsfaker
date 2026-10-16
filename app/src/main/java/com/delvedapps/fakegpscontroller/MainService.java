package com.delvedapps.fakegpscontroller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.delvedapps.core.reflection.controllers.BaseController;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.LocationMessage;
import com.delvedapps.core.storage.sharedpref.SavedPrefs;

import org.microg.networklocation.backends.apple.AppleWifiLocationSource;
import org.microg.networklocation.backends.cellapi.CellAPI;
import org.microg.networklocation.backends.file.NewFileCellLocationSource;
import org.microg.networklocation.backends.file.OldFileCellLocationSource;
import org.microg.networklocation.backends.mapquest.NominatimGeocodeSource;
import org.microg.networklocation.backends.mozilla.IchnaeaCellLocationSource;
import org.microg.networklocation.backends.opencellid.OpenCellIdLocationSource;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationCalculator;
import org.microg.networklocation.data.LocationRetriever;
import org.microg.networklocation.data.WifiSpec;
import org.microg.networklocation.database.GeocodeDatabase;
import org.microg.networklocation.database.LocationDatabase;
import org.microg.networklocation.helper.Reflected;
import org.microg.networklocation.platform.PlatformFactory;
import org.microg.networklocation.provider.GeocodeProvider;
import org.microg.networklocation.provider.NetworkLocationProvider;
import org.microg.networklocation.source.GeocodeSource;
import org.microg.networklocation.source.LocationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainService extends Service implements BaseController, View.OnClickListener, View.OnTouchListener {
    private static final String TAG = MainService.class.getSimpleName();
    private static ImageView ivClose, ivDown, ivExpand, ivLeft, ivMap, ivMinimize, ivMove, ivRight, ivUp;
    private static int oldX = 0, oldY = 0, newX = 0, newY = 0;
    private static View vInnerController, vOuterController;
    private static BroadcastReceiver airplaneModeReceiver;
    private static LocationCalculator locationCalculator;
    private static LocationRetriever locationRetriever;
    private static NetworkLocationProvider nlprovider;
    private static LocationRunnable locationRunnable;
    private static LocationDatabase locationDatabase;
    private static WindowManager.LayoutParams params;
    private static int deviceHeight, deviceWidth;
    private static GeocodeProvider geoprovider;
    private static WindowManager windowManager;
    private static WifiManager wifiManager;
    private static double xCenter, yCenter;
    private static Thread locationThread;
    private static ViewGroup viewGroup;
    private static float deviceDpi;
    private static Context context;

    private static class LocationRunnable implements Runnable {
        private boolean running = true;

        public void run() {
            Random random = new Random();
            Object oLat, oLon;
            while (running) {
                oLat = SavedPrefs.getPref(context, SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING);
                oLon = SavedPrefs.getPref(context, SavedPrefs.LOCATION_LNG, null, SavedPrefs.TYPE_STRING);
                if (oLat == null || oLon == null || nlprovider == null) {
                    localSleep(1000);
                } else {
                    Location network = new Location("network"), gps = new Location("gps");
                    network.setLatitude(Double.valueOf(oLat.toString()));
                    network.setLongitude(Double.valueOf(oLon.toString()));
                    gps.setLatitude(Double.valueOf(oLat.toString()));
                    gps.setLongitude(Double.valueOf(oLon.toString()));
                    try {
                        nlprovider.onLocationChanged(network);
                        nlprovider.onLocationChanged(gps);
                    } catch (SecurityException e) {
                        Log.d(TAG, "SecurityException was triggered");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "The app needs to be in your system directory.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Listener.send(new LocationMessage(1, null));
                        localSleep(60000);
                    }
                    localSleep(random.nextInt(150) + 25);
                }
            }
        }

        private void localSleep(int duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                Log.d(TAG, "locationThread was stopped");
            }
        }

        public void stop() {
            running = false;
        }
    }

    @Listen(consumed = true)
    public void onMessageReceived(LocationMessage request) {
        switch (request.object) {
            case 0:
                Log.d(TAG, "Set Location");
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LAT, request.location.getLatitude(), SavedPrefs.TYPE_STRING);
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LNG, request.location.getLongitude(), SavedPrefs.TYPE_STRING);
                break;
            case 1:
                Log.d(TAG, "End Location");
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING);
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LNG, null, SavedPrefs.TYPE_STRING);
                break;
            case 2:
                Log.d(TAG, "Set Controller");
                SavedPrefs.setPref(context, SavedPrefs.CONTROLLER, "VALID", SavedPrefs.TYPE_STRING);
                onCreate();
                break;
            case 3:
                Log.d(TAG, "End Controller");
                SavedPrefs.setPref(context, SavedPrefs.CONTROLLER, null, SavedPrefs.TYPE_STRING);
                removeController();
                break;
            case 4:
                Log.d(TAG, "End Controller && End Location");
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING);
                SavedPrefs.setPref(context, SavedPrefs.LOCATION_LNG, null, SavedPrefs.TYPE_STRING);
                SavedPrefs.setPref(context, SavedPrefs.CONTROLLER, null, SavedPrefs.TYPE_STRING);
                removeController();
                break;
        }
    }


    @Override
    public View getView() {
        return viewGroup;
    }

    @Override
    public void hideSoftKeyboard() {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "Incoming Bind Intent: " + intent);
        if (intent == null || intent.getAction() == null)
            return null;
        String action = intent.getAction();
        if (action.equalsIgnoreCase("com.google.android.location.NetworkLocationProvider") ||
                action.equalsIgnoreCase("com.android.internal.location.service.NetworkLocationProvider") ||
                action.equalsIgnoreCase("com.android.internal.location.service.v2.NetworkLocationProvider") ||
                action.equalsIgnoreCase("com.android.internal.location.service.v3.NetworkLocationProvider")) {
            return nlprovider.getBinder();
        } else if (action.equalsIgnoreCase("com.google.internal.android.provider.GeocodeProvider") ||
                action.equalsIgnoreCase("com.android.internal.location.service.GeocodeProvider")) {
            return geoprovider.getBinder();
        } else if (action.equalsIgnoreCase("com.google.android.location.internal.ANDROID_NLP")) {
            return nlprovider.getBinder();
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created Service");
        context = this;
        Listener.register(this);

        locationCalculator = new LocationCalculator(locationDatabase = new LocationDatabase(context), locationRetriever = new LocationRetriever(locationDatabase), PlatformFactory.newCellSpecRetriever(context), PlatformFactory.newWifiSpecRetriever(context));
        deviceHeight = getResources().getDisplayMetrics().heightPixels;
        deviceWidth = getResources().getDisplayMetrics().widthPixels;
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        nlprovider = PlatformFactory.newNetworkLocationProvider();
        deviceDpi = getResources().getDisplayMetrics().density;
        geoprovider = PlatformFactory.newGeocodeProvider();
        xCenter = 25 * deviceDpi;
        yCenter = 140 * deviceDpi;

        List<LocationSource<WifiSpec>> wifiSources = new ArrayList<>();
        List<LocationSource<CellSpec>> cellSources = new ArrayList<>();
        List<GeocodeSource> geocodeSources = new ArrayList<>();
        geocodeSources.add(new NominatimGeocodeSource(context));
        wifiSources.add(new AppleWifiLocationSource(context));
        cellSources.add(new IchnaeaCellLocationSource(context));
        cellSources.add(new OpenCellIdLocationSource(context));
        cellSources.add(new NewFileCellLocationSource());
        cellSources.add(new OldFileCellLocationSource());
        cellSources.add(new CellAPI(context));
        locationRetriever.setWifiLocationSources(wifiSources);
        locationRetriever.setCellLocationSources(cellSources);
        geoprovider.setSources(geocodeSources);

        geoprovider.setGeocodeDatabase(new GeocodeDatabase());
        nlprovider.setCalculator(locationCalculator);
        locationRetriever.start();

        registerReceiver(airplaneModeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateProviderStateOnAirplaneMode();
            }
        }, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        updateProviderStateOnAirplaneMode();

        if (SavedPrefs.getPref(context, SavedPrefs.CONTROLLER, null, SavedPrefs.TYPE_STRING) != null) {
            if (windowManager == null || viewGroup == null) {
                createView();
            } else {
                params = new WindowManager.LayoutParams((int) (145 * deviceDpi), (int) (145 * deviceDpi), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.LEFT | Gravity.TOP;
                windowManager.addView(viewGroup, params);
            }
        }
        if (locationThread == null || locationRunnable == null || !locationThread.isAlive()) {
            locationThread = new Thread(locationRunnable = new LocationRunnable());
            locationThread.start();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopped service");
        Listener.unregister(this);
        unregisterReceiver(airplaneModeReceiver);
        removeController();

        if (locationRunnable != null && locationThread != null) {
            locationRunnable.stop();
            locationThread.interrupt();
        }
        if (locationRetriever != null)
            locationRetriever.stop();
        if (nlprovider != null)
            nlprovider.disable();

        locationCalculator = null;
        locationRetriever = null;
        locationRunnable = null;
        locationThread = null;
        geoprovider = null;
        wifiManager = null;
        nlprovider = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                params.x = (int) (event.getRawX() - xCenter);
                params.y = (int) (event.getRawY() - yCenter);
                windowManager.updateViewLayout(viewGroup, params);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivExpand:
                vInnerController.setVisibility(View.VISIBLE);
                vOuterController.setVisibility(View.VISIBLE);
                params.width = (int) (145 * deviceDpi);
                params.height = (int) (145 * deviceDpi);
                params.x = oldX;
                params.y = oldY;
                windowManager.updateViewLayout(viewGroup, params);
                break;
            case R.id.ivMinimize:
                vInnerController.setVisibility(View.GONE);
                vOuterController.setVisibility(View.GONE);
                params.width = (int) (40 * deviceDpi);
                params.height = (int) (40 * deviceDpi);
                params.x = newX = oldX = params.x > deviceWidth / 2 - 85 ? deviceWidth - 30 : 0;
                params.y = newY = oldY = params.y > deviceHeight / 2 - 65 ? deviceHeight - 30 : 0;
                windowManager.updateViewLayout(viewGroup, params);
                break;
            case R.id.ivDown:
                moveLocation("DOWN");
                break;
            case R.id.ivUp:
                moveLocation("UP");
                break;
            case R.id.ivLeft:
                moveLocation("LEFT");
                break;
            case R.id.ivRight:
                moveLocation("RIGHT");
                break;
            case R.id.ivMap:
                Listener.send(new LocationMessage(3, null));
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.ivClose:
                Listener.send(new LocationMessage(4, null));
                break;
        }
    }

    private void createView() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams((int) (145 * deviceDpi), (int) (145 * deviceDpi), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.window, null);
        windowManager.addView(viewGroup, params);
        vInnerController = viewGroup.findViewById(R.id.vInnerController);
        vOuterController = viewGroup.findViewById(R.id.vOuterController);
        ivUp = (ImageView) viewGroup.findViewById(R.id.ivUp);
        ivLeft = (ImageView) viewGroup.findViewById(R.id.ivLeft);
        ivRight = (ImageView) viewGroup.findViewById(R.id.ivRight);
        ivDown = (ImageView) viewGroup.findViewById(R.id.ivDown);
        ivMinimize = (ImageView) viewGroup.findViewById(R.id.ivMinimize);
        ivClose = (ImageView) viewGroup.findViewById(R.id.ivClose);
        ivMove = (ImageView) viewGroup.findViewById(R.id.ivMove);
        ivMap = (ImageView) viewGroup.findViewById(R.id.ivMap);
        ivExpand = (ImageView) viewGroup.findViewById(R.id.ivExpand);
        ivUp.setColorFilter(Color.parseColor("#303030"));
        ivLeft.setColorFilter(Color.parseColor("#303030"));
        ivRight.setColorFilter(Color.parseColor("#303030"));
        ivDown.setColorFilter(Color.parseColor("#303030"));
        ivUp.setOnClickListener(this);
        ivLeft.setOnClickListener(this);
        ivRight.setOnClickListener(this);
        ivDown.setOnClickListener(this);
        ivMinimize.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivMap.setOnClickListener(this);
        ivExpand.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
    }

    public static void updateProviderStateOnAirplaneMode() {
        boolean airplane = isAirplaneModeOn();
        boolean wifi = wifiManager.isWifiEnabled();
        Log.d(TAG, "airplane:" + airplane + " | wifi:" + wifi);
        if (airplane && !wifi) {
            Log.d(TAG, "AirplaneMode is enabled and wifi is off, so no way to get location for us");
            nlprovider.disable();
        } else {
            Log.d(TAG, "AirplaneMode or wifi is enabled. make sure we're active!");
            nlprovider.enable();
        }
    }

    private static boolean isAirplaneModeOn() {
        return ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) ? (Settings.System.getInt(context.getContentResolver(), Settings.System
                .AIRPLANE_MODE_ON, 0)) : (Reflected.androidProviderSettingsGlobalGetInt(context.getContentResolver(), "airplane_mode_on", 0))) != 0;
    }

    private void moveLocation(String paramString) {
        Log.d(TAG, "moveLocation pressed " + paramString);
        Object oLat = SavedPrefs.getPref(context, SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING), oLng = SavedPrefs.getPref(context, SavedPrefs.LOCATION_LNG, null, SavedPrefs.TYPE_STRING);
        if (oLat != null && oLng != null) {
            Double lat = Double.valueOf(oLat.toString()), lng = Double.valueOf(oLng.toString());
            if (paramString.contentEquals("UP")) {
                lat += +.00015;
            } else if (paramString.contentEquals("DOWN")) {
                lat += -.00015;
            } else if (paramString.contentEquals("RIGHT")) {
                lng += +.00015;
            } else {
                lng += -.00015;
            }
            Location location = new Location("location");
            location.setLatitude(lat);
            location.setLongitude(lng);
            Listener.send(new LocationMessage(0, location));
        }
    }

    private void removeController() {
        try {
            if (windowManager != null && viewGroup != null)
                windowManager.removeView(viewGroup);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "viewGroup isn't attached to windowManager");
        }
    }
}
