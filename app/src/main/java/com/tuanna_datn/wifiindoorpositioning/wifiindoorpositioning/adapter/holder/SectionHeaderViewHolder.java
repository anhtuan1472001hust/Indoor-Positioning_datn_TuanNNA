package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;

public class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
    final TextView tvTitle;


    public SectionHeaderViewHolder(View headerView) {
        super(headerView);
        tvTitle = (TextView) headerView.findViewById(R.id.tv_section_name);
    }
}
