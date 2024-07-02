package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.ui.frags.PrefsFragment;


public class UnifiedNavigationActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();

    }
}
