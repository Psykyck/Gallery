package com.grafixartist.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private int pos;
    private Toolbar toolbar;
    private DatabaseHelper dh;
    private Location myLocation;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static final String TAG = "DetailActivity";

    private final String PREFS_NAME = "MyPrefsFile";

    private final String EMAIL = "email";

    private static final int CHOOSE_IMAGE_REQUEST = 1;
    private static final int CHOOSE_LOCATION_REQUEST = 2;
    private static final int ENABLE_LOCATION_REQUEST = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4;

    public ArrayList<ImageModel> data = new ArrayList<>();

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up view for detail activity
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Get data and position index of photo clicked
        data = getIntent().getParcelableArrayListExtra("data");
        pos = getIntent().getIntExtra("pos", 0);

        // Set the title from data
        setTitle(data.get(pos).getName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pos);

        // Add on page change listener to detail image
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //noinspection ConstantConditions
                setTitle(data.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInf) {
        this.dh = new DatabaseHelper(this);

        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateContextMenu(menu, v, menuInf);
        getMenuInflater().inflate(R.menu.context_menu, menu);

        // Retrieve menu items
        MenuItem lockPass = menu.findItem(R.id.action_lock_pass);
        MenuItem lockLoc = menu.findItem(R.id.action_lock_loc);
        MenuItem unlock = menu.findItem(R.id.action_unlock);

        // Check if any locks are placed on photo
        boolean checkPinLock = dh.checkPinLock(data.get(pos).getOriginalUrl()) || dh.checkLocLock(data.get(pos).getOriginalUrl());

        // Enable and disable accordingly. Set visibility accordingly as well
        lockPass.setEnabled(!checkPinLock);
        lockLoc.setEnabled(!checkPinLock);
        unlock.setEnabled(checkPinLock);

        lockPass.setVisible(!checkPinLock);
        lockLoc.setVisible(!checkPinLock);
        unlock.setVisible(checkPinLock);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        this.dh = new DatabaseHelper(this);

        // Get id of the context menu item selected
        int id = item.getItemId();
        //Lock photo by password
        if (id == R.id.action_lock_pass) {
            // Start intent to choose replacement image
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, CHOOSE_IMAGE_REQUEST);
            return true;
        }
        //Lock photo by location
        if (id == R.id.action_lock_loc) {
            // Check network connection is available
            if(isNetworkAvailable(this)) {
                // Start activity to proceed with location lock
                Intent gps = new Intent(this, MapsActivity.class);
                startActivityForResult(gps, CHOOSE_LOCATION_REQUEST);
            }
            else {
                Toast.makeText(this, getString(R.string.InternetNotAvailable), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        //Unlock photo
        if (id == R.id.action_unlock) {
            // Check photo is locked by pin
            if (dh.checkPinLock(data.get(pos).getOriginalUrl())) {
                // Check network connection is available
                if(isNetworkAvailable(this)) {
                    // Fetch pin for given photo
                    final String UUID = dh.getPinUUID(data.get(pos).getOriginalUrl());
                    // Run asynchronous thread to send email to user with retrieved pin
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            String email = settings.getString(EMAIL, "");
                            try {
                                GmailSender sender = new GmailSender(getString(R.string.GalleryEmail),
                                        getString(R.string.GalleryPass));
                                sender.sendMail(getString(R.string.EmailSubject), getString(R.string.EmailMessage) + UUID,
                                        getString(R.string.GalleryEmail), email);
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                            }
                        }
                    }).start();

                    // Set up layout inflater to create alert dialog
                    LayoutInflater layoutInflater = LayoutInflater.from(this);

                    // Get view from inflating with pin unlock layout
                    View pinUnlockView = layoutInflater.inflate(R.layout.pin_unlock, null);

                    // Create alert dialog builder
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    // Set with pin unlock view
                    alertDialogBuilder.setView(pinUnlockView);

                    // Grab input from edit text field
                    final EditText input = (EditText) pinUnlockView.findViewById(R.id.pin_input);

                    alertDialogBuilder
                            // Can't cancel outside of dialog
                            .setCancelable(false)
                            // Set on click listener for okay button
                            .setPositiveButton(getString(R.string.Okay), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Check pin from edit text field is equal to pin retrieved from db
                                    if (input.getText().toString().trim().equals(UUID)) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.SuccessUnlocked), Toast.LENGTH_LONG).show();
                                        // Unlock the photo
                                        dh.unlockPin(data.get(pos).getOriginalUrl());
                                        // Make new intent to return to main activity screen
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("result", true);
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        // Exit activity
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.FailUnlockPin), Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.Cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,	int id) {
                                            // Cancel out of dialog
                                            dialog.cancel();
                                        }
                                    });
                    // Create and show dialog
                    alertDialogBuilder.create().show();
                }
                else {
                    Toast.makeText(this, getString(R.string.InternetNotAvailable), Toast.LENGTH_LONG).show();
                }
            }
            else {
                // Check location is on
                checkLocationOn();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static boolean isNetworkAvailable(Context content) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) content.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent iData) {
        super.onActivityResult(requestCode, resultCode, iData);
        switch (requestCode) {
            // Case where image request was made
            case(CHOOSE_IMAGE_REQUEST): {
                // Check it was a success
                if(resultCode != RESULT_CANCELED) {
                    // Get selected replacement photo and enable pin lock on photo
                    Uri selectedImageUri = iData.getData();
                    dh.enablePinLock(data.get(pos).getOriginalUrl(), getPath(selectedImageUri));
                    // Create intent to return to main screen and finish activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("result", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
                break;
            }
            // Case where location request was given back
            case(CHOOSE_LOCATION_REQUEST): {
                // Check if result was success
                if(resultCode != RESULT_CANCELED) {
                    // Check network connection is available
                    if(isNetworkAvailable(this)) {
                        // Get coordinates, radius, and selected replacement image
                        String coordinates = iData.getStringExtra("coordinates");
                        String radius = Integer.toString(iData.getIntExtra("radius", 100));
                        Uri selectedImageUri = Uri.parse(iData.getStringExtra("replacement"));
                        // Enable location lock for photo
                        dh.enableLocationLock(data.get(pos).getOriginalUrl(), coordinates, getPath(selectedImageUri), radius);
                        // Create intent to return to main screen and finish activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("result", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                    else {
                        Toast.makeText(this, getString(R.string.InternetNotAvailable), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            // Return from location settings activity
            case(ENABLE_LOCATION_REQUEST): {
                // Check location again
                checkLocationOn();
                break;
            }
        }
    }

    private void checkLocationOn() {
        int locationMode = 0;
        String locationProviders;

        // Check sdk version of device is kit kat and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                // Check location mode is on or off
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            // If off, direct to gps dialog to enable it
            if(locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                enableGPSDialog();
            }
            else {
                // Otherwise, enable current location
                enableMyLocation();
            }
        } else{
            // Get location providers
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(!TextUtils.isEmpty(locationProviders)) {
                // No location providers, so need to turn on gps
                enableGPSDialog();
            }
            else {
                // Location is on, so go to enable location
                enableMyLocation();
            }
        }
    }

    private void enableGPSDialog() {
        // notify user that gps is not turned on
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.gps_not_enabled_title));
        dialog.setMessage(getString(R.string.gps_not_enabled_message));
        dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // Send user to settings to turn on location
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(myIntent, ENABLE_LOCATION_REQUEST);
            }
        });
        dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialog.show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // Check permission is granted for access to fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Update current location when location update is received
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

            // If location was found, bookmark it
            if (location != null) {
                myLocation = location;
            }

            // Ask for location updates every 2.4 seconds
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2400, 0, locationListener);

            // Proceed to find current location and check unlock status
            findLocationAndCheck();
        }
    }

    public void findLocationAndCheck() {
        // Get original photo path of specified image
        final String path = data.get(pos).getOriginalUrl();
        // Set up progress dialog for finding location and further unlocking photo
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.PleaseWaitTitle));
        progress.setMessage(getString(R.string.SearchingLocation));
        progress.setCancelable(false);
        progress.show();
        final Handler handler = new Handler();
        // Set up handler to delay by 5 seconds for a location lock on
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                progress.dismiss();
                // Check if current location was found
                if (myLocation != null) {
                    // Fetch coordinates from current location
                    String latitude = Double.toString(myLocation.getLatitude());
                    String longitude = Double.toString(myLocation.getLongitude());
                    String coordinates = latitude + ":" + longitude;
                    // Check if unlocking by location was success
                    boolean result = dh.checkUnlockLocation(coordinates, path);
                    if (result) {
                        Toast.makeText(getApplicationContext(), getString(R.string.SuccessUnlocked), Toast.LENGTH_LONG).show();
                        // Successful unlock. Return to main activity screen
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("result", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.FailUnlockLoc), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.CantFindLoc), Toast.LENGTH_LONG).show();
                }
            }
        }, 5000);
    }

    /**
     * Helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // Check empty URI is given
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        // Make query for the given uri
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public ArrayList<ImageModel> data = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<ImageModel> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, data.get(position).getName(), data.get(position).getUrl());
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return data.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return data.get(position).getName();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        String name, url;
        int pos;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_IMG_TITLE = "image_title";
        private static final String ARG_IMG_URL = "image_url";

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            this.pos = args.getInt(ARG_SECTION_NUMBER);
            this.name = args.getString(ARG_IMG_TITLE);
            this.url = args.getString(ARG_IMG_URL);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String name, String url) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_IMG_TITLE, name);
            args.putString(ARG_IMG_URL, url);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            // Save url of fragment
            savedInstanceState.putString("url", this.url);
            Log.i(TAG, "onSaveInstanceState");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView() called");

            // If url is not found, then update from savedInstanceState
            if(this.url == null) {
                this.url = savedInstanceState.getString("url");
            }

            // Create and inflate view for fragment detail image
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            // Register context menu for image view
            registerForContextMenu(imageView);

            Glide.with(getActivity()).load(url).thumbnail(0.1f).into(imageView);

            return rootView;
        }

    }
}
