package com.grafixartist.gallery;

import android.app.Activity;
import android.content.Intent;
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

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GalleryAdapter mAdapter;
    RecyclerView mRecyclerView;

    private static final String TAG = "MainActivity";

    ArrayList<ImageModel> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);

        ArrayList<String> IMGS = getImagesPath(MainActivity.this);

        for (int i = 0; i < IMGS.size(); i++) {

            ImageModel imageModel = new ImageModel();
            imageModel.setName("Image " + i);
            imageModel.setUrl(IMGS.get(i));
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

    public static ArrayList<String> getImagesPath(Activity activity) {
        Uri uri;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        Cursor cursor;
        int column_index_data;
        int title_index_data;
        int size_index_data;
        String PathOfImage;
        String TitleOfImage;
        String SizeOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                               MediaStore.MediaColumns.DISPLAY_NAME,
                               MediaStore.MediaColumns.DATE_ADDED,
                                // ADDED THIS LINE TO GET DATA FROM SIZE COLUMN
                                MediaStore.MediaColumns.SIZE
                              };

        cursor = activity.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        // THIS IS WHERE INDEX OF TITLE AND SIZE COLUMNS ARE FOUND
        title_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        size_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);

        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data);

            // THIS IS WHERE TITLE AND SIZE ARE RETRIEVED
            TitleOfImage = cursor.getString(title_index_data);
            SizeOfImage = cursor.getString(size_index_data);

            listOfAllImages.add(PathOfImage);
        }
        //close(cursor);
        return listOfAllImages;
    }

}
