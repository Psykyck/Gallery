package com.grafixartist.gallery;

/**
 * Created by Sean on 11/9/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppPreferences extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Opens settings menu
        addPreferencesFromResource(R.xml.adv_settings);
    }
}
