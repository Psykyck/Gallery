package com.grafixartist.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GalleryAdapter mAdapter;
    RecyclerView mRecyclerView;
    private static int x=0;

    private static final String TAG = "MainActivity";

    ArrayList<ImageModel> data = new ArrayList<>();

    private DatabaseHelper dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.dh = new DatabaseHelper(this);

        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

        ArrayList<Image> IMGS = getImagesPath(MainActivity.this);

        for (int i = 0; i < IMGS.size(); i++) {
            ImageModel imageModel = new ImageModel();
            imageModel.setName(IMGS.get(i).getName());
            imageModel.setUrl(IMGS.get(i).getPath());
            dh.insertPhoto(IMGS.get(i).getPath(), IMGS.get(i).getName(), IMGS.get(i).getDateTaken(), IMGS.get(i).getSize());
            data.add(imageModel);
        }

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
                        startActivity(intent);
                    }
                }));

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, AppPreferences.class);
            startActivity(i);
            finish();
        }
        if (id == R.id.action_sort) {
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
        }
        if(id == R.id.action_logout){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<Image> getImagesPath(Activity activity) {
        Uri uri;
        ArrayList<Image> listOfAllImages = new ArrayList<>();
        Cursor cursor;
        int column_index_data;
        int title_index_data;
        int size_index_data;
        int date_index_data;
        String imgPath;
        String imgSize;
        String imgName;
        String imgDate;
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
            //New img object
            Image img = new Image(imgPath, imgName, imgSize, imgDate);
            listOfAllImages.add(img);
        }
        cursor.close();
        return listOfAllImages;
    }

}
