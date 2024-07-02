package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter.WifiResultsAdapter;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SearchWifiAccessPointActivity extends AppCompatActivity implements View.OnClickListener,
        RecyclerItemClickListener.OnItemClickListener {

    private String TAG = "SearchWifiAccessPointActivity";
    private RecyclerView rvWifis;
    private RecyclerView.LayoutManager layoutManager;

    private TextView tvWaiting;
    private WifiManager mainWifi;
    private WifiListReceiver receiverWifi;
    private final Handler handler = new Handler();
    private Button btnRefrsh;
    private List<ScanResult> results = new ArrayList<>();
    private WifiResultsAdapter wifiResultsAdapter = new WifiResultsAdapter();
    private boolean wifiWasEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seach_wifis);
        initUI();
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Log.e("Bello","mainWifi: " + mainWifi.isWifiEnabled());
        receiverWifi = new WifiListReceiver();

        wifiWasEnabled = mainWifi.isWifiEnabled();
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
        layoutManager = new LinearLayoutManager(this);
        rvWifis.setLayoutManager(layoutManager);
        rvWifis.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        rvWifis.setItemAnimator(new DefaultItemAnimator());
        rvWifis.setAdapter(wifiResultsAdapter);
        rvWifis.addOnItemTouchListener(new RecyclerItemClickListener(this, rvWifis, this));
    }

    public void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvWaiting.setVisibility(View.VISIBLE);
                Log.e("Bello","start scan");
                mainWifi.startScan();
//                refresh();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        Log.e("Bello","onResume");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        refresh();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    private void initUI() {
        tvWaiting = findViewById(R.id.tv_please_wait);
        rvWifis = findViewById(R.id.rv_wifis);
        btnRefrsh = findViewById(R.id.btn_wifi_refresh);
        btnRefrsh.setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        AccessPoint accessPoint = new AccessPoint();
        ScanResult scanResult = results.get(position);
        accessPoint.setId(UUID.randomUUID().toString());
        accessPoint.setMac_address(scanResult.BSSID);
        accessPoint.setSsid(scanResult.SSID);
        accessPoint.setBssid(scanResult.BSSID);
        accessPoint.setDescription(scanResult.capabilities);

        Intent intent = new Intent();
        intent.putExtra("accessPoint", accessPoint);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnRefrsh.getId()) {
            refresh();
        }
    }

    class WifiListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = mainWifi.getScanResults();
            Log.e("Bello","results: " + results);
            Collections.sort(results, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult scanResult, ScanResult scanResult2) {
                    if (scanResult.level > scanResult2.level) {
                        return -1;
                    } else if (scanResult.level < scanResult2.level) {
                        return 1;
                    }
                    return 0;
                }
            });
            tvWaiting.setVisibility(View.GONE);
            wifiResultsAdapter.setResults(results);
            wifiResultsAdapter.notifyDataSetChanged();
            final int N = results.size();

            Log.v(TAG, "Wi-Fi Scan Results ... Count:" + N);
            for(int i=0; i < N; ++i) {
                Log.v(TAG, "  BSSID       =" + results.get(i).BSSID);
                Log.v(TAG, "  SSID        =" + results.get(i).SSID);
                Log.v(TAG, "  Capabilities=" + results.get(i).capabilities);
                Log.v(TAG, "  Frequency   =" + results.get(i).frequency);
                Log.v(TAG, "  Level       =" + results.get(i).level);
                Log.v(TAG, "---------------");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!wifiWasEnabled) {
            mainWifi.setWifiEnabled(false);
        }
    }
}
