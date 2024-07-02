package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.R;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;

import io.realm.RealmResults;


public class ProjectsListAdapter extends RecyclerView.Adapter<ProjectsListAdapter.ViewHolder> {
    private RealmResults<IndoorProject> mDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public ViewHolder(LinearLayout v) {
            super(v);
            name = v.findViewById(R.id.tv_project_name);
        }
    }

    public ProjectsListAdapter(RealmResults<IndoorProject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ProjectsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_list, parent, false);
        ViewHolder vh = new ViewHolder(linearLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mDataset.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
