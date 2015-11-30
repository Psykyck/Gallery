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
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        data = getIntent().getParcelableArrayListExtra("data");
        pos = getIntent().getIntExtra("pos", 0);

        setTitle(data.get(pos).getName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pos);

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

        //menu.setHeaderTitle("Sort By");
        MenuItem lockPass = menu.findItem(R.id.action_lock_pass);
        MenuItem lockLoc = menu.findItem(R.id.action_lock_loc);
        MenuItem unlock = menu.findItem(R.id.action_unlock);

        boolean checkPinLock = dh.checkPinLock(data.get(pos).getOriginalUrl()) || dh.checkLocLock(data.get(pos).getOriginalUrl());

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

        int id = item.getItemId();
        //Lock photo by password
        if (id == R.id.action_lock_pass) {
            if(isNetworkAvailable(this)) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, CHOOSE_IMAGE_REQUEST);
            }
            else {
                Toast.makeText(this, getString(R.string.InternetNotAvailable), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        //Lock photo by location
        if (id == R.id.action_lock_loc) {
            if(isNetworkAvailable(this)) {
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
            if (dh.checkPinLock(data.get(pos).getOriginalUrl())) {
                if(isNetworkAvailable(this)) {
                    final String UUID = dh.getPinUUID(data.get(pos).getOriginalUrl());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            String email = settings.getString(EMAIL, "");
                            try {
                                GMailSender sender = new GMailSender(getString(R.string.GalleryEmail),
                                        getString(R.string.GalleryPass));
                                sender.sendMail(getString(R.string.EmailSubject), getString(R.string.EmailMessage) + UUID,
                                        getString(R.string.GalleryEmail), email);
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                            }
                        }
                    }).start();

                    LayoutInflater layoutInflater = LayoutInflater.from(this);

                    View pinUnlockView = layoutInflater.inflate(R.layout.pin_unlock, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    alertDialogBuilder.setView(pinUnlockView);

                    final EditText input = (EditText) pinUnlockView.findViewById(R.id.pin_input);

                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.Okay), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // get user input and set it to result
                                    if(input.getText().toString().trim().equals(UUID)) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.SuccessUnlocked), Toast.LENGTH_LONG).show();
                                        dh.unlockPin(data.get(pos).getOriginalUrl());
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("result", true);
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.FailUnlockPin), Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.Cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,	int id) {
                                            dialog.cancel();
                                        }
                                    });
                    alertDialogBuilder.create().show();
                }
            }
            else {
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
            case(CHOOSE_IMAGE_REQUEST): {
                if(resultCode != RESULT_CANCELED) {
                    Uri selectedImageUri = iData.getData();
                    dh.enablePinLock(data.get(pos).getOriginalUrl(), getPath(selectedImageUri));
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("result", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
                break;
            }
            case(CHOOSE_LOCATION_REQUEST): {
                if(resultCode != RESULT_CANCELED) {
                    if(isNetworkAvailable(this)) {
                        String coordinates = iData.getStringExtra("coordinates");
                        String radius = Integer.toString(iData.getIntExtra("radius", 100));
                        Uri selectedImageUri = Uri.parse(iData.getStringExtra("replacement"));
                        dh.enableLocationLock(data.get(pos).getOriginalUrl(), coordinates, getPath(selectedImageUri), radius);
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
            case(ENABLE_LOCATION_REQUEST): {
                checkLocationOn();
                break;
            }
        }
    }

    private void checkLocationOn() {
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
            else {
                enableMyLocation();
            }
        } else{
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(!TextUtils.isEmpty(locationProviders)) {
                enableGPSDialog();
            }
            else {
                enableMyLocation();
            }
        }
    }

    private void enableGPSDialog() {
        // notify user
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
                myLocation = location;
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2400, 0, locationListener);

            final String path = data.get(pos).getOriginalUrl();
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(getString(R.string.PleaseWaitTitle));
            progress.setMessage(getString(R.string.SearchingLocation));
            progress.setCancelable(false);
            progress.show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    progress.dismiss();
                    if (myLocation != null) {
                        String latitude = Double.toString(myLocation.getLatitude());
                        String longitude = Double.toString(myLocation.getLongitude());
                        String coordinates = latitude + ":" + longitude;
                        boolean result = dh.checkUnlockLocation(coordinates, path);
                        if (result) {
                            Toast.makeText(getApplicationContext(), getString(R.string.SuccessUnlockLocTitle), Toast.LENGTH_LONG).show();
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
    }

/**
 * helper to retrieve the path of an image URI
 */
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
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

        private static View a;

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
            savedInstanceState.putString("url", this.url);
            Log.i(TAG, "onSaveInstanceState");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView() called");

            if(this.url == null) {
                this.url = savedInstanceState.getString("url");
            }

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            a = rootView;

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            registerForContextMenu(imageView);

            Glide.with(getActivity()).load(url).thumbnail(0.1f).into(imageView);

            return rootView;
        }

    }
}
