package com.maymanm.weathermap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.maymanm.weathermap.viewmodel.DirectionsViweModel;

import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String locationProvider;
    private static final String TAG = "MapsActivity";
    private Location lastLocation;
    private double lastLong, lastLat;
    private Marker currentMarker, targetMarker;
    private DirectionsViweModel directionsViweModel;
    private Polyline[] polyLineArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        settingUpTheMap();

        mMap.setOnMapClickListener(this);
    }

    private void settingUpTheMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    checkSelfPermission(ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_COARSE_LOCATION,
                                ACCESS_FINE_LOCATION},
                        111);
                return;
            }
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationProvider = locationManager.getBestProvider(new Criteria(), false);
        locationManager.requestLocationUpdates(Objects.requireNonNull(locationProvider), 1, 10, MapsActivity.this);

        mMap.setMyLocationEnabled(true); //set a point to my location
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // button current loc
        mMap.getUiSettings().setZoomControlsEnabled(true); //set zoom buttons

        lastLocation = locationManager.getLastKnownLocation(locationProvider);
        settingMarkers();
    }

    private void settingMarkers() {
        if (lastLocation != null) {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        lastLat = location.getLatitude();
        lastLong = location.getLongitude();

        currentMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 111:
                if (grantResults.length > 0 &&
                        grantResults[0] == PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: " + grantResults[0]);
                    onMapReady(mMap);
                } else {
                    //permission denied
                    // array is zero
                    Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (targetMarker != null) targetMarker.remove();
        targetMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Location targetLoc = new Location("");
        targetLoc.setLatitude(latLng.latitude);
        targetLoc.setLongitude(latLng.longitude);
        float distance = lastLocation.distanceTo(targetLoc);
        Log.d(TAG, "onMapClick: " + distance + "meters away");

        directionsViweModel = ViewModelProviders.of(this).get(DirectionsViweModel.class);
        directionsViweModel.getDirectionsLiveData(lastLocation.getLatitude() + "," + lastLocation.getLongitude(),
                latLng.latitude + "," + latLng.longitude,
                getString(R.string.google_maps_key)
        ).observe(this, directionsModel -> {
            if (directionsModel != null){
                Log.d(TAG, "onMapClick: "+directionsModel.getStatus());
            }

            directionsViweModel.getPolyLineLiveData(directionsModel).observe(this, polylines -> {

                if (polyLineArray != null){
                    for (Polyline polyline: polyLineArray){
                        polyline.remove();
                    }
                }

                if (polylines!= null){
                 polyLineArray = new Polyline[polylines.length];
                    Log.d(TAG, "onMapClick: "+"reached here");
                    for (int i = 0; i< polylines.length;i++){
                        PolylineOptions options = new PolylineOptions();
                        options.color(Color.RED);
                        options.width(10);
                        options.addAll(PolyUtil.decode(polylines[i]));
                        polyLineArray[i] = mMap.addPolyline(options);
                    }
                }
            });

        });




    }


}
