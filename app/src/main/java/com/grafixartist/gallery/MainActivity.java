package com.grafixartist.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GalleryAdapter mAdapter;

    RecyclerView mRecyclerView;

    private static int orderByIndex = 0;

    private static final int LOCK_UNLOCK_ACTION = 1;

    private final String PREFS_NAME = "MyPrefsFile";

    ArrayList<ImageModel> data = new ArrayList<>();

    private DatabaseHelper dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up content view
        setContentView(R.layout.activity_main);

        // Set up image models and tool bar
        setUpImageModels(MainActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);


        // Create adapter
        mAdapter = new GalleryAdapter(MainActivity.this, data);
        mRecyclerView.setAdapter(mAdapter);

        // Add on touch listener to details activity
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putParcelableArrayListExtra("data", data);
                        intent.putExtra("pos", position);
                        startActivityForResult(intent, LOCK_UNLOCK_ACTION);
                    }
                }));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent iData) {
        super.onActivityResult(requestCode, resultCode, iData);
        switch (requestCode) {
            // Check if lock unlock action was used
            case(LOCK_UNLOCK_ACTION): {
                if(resultCode != RESULT_CANCELED && iData.getBooleanExtra("result", false)) {
                    // If so, restart activity to reflect changes
                    finish();
                    startActivity(getIntent());
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If settings button pressed, pull up AppPreferences class
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, AppPreferences.class);
            startActivity(i);
        }
        // If sort button pressed, pull up alert dialog for further sort
        else if (id == R.id.action_sort) {
            final CharSequence[] items = {"Date", "Name", "Size"};

            // Build alert dialog with Date, Name, and Size and menu items
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sort By");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    // Save sort by preference
                    orderByIndex = item;
                    //Refresh view
                    finish();
                    startActivity(getIntent());
                    Toast.makeText(getApplicationContext(), "Sorted by " + items[item], Toast.LENGTH_SHORT).show();
                }
            });
            // Create and show alert
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        // If logout pressed, update shared preferences file for first time login and exit to login class
        else if (id == R.id.action_logout) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            settings.edit().putBoolean("first_time_login", true).apply();
            Intent i = new Intent(this, Login.class);
            startActivity(i);
            finish();
        }

        return id == R.id.action_logout || super.onOptionsItemSelected(item);
    }

    public void setUpImageModels(Activity activity) {
        Uri uri;
        ArrayList<Image> listOfAllImages = new ArrayList<>();
        ArrayList<Image> listOfOrigImages = new ArrayList<>();
        int column_index_data, title_index_data, size_index_data, date_index_data;
        String imgPath, imgSize, imgName, imgDate;
        this.dh = new DatabaseHelper(this);
        // Grab URI for external content
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Hold projections for db query
        String[] projection = {MediaStore.MediaColumns.DATA,
                               MediaStore.MediaColumns.DISPLAY_NAME,
                               MediaStore.MediaColumns.DATE_ADDED,
                                // ADDED THIS LINE TO GET DATA FROM SIZE COLUMN
                                MediaStore.MediaColumns.SIZE
                              };

        // Hold order by settings
        String[] orderBy = {MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE};

        // Make query to retrieve all images ordered by preferred sort method
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, orderBy[orderByIndex] + " DESC");

        // Get image properties
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        title_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        size_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
        date_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

        // Check cursor is not null
        while (cursor.moveToNext()) {
            // Get path, name, size, and data of path file
            imgPath = cursor.getString(column_index_data);
            imgName = cursor.getString(title_index_data);
            imgSize = cursor.getString(size_index_data);
            imgDate = cursor.getString(date_index_data);

            //Insert into db if not exists
            dh.insertPhoto(imgPath, imgName, imgDate, imgSize);
            dh.insertPin(imgPath);
            dh.insertLoc(imgPath);

            // Set up and add image from properties retrieved
            Image img = new Image(imgPath, imgName, imgSize, imgDate);
            listOfOrigImages.add(img);

            //Check if locked by password. If so, retrieve replacement photo
            if(dh.checkPinLock(imgPath)){
                img = dh.getReplacementPhoto(imgPath, 1);
            }
            // Check if locked by location. If so, retrieve replacement photo
            else if(dh.checkLocLock(imgPath)) {
                img = dh.getReplacementPhoto(imgPath, 2);
            }
            // Add to images
            listOfAllImages.add(img);
        }
        cursor.close();

        // Loop through images and create image models
        for (int i = 0; i < listOfAllImages.size(); i++) {
            ImageModel imageModel = new ImageModel();
            imageModel.setName(listOfAllImages.get(i).getName());
            imageModel.setUrl(listOfAllImages.get(i).getPath());
            imageModel.setOriginalUrl(listOfOrigImages.get(i).getPath());
            data.add(imageModel);
        }
    }

}
