package com.grafixartist.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static final String TAG = "DetailActivity";
    private static final int CHOOSE_IMAGE_REQUEST = 1;
    public ArrayList<ImageModel> data = new ArrayList<>();
    int pos;
    Toolbar toolbar;
    private DatabaseHelper dh;
    private String replacementPath;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
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
        if(dh.checkPinLock(data.get(pos).getUrl()) || dh.checkLocLock(data.get(pos).getUrl())){ //is already locked
            lockPass.setEnabled(false);
            lockLoc.setEnabled(false);
            unlock.setEnabled(true);
        } else {
            lockPass.setEnabled(true);
            lockLoc.setEnabled(true);
            unlock.setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        this.dh = new DatabaseHelper(this);

        int id = item.getItemId();
        //Lock photo by password
        if (id == R.id.action_lock_pass) {
            //assign replacement thumbnail
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, CHOOSE_IMAGE_REQUEST);
            return true;
        }
        //Lock photo by location
        if (id == R.id.action_lock_loc) {
            //assign replacement thumbnail
            //user inputs coordinates
            //user inputs radius
            //set lock status to locked by location
            return true;
        }
        //Unlock photo
        if (id == R.id.action_unlock) {
            //if locked by pass
                //pop-up asking for pass
                //grab pass from db, email to logged in user
                //if right pass
                    //replace thumbnail
                    //toast unlock success
                    //return to image
                //else
                    //toast wrong pass
            //if locked by loc
                //call gps service
                //check if user within coordinate radius
                //if right pass
                    //replace thumbnail
                    //toast unlock success
                //else
                    //toast wrong location

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent iData) {
        super.onActivityResult(requestCode, resultCode, iData);
        Uri selectedImageUri = iData.getData();
        dh.enablePinLock(data.get(pos).getUrl(), getPath(selectedImageUri));
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
        public void onStart() {
            super.onStart();
            Log.d(TAG, "onStart() called");
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            savedInstanceState.putString("url", this.url);
            Log.i(TAG, "onSaveInstanceState");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause() called");
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "onResume() called");
        }
        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "onStop() called");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "onDestroy() called");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView() called");

            if(this.url == null) {
                this.url = savedInstanceState.getString("url");
            }

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            registerForContextMenu(imageView);

            Glide.with(getActivity()).load(url).thumbnail(0.1f).into(imageView);

            return rootView;
        }

    }
}
