package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.core;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.WifiData;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;


public class  WifiService extends Service {

	private final String TAG = "WifiService";

	private WifiManager mWifiManager;
	private ScheduledFuture<?> scheduleReaderHandle;
	private ScheduledExecutorService mScheduler;
	private WifiData mWifiData;

	private long initialDelay = 0;
	private long periodReader = AppContants.FETCH_INTERVAL;

	@Override
	public void onCreate() {
		Log.e("Bello","WifiService onCreate");
		Log.d(TAG, "WifiService onCreate");
		mWifiData = new WifiData();
		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mScheduler = Executors.newScheduledThreadPool(1);

		scheduleReaderHandle = mScheduler.scheduleAtFixedRate(new ScheduleReader(), initialDelay, periodReader,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void onDestroy() {
		// stop read thread
		scheduleReaderHandle.cancel(true);
		mScheduler.shutdown();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	class ScheduleReader implements Runnable {
		@Override
		public void run() {
			if (mWifiManager.isWifiEnabled()) {
				// get networks
				List<ScanResult> mResults = mWifiManager.getScanResults();
				Log.e("Bello","New scan result: " + mResults);
				Log.d(TAG, "New scan result: (" + mResults.size() + ") networks found");
				mWifiData.addNetworks(mResults);
				Intent intent = new Intent(AppContants.INTENT_FILTER);
				intent.putExtra(AppContants.WIFI_DATA, mWifiData);
				LocalBroadcastManager.getInstance(WifiService.this).sendBroadcast(intent);
			}
		}
	}
}
