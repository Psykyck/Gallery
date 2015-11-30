package com.grafixartist.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback  {

    private GoogleMap mMap;

    private Location myLocation;

    private Marker marker;

    private DatabaseHelper dh;

    private boolean useMarkerClicked = false;

    private int radius = 100;

    private static final int CHOOSE_IMAGE_REQUEST = 1;

    private static final int ENABLE_LOCATION_REQUEST = 2;

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
        // Create database helper instance
        this.dh = new DatabaseHelper(this);
        // Set up content view and support map fragment
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (this.myLocation != null) {
            // If location is known, draw marker there
            drawMarker(this.myLocation);
        }
        else {
            Toast.makeText(this, getString(R.string.WaitingLocationToast), Toast.LENGTH_SHORT).show();
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

        // Set up listeners
        mMap.setOnMapLongClickListener(this);

        mMap.setOnMyLocationButtonClickListener(this);

        final Context x = this;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (useMarkerClicked) {
                    // If use marker location is pressed, then confirm it
                    useLocationConfirmation(x);
                }
            }
        });
        // Check location and enable it
        checkLocationOn();
        enableMyLocation();
    }

    private void checkLocationOn() {
        int locationMode = 0;
        String locationProviders;

        // Check sdk is kitkat and above on device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                // Check location is off or on
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            // If off, pull up gps dialog to turn it on
            if(locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                enableGPSDialog();
            }
        } else{
            // Check location mode is on or off
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            // If not on, pull up gps dialog
            if(!TextUtils.isEmpty(locationProviders)) {
                enableGPSDialog();
            }
        }
        // Enable location
        enableMyLocation();
    }

    private void enableGPSDialog() {
        // Notify user to turn on gps location
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.gps_not_enabled_title));
        dialog.setMessage(getString(R.string.gps_not_enabled_message));
        dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(myIntent, ENABLE_LOCATION_REQUEST);
            }
        });
        dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // Check permission for access fine location is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Check map is ready
            if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                // Getting Current Location
                Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Re-update location when changed
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

                // If location is found, draw marker there
                if (location != null) {
                    drawMarker(location);
                }
                // Further request location updates every 5 seconds
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    private void drawMarker(Location location){
        // Remove any existing markers on the map
        mMap.clear();
        // Re-update location
        this.myLocation = location;
        // Retrieve LatLng from location
        LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
        // Get longitude and latitude from location
        String strLongitude = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
        String strLatitude = location.convert(location.getLatitude(), location.FORMAT_DEGREES);
        // Make marker with lat long included in marker description
        marker = mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat: " + strLatitude + " Lng: " + strLongitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Marker Location"));
    }

    public void MoveToMarker(View v) {
        // If marker is on map, move view to marker
        if(this.marker != null) {
            LatLng latLng = this.marker.getPosition();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            useMarkerClicked = true;
            mMap.animateCamera(cameraUpdate);
        }
        else {
            Toast.makeText(this, getString(R.string.MakeMarkerToast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Check permission request code
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        // Check permission is granted
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
        // Check if permission is denied
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
        // Redraw marker at long click location
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        drawMarker(location);
    }

    public void getLocation(View view) {
        try
        {
            // Get edit text from view for location search
            EditText searchText = (EditText)findViewById(R.id.searchEditText);
            // Find geocoder for default locale
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            // Pull up results from using location search
            List<Address> addresses = geocoder.getFromLocationName(searchText.getText().toString(), 1);
            StringBuilder sb = new StringBuilder();
            // Check for any results
            if (addresses != null && addresses.size() > 0)
            {
                // Get first result
                final Address address = addresses.get(0);
                // Build dialog asking for the result location searched
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                adb.setTitle(R.string.SearchResult);
                adb.setMessage(this.getString(R.string.SearchMessage) + sb);
                adb.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // Save location of the result and draw and move to marker
                        Location location = new Location(myLocation);
                        location.setLatitude(address.getLatitude());
                        location.setLongitude(address.getLongitude());
                        drawMarker(location);
                        LatLng latLng = marker.getPosition();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                        mMap.animateCamera(cameraUpdate);
                    }
                });
                adb.setNegativeButton(getString(R.string.No), null);
                adb.show();
            }
            else {
                // Show dialog for invalid location
                createAlertDialog();
            }
        }
        catch (IOException e)
        {
            createAlertDialog();
        }
    }

    private void createAlertDialog() {
        // Build dialog to indicate invalid location
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.InvalidTitle);
        adb.setMessage(R.string.InvalidMessage);
        adb.setPositiveButton(R.string.Close,null);
        adb.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent iData) {
        super.onActivityResult(requestCode, resultCode, iData);
        switch (requestCode) {
            // Case for image request result (replacement photo)
            case(CHOOSE_IMAGE_REQUEST): {
                // Check if it is success
                if(resultCode != RESULT_CANCELED) {
                    Intent resultIntent = new Intent();
                    // Get coordinates from marker
                    LatLng coordinates = marker.getPosition();
                    double latitude = coordinates.latitude;
                    double longitude = coordinates.longitude;
                    String result = Double.toString(latitude) + ":" + Double.toString(longitude);
                    // Get replacement photo uri
                    Uri selectedImageUri = iData.getData();
                    // Store in intent the radius, coordinates, and replacement uri
                    resultIntent.putExtra("radius", radius);
                    resultIntent.putExtra("coordinates", result);
                    resultIntent.putExtra("replacement", selectedImageUri.toString());
                    setResult(Activity.RESULT_OK, resultIntent);
                    // Return to main activity
                    finish();
                }
                break;
            }
            // Case where location setting is returned
            case(ENABLE_LOCATION_REQUEST): {
                // Check location is on
                checkLocationOn();
                break;
            }
        }
    }

    private void useLocationConfirmation(Context x) {

        // Store context for dialog
        final Context a = x;

        // Build dialog asking to use location
        AlertDialog.Builder dialog = new AlertDialog.Builder(x);

        dialog.setMessage(getResources().getString(R.string.ConfirmLocation));

        dialog.setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // Ask for radius
                getRadiusConfirmation(a);
            }
        });

        dialog.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });

        dialog.show();
        // Indicate use marker button not clicked as false
        useMarkerClicked = false;
    }

    private void getRadiusConfirmation(Context x) {

        // Build alert dialog for asking for radius
        AlertDialog.Builder dialog = new AlertDialog.Builder(x);

        dialog.setTitle(R.string.RadiusAlertTitle);

        dialog.setMessage(R.string.RadiusAlertMessage);

        final ArrayList<String> numbersAsStrings = new ArrayList<>();

        // Build choices for number picker (100-1000 in intervals of 100)
        for (int i = 1; i <= 10; i++) {
            numbersAsStrings.add(String.valueOf(i * 100));
        }

        // Create NumberPicker using numberAsStrings
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(numbersAsStrings.size() - 1);
        numberPicker.setDisplayedValues(numbersAsStrings.toArray(new String[numbersAsStrings.size()]));

        dialog.setPositiveButton(getString(R.string.Okay), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get index of value of the number picker
                int index = numberPicker.getValue();
                // Set up intent to pick images
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Get radius value from using index
                radius = Integer.parseInt(numbersAsStrings.get(index));
                // Start activity to choose replacement photo
                startActivityForResult(i, CHOOSE_IMAGE_REQUEST);
            }
        });

        dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        dialog.setView(numberPicker).show();
    }
}
