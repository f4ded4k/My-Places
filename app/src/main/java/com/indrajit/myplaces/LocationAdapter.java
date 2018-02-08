package com.indrajit.myplaces;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.GenericViewHolder>{

    private LayoutInflater inflater;
    private ArrayList<MyLocation> myLocations;
    //private Context context;
    private onRespondListener response;

    interface onRespondListener {

        void _onClickAddNewPlace();
    }

    LocationAdapter(Context context, onRespondListener response) {

        this.inflater = LayoutInflater.from(context);
        this.myLocations = RecyclerDataFetcher.populateList(context);
        this.response = response;
        //this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if( position == 0){
            return 0;

        } else{

            return 1;
        }
    }

    @Override
    public LocationAdapter.GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == 1) {
            View v = inflater.inflate(R.layout.location_viewholder, parent, false);

            return new LocationViewHolder(v);
        } else{

            View v = inflater.inflate(R.layout.add_viewholder, parent, false);
            return new AddViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(LocationAdapter.GenericViewHolder holder, int position) {

        if(holder.getItemViewType() == 1) {

            ((LocationViewHolder) holder).fullname.setText(myLocations.get(position).getFullname());
            ((LocationViewHolder) holder).nickname.setText(myLocations.get(position).getNickname());
            ((LocationViewHolder) holder).fav.setChecked(myLocations.get(position).getFav() == 1);
        }
    }

    @Override
    public int getItemCount() {
        return myLocations.size();
    }

    class GenericViewHolder extends RecyclerView.ViewHolder{


        GenericViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class LocationViewHolder extends GenericViewHolder{

        private TextView fullname,nickname;
        private Switch fav;

        private LocationViewHolder(View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.fullname);
            nickname = itemView.findViewById(R.id.nickname);
            fav = itemView.findViewById(R.id.switchFav);
        }
    }

    private class AddViewHolder extends GenericViewHolder{

        CardView cardView;

        private AddViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.addViewHolderCardView);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    response._onClickAddNewPlace();
                }
            });
        }
    }
}
