package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

public class WifiData implements Parcelable {
	private List<WifiDataNetwork> mNetworks;

	public WifiData() {
		mNetworks = new ArrayList<>();
	}

	public WifiData(Parcel in) {
		in.readTypedList(mNetworks, WifiDataNetwork.CREATOR);
	}

	public static final Creator<WifiData> CREATOR = new Creator<WifiData>() {
		public WifiData createFromParcel(Parcel in) {
			return new WifiData(in);
		}

		public WifiData[] newArray(int size) {
			return new WifiData[size];
		}
	};

	public void addNetworks(List<ScanResult> results) {
		mNetworks.clear();
		for (ScanResult result : results) {
			mNetworks.add(new WifiDataNetwork(result));
		}
		Collections.sort(mNetworks);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mNetworks);
	}

	@Override
	public String toString() {
		if (mNetworks == null || mNetworks.size() == 0)
			return "Empty data";
		else
			return mNetworks.size() + " networks data";
	}

	public List<WifiDataNetwork> getNetworks() {
		return mNetworks;
	}
}
