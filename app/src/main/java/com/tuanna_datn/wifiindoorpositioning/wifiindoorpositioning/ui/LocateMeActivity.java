package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter.NearbyReadingsAdapter;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.core.Algorithms;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.core.WifiService;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.LocDistance;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.LocationWithNearbyPlaces;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.WifiData;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.utils.Utils;

import io.realm.Realm;

public class LocateMeActivity extends AppCompatActivity {

    private WifiData mWifiData;
    private Algorithms algorithms = new Algorithms();
    private String projectId, defaultAlgo;
    private IndoorProject project;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiServiceIntent;
    private TextView tvLocation, tvNearestLocation, tvDistance;
    private RecyclerView rvPoints;
    private LinearLayoutManager layoutManager;
    private NearbyReadingsAdapter readingsAdapter = new NearbyReadingsAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiData = null;

        // set receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(AppContants.INTENT_FILTER));

        // launch WiFi service
        wifiServiceIntent = new Intent(this, WifiService.class);
        startService(wifiServiceIntent);

        // recover retained object
        mWifiData = (WifiData) getLastNonConfigurationInstance();

        // set layout
        setContentView(R.layout.activity_locate_me);
        initUI();

        defaultAlgo = Utils.getDefaultAlgo(this);
        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show();
            this.finish();
        }
        Realm realm = Realm.getDefaultInstance();
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
        Log.v("LocateMeActivity", "onCreate");
    }

    private void initUI() {
        layoutManager = new LinearLayoutManager(this);
        tvLocation = findViewById(R.id.tv_location);
        tvNearestLocation = findViewById(R.id.tv_nearest_location);
        tvDistance = findViewById(R.id.tv_distance_origin);
        rvPoints = findViewById(R.id.rv_nearby_points);
        rvPoints.setLayoutManager(layoutManager);
        rvPoints.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvPoints.setAdapter(readingsAdapter);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LocateMeActivity", "MainActivityReceiver");
            mWifiData = (WifiData) intent.getParcelableExtra(AppContants.WIFI_DATA);
            Log.e("Bello","mWifiData: " + mWifiData);
            if (mWifiData != null) {
                LocationWithNearbyPlaces loc = Algorithms.processingAlgorithms(mWifiData.getNetworks(), project, Integer.parseInt(defaultAlgo));
                Log.e("Bello","loc:" + loc);
                if (loc == null) {
                    tvLocation.setText("Tạo độ: NA\nNote:Hãy bật wifi và vị trí để tiến hành xác định vị trí");
                } else {
                    String locationValue = Utils.reduceDecimalPlaces(loc.getLocation());
                    Log.v("LocateMeActivity", "locationValue:" + locationValue);
                    tvLocation.setText("Toạ độ: " + locationValue);
                    String theDistancefromOrigin = Utils.getTheDistancefromOrigin(loc.getLocation());
                    tvDistance.setText("Khoảng cach từ stage area là: " + theDistancefromOrigin + "m");
                    LocDistance theNearestPoint = Utils.getTheNearestPoint(loc);
                    if (theNearestPoint != null) {
                        tvNearestLocation.setText("Bạn đang gần với: " + theNearestPoint.getName());
                    }
                    readingsAdapter.setReadings(loc.getPlaces());
                    readingsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiServiceIntent);
    }
}
