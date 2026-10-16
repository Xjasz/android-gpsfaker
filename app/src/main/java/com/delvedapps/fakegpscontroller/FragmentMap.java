package com.delvedapps.fakegpscontroller;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.reflection.injectors.Inject;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.BackRequestMessage;
import com.delvedapps.core.reflection.messages.LocationMessage;
import com.delvedapps.core.reflection.models.BaseFragment;
import com.delvedapps.core.storage.sharedpref.SavedPrefs;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentMap extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {
    private static final String TAG = FragmentMap.class.getSimpleName();

    private LatLng appLatLng = new LatLng(37.4220, -122.0841);
    private SupportMapFragment supportMapFragment;
    private GoogleApiClient googleApiClient;
    private Bitmap bitmapLocationIcon;
    private GoogleMap googleMap;


    @Inject(id = R.id.tbSetStop)
    private ToggleButton tbSetStop;

    @Inject(id = R.id.tbSetStop)
    private void onSetStopPressed() {
        if (tbSetStop.isChecked()) {
            Location location = new Location("location");
            location.setLatitude(appLatLng.latitude);
            location.setLongitude(appLatLng.longitude);
            Listener.send(new LocationMessage(0, location));
        } else {
            Listener.send(new LocationMessage(1, null));
        }
    }

    @Inject(id = R.id.ivController)
    private ImageView ivController;

    @Inject(id = R.id.ivController)
    private void onControllerPressed() {
        if (SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING) == null) {
            Singleton.toastMessage("Please set location before using the controller");
        } else {
            Singleton.toastMessage("Launching controller");
            Listener.send(new LocationMessage(2, null));
            Singleton.getMainActivity().finish();
        }
    }

    @Inject(id = R.id.ivMapType)
    private ImageView ivMapType;

    @Inject(id = R.id.ivMapType)
    private void onMapTypePressed() {
        if (SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.CURRENTMAPTYPE, "Standard", SavedPrefs.TYPE_STRING).toString().contentEquals("Standard")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.CURRENTMAPTYPE, "Hybrid", SavedPrefs.TYPE_STRING);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.CURRENTMAPTYPE, "Standard", SavedPrefs.TYPE_STRING);
        }
    }

    @Inject(id = R.id.ivCompass)
    private ImageView ivCompass;

    @Inject(id = R.id.ivCompass)
    private void onCompassPressed() {
        Singleton.startFadeAnimation(ivCompass, false, 300);
        updateMap(1);
    }

    @Inject(id = R.id.etLat)
    private EditText etLat;


    @Inject(id = R.id.etLon)
    private EditText etLon;

    @Inject(id = R.id.ivGpsSearch)
    private ImageView ivGpsSearch;

    @Inject(id = R.id.ivGpsSearch)
    private void onGpsSearchPressed() {
        Singleton.startFadeAnimation(rlSearchBackground, false, 300);
        Singleton.startFadeAnimation(ivCompass, false, 300);
        updateMap(2);
    }

    @Inject(id = R.id.ivSearch)
    private ImageView ivSearch;

    @Inject(id = R.id.ivSearch)
    private void onSearchPressed() {
        Singleton.startFadeAnimation(rlSearchBackground, true, 300);
    }

    @Inject(id = R.id.rlSearchBackground)
    private ViewGroup rlSearchBackground;

    @Inject(id = R.id.rlSearchBackground)
    private void onSearchBackgroundPressed() {
        Singleton.startFadeAnimation(rlSearchBackground, false, 300);
    }

    @Inject(id = R.id.rlSearchLayout)
    private ViewGroup rlSearchLayout;

    @Inject(id = R.id.rlSearchLayout)
    private void onSearchLayoutPressed() {
    }

    @Listen(consumed = true)
    public void onMessageReceived(BackRequestMessage<Integer> request) {
        switch (request.object) {
            case 0:
                if (rlSearchBackground.getVisibility() == View.VISIBLE) {
                    Singleton.startFadeAnimation(rlSearchBackground, false, 300);
                } else {
                    request.respond(0);
                }
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewGroup = super.onCreateView(inflater, container, savedInstanceState);
        initializeFragment();
        return viewGroup;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.setOnCameraChangeListener(this);
        if (hasLocationPermission())
            googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(appLatLng));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton.pause(1000);
                Singleton.getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMap(1);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        try {
            getChildFragmentManager().beginTransaction().remove(supportMapFragment).commit();
            googleApiClient.disconnect();
            googleMap.clear();
        } catch (Exception e) {
            Log.d(TAG, "View was destroyed");
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        } else if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(Singleton.getAppContext()).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if (ivCompass.getVisibility() == View.GONE) {
            Singleton.startFadeAnimation(ivCompass, true, 300);
        }
        updateMap(0);
    }

    private void initializeFragment() {
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMapFragment);
        supportMapFragment.getMapAsync(this);
        ivController.setColorFilter(Color.WHITE);
        ivSearch.setColorFilter(Color.WHITE);
        ivGpsSearch.setColorFilter(Color.WHITE);

        Object lat = SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.LOCATION_LAT, null, SavedPrefs.TYPE_STRING);
        Object lon = SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.LOCATION_LNG, null, SavedPrefs.TYPE_STRING);
        if (lat != null && lon != null) {
            appLatLng = new LatLng(Double.parseDouble(lat.toString()), Double.parseDouble(lon.toString()));
            tbSetStop.setChecked(true);
            onSetStopPressed();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmapLocationIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.currlocation, Singleton.getAppContext().getTheme())).getBitmap();
        } else {
            bitmapLocationIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.currlocation)).getBitmap();
        }
    }

    private void updateMap(int type) {
        Log.d(TAG, "updateMap: " + String.valueOf(type));
        if (googleMap != null) {
            googleMap.clear();
            if (type == 0) {
                LatLngBounds cameraBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                appLatLng = new LatLng(cameraBounds.southwest.latitude + (cameraBounds.northeast.latitude - cameraBounds.southwest.latitude) / 2, cameraBounds.southwest.longitude + (cameraBounds.northeast.longitude - cameraBounds.southwest.longitude) / 2);
            } else {
                if (type == 1) {
                    if (!hasLocationPermission() || !googleApiClient.isConnected())
                        return;
                    Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    if (currentLocation != null) {
                        appLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    } else {
                        return;
                    }
                } else if (type == 2) {
                    String lat = etLat.getText().toString(), lon = etLon.getText().toString();
                    if (Singleton.isParsable(lat, "double") && Singleton.isParsable(lon, "double")) {
                        appLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
                    } else {
                        Singleton.toastMessage("Please enter latitude and longitude.");
                        return;
                    }
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(buildLatLngBounds(appLatLng, .0045), 0));
            }
            googleMap.addMarker(new MarkerOptions().position(appLatLng).title("fake_location").icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmapLocationIcon, (int) ((Singleton.DEVICE_HEIGHT / 15) * .81f), (Singleton.DEVICE_HEIGHT / 15), false))));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                public boolean onMarkerClick(Marker marker) {
                    return marker.getTitle().contentEquals("fake_location");
                }
            });
        }
    }


    private boolean hasLocationPermission() {
        return Singleton.getAppContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static LatLngBounds buildLatLngBounds(LatLng latLng, double zoom) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng southWest = new LatLng(latLng.latitude + zoom, latLng.longitude + zoom);
        LatLng northEast = new LatLng(latLng.latitude - zoom, latLng.longitude - zoom);
        builder.include(southWest);
        builder.include(northEast);
        return builder.build();
    }
}
