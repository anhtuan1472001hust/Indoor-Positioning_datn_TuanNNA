package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;


public class PointViewHolder extends RecyclerView.ViewHolder {
    final TextView tvIdentifier, tvIdentifier2, tvPointX, tvPointY;

    public PointViewHolder(View itemView) {
        super(itemView);
        tvIdentifier = itemView.findViewById(R.id.point_identifier);
        tvIdentifier2 = itemView.findViewById(R.id.point_identifier2);
        tvPointX = itemView.findViewById(R.id.point_x);
        tvPointY = itemView.findViewById(R.id.point_y);

    }
}
