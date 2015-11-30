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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GalleryAdapter mAdapter;

    RecyclerView mRecyclerView;

    private static int x = 0;

    private static final int LOCK_UNLOCK_ACTION = 1;

    private final String PREFS_NAME = "MyPrefsFile";

    ArrayList<ImageModel> data = new ArrayList<>();

    private DatabaseHelper dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpImageModels(MainActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);


        mAdapter = new GalleryAdapter(MainActivity.this, data);
        mRecyclerView.setAdapter(mAdapter);

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
            case(LOCK_UNLOCK_ACTION): {
                if(resultCode != RESULT_CANCELED && iData.getBooleanExtra("result", false)) {
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

        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, AppPreferences.class);
            startActivity(i);
        } else if (id == R.id.action_sort) {
            final CharSequence[] items = {"Date", "Name", "Size"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sort By");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    x = item;
                    //Refresh view
                    finish();
                    startActivity(getIntent());
                    Toast.makeText(getApplicationContext(), "Sorted by " + items[item], Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        } else if (id == R.id.action_logout) {
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
        Cursor cursor;
        int column_index_data, title_index_data, size_index_data, date_index_data;
        String imgPath, imgSize, imgName, imgDate;
        this.dh = new DatabaseHelper(this);
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                               MediaStore.MediaColumns.DISPLAY_NAME,
                               MediaStore.MediaColumns.DATE_ADDED,
                                // ADDED THIS LINE TO GET DATA FROM SIZE COLUMN
                                MediaStore.MediaColumns.SIZE
                              };

        String[] orderBy = {MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE};

        cursor = activity.getContentResolver().query(uri, projection, null, null, orderBy[x] + " DESC");

        //Get image properties
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        title_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        size_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
        date_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

        while (cursor.moveToNext()) {
            imgPath = cursor.getString(column_index_data);
            imgName = cursor.getString(title_index_data);
            imgSize = cursor.getString(size_index_data);
            imgDate = cursor.getString(date_index_data);
            //Insert into db if not exists
            dh.insertPhoto(imgPath, imgName, imgDate, imgSize);
            dh.insertPin(imgPath);
            dh.insertLoc(imgPath);
            Image img = new Image(imgPath, imgName, imgSize, imgDate);
            listOfOrigImages.add(img);
            //Check if locked by password
            if(dh.checkPinLock(imgPath)){
                img = dh.getReplacementPhoto(imgPath, 1);
            }
            else if(dh.checkLocLock(imgPath)) {
                img = dh.getReplacementPhoto(imgPath, 2);
            }
            listOfAllImages.add(img);
        }
        cursor.close();

        for (int i = 0; i < listOfAllImages.size(); i++) {
            ImageModel imageModel = new ImageModel();
            imageModel.setName(listOfAllImages.get(i).getName());
            imageModel.setUrl(listOfAllImages.get(i).getPath());
            imageModel.setOriginalUrl(listOfOrigImages.get(i).getPath());
            data.add(imageModel);
        }
    }

}
