package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;

import java.util.ArrayList;
import java.util.List;

public class ReferenceReadingsAdapter extends RecyclerView.Adapter<ReferenceReadingsAdapter.ViewHolder> {
    private List<AccessPoint> readings = new ArrayList<>();

    @Override
    public ReferenceReadingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reference_reading, parent, false);
        ReferenceReadingsAdapter.ViewHolder vh = new ReferenceReadingsAdapter.ViewHolder(linearLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bssid.setText(readings.get(position).getBssid());
        holder.ssid.setText(readings.get(position).getSsid());
        holder.level.setText(String.valueOf(readings.get(position).getMeanRss()));
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView bssid, ssid, level;

        public ViewHolder(LinearLayout v) {
            super(v);
            bssid = v.findViewById(R.id.wifi_bssid);
            ssid = v.findViewById(R.id.wifi_ssid);
            level = v.findViewById(R.id.wifi_level);
        }
    }

    public List<AccessPoint> getReadings() {
        return readings;
    }

    public void addAP(AccessPoint accessPoint) {
        readings.add(accessPoint);
    }

    public void setReadings(List<AccessPoint> readings) {
        this.readings = readings;
    }
}
