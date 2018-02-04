package com.indrajit.myplaces;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder>{

    private LayoutInflater inflater;
    private ArrayList<MyLocation> myLocations;

    public LocationAdapter(Context context) {

        this.inflater = LayoutInflater.from(context);
        this.myLocations = RecyclerDataFetcher.populateList(context);
    }

    @Override
    public LocationAdapter.LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.location_viewholder, parent);

        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LocationAdapter.LocationViewHolder holder, int position) {

        holder.fullname.setText(myLocations.get(position).getFullname());
        holder.nickname.setText(myLocations.get(position).getNickname());
        holder.fav.setChecked(myLocations.get(position).getFav() == 1);
    }

    @Override
    public int getItemCount() {
        return myLocations.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder{

        TextView fullname,nickname;
        Switch fav;

        private LocationViewHolder(View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.fullname);
            nickname = itemView.findViewById(R.id.nickname);
            fav = itemView.findViewById(R.id.favSwitch);
        }
    }
}
