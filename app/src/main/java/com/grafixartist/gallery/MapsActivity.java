package com.grafixartist.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback  {

    private GoogleMap mMap;

    private Location myLocation;

    private Marker marker;

    private DatabaseHelper dh;

    private boolean useMarkerClicked = false;

    private static final int CHOOSE_IMAGE_REQUEST = 1;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dh = new DatabaseHelper(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Finding your location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (myLocation != null) {
            drawMarker(myLocation);
        }
        return false;
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

        mMap.setOnMapLongClickListener(this);

        mMap.setOnMyLocationButtonClickListener(this);

        final Context x = this;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (useMarkerClicked) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(x);

                    dialog.setMessage(getResources().getString(R.string.ConfirmLocation));

                    dialog.setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent resultIntent = new Intent();
                            LatLng coordinates = marker.getPosition();
                            double latitude = coordinates.latitude;
                            double longitude = coordinates.longitude;
                            String result = Double.toString(latitude) + ":" + Double.toString(longitude);
                            resultIntent.putExtra("2", result);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    });

                    dialog.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        }
                    });

                    dialog.show();
                    useMarkerClicked = false;
                }
            }
        });

        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if(locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                enableGPSDialog();
            }
        } else{
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(!TextUtils.isEmpty(locationProviders)) {
                enableGPSDialog();
            }
        }
        enableMyLocation();
    }

    private void enableGPSDialog() {
        // notify user
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);

                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Getting Current Location
                Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // redraw the marker when get location update.
                        myLocation = location;
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
                };

                if (location != null) {
                    //PLACE THE INITIAL MARKER
                    drawMarker(location);
                }
//            else {
//                location = mMap.getMyLocation();
//                drawMarker(location);
//            }

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    private void drawMarker(Location location){
        // Remove any existing markers on the map
        mMap.clear();
        this.myLocation = location;
        LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
        String strLongitude = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
        String strLatitude = location.convert(location.getLatitude(), location.FORMAT_DEGREES);
        marker = mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat: " + strLatitude + " Lng: " + strLongitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Marker Location"));
    }

    public void MoveToMarker(View v) {
        LatLng latLng = this.marker.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        useMarkerClicked = true;
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        drawMarker(location);
    }

}
