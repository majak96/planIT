package com.example.planit.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.example.planit.R;
import com.example.planit.utils.LocationDialog;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;

public class DirectionsActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "DirectionsActivity";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    private AlertDialog dialog;
    private Marker userMarker;
    private GeoPoint taskLocation;

    private MapView map;
    private MapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_directions);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Location");

        //configure the map
        map = findViewById(R.id.map_view);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);

        mapController = (MapController) map.getController();
        mapController.setZoom(10);

        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        //get task location coordinates and address
        Double latitude = getIntent().getDoubleExtra("latitude", 0);
        Double longitude = getIntent().getDoubleExtra("longitude", 0);
        String address = getIntent().getStringExtra("address");

        //mark task location
        taskLocation = new GeoPoint(latitude, longitude);
        mapController.setCenter(taskLocation);

        Marker marker = new Marker(map);
        marker.setTitle(address + " (" + latitude + ", " + longitude + ")");
        marker.setSubDescription("The location of your task");
        marker.setPosition(taskLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_place_red, null));
        map.getOverlays().add(marker);
    }

    public void updateMarker(GeoPoint point) {
        //add a new marker
        if (userMarker == null) {
            userMarker = new Marker(map);

            userMarker.setPosition(point);
            userMarker.setTitle(" (" + point.getLatitude() + ", " + point.getLongitude() + ")");
            userMarker.setSubDescription("Your current location");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            userMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_person_place_blue, null));

            map.getOverlays().add(userMarker);

            //set bounding box to see both markers
            BoundingBox boundingBox = calculateBoundingBox(taskLocation, point);
            map.zoomToBoundingBox(boundingBox, false);

            map.invalidate();
        }
        //update the marker position if it exists
        else {
            userMarker.setPosition(point);
            map.invalidate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        map.onResume();

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);

        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //if location isn't enabled show dialog
        if (!enabledGPS && !enabledNetwork) {
            showLocationDialog();
        } else {
            //check for permissions
            if (checkLocationPermission()) {
                locationManager.requestLocationUpdates(provider, 5000, 10, this);
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint taskLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        updateMarker(taskLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public boolean checkLocationPermission() {
        //if we don't have the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //check if we should show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // show an explanation for the permission
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_not_allowed_title)
                        .setMessage(R.string.location_not_allowed_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //request the permission
                                ActivityCompat.requestPermissions(DirectionsActivity.this,
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);


                            }
                        })
                        .setNegativeButton(R.string.no_allow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
            } else {
                //request the permission without explanation
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        //if we have the permission
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Criteria criteria = new Criteria();
                    provider = locationManager.getBestProvider(criteria, true);
                    locationManager.requestLocationUpdates(provider, 5000, 10, this);
                }
            } else if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Criteria criteria = new Criteria();
                    provider = locationManager.getBestProvider(criteria, true);
                    locationManager.requestLocationUpdates(provider, 5000, 10, this);
                }
            }
            return;
        }
    }

    public BoundingBox calculateBoundingBox(GeoPoint taskLocation, GeoPoint userLocation) {
        double westPoint = Math.min(taskLocation.getLongitude(), userLocation.getLongitude()) - 0.05;
        double eastPoint = Math.max(taskLocation.getLongitude(), userLocation.getLongitude()) + 0.05;
        double southPoint = Math.max(taskLocation.getLatitude(), userLocation.getLatitude()) - 0.05;
        double northPoint = Math.min(taskLocation.getLatitude(), userLocation.getLatitude()) + 0.05;

        BoundingBox boundingBox = new BoundingBox(Math.max(northPoint, -90.0), Math.min(eastPoint, 180), Math.min(southPoint, 90), Math.max(westPoint, -180));
        return boundingBox;
    }

    private void showLocationDialog() {
        if (dialog == null) {
            dialog = new LocationDialog(this).prepareDialog();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
        map.onPause();
    }
}
