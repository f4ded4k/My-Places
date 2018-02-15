package com.indrajit.myplaces;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.GenericViewHolder>{

    private LayoutInflater inflater;
    static ArrayList<MyLocation> myLocations;
    private Context context;
    private onRespondListener response;
    private int last_position = -1;

    interface onRespondListener {

        void _onClickAddNewPlace();
        void _onClickLocation(int position);
        void _onChangeFav(LatLng latLng, boolean checked);
        void _onClickMenuItems(MenuItem menuItem, int i, View view);
    }

    LocationAdapter(Context context, onRespondListener response, ArrayList<MyLocation> myLocations) {

        this.inflater = LayoutInflater.from(context);
        LocationAdapter.myLocations = myLocations;
        this.response = response;
        this.context = context;
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

            startAnimation(position, ((LocationViewHolder) holder).itemView);
        } else if(holder.getItemViewType() == 0){

            startAnimation(position, ((AddViewHolder) holder).itemView);
        }
    }

    @Override
    public void onViewAttachedToWindow(GenericViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(GenericViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return myLocations.size();
    }

    private void startAnimation(int position, View view){

        if(position > last_position){

            Animation animator = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            view.startAnimation(animator);
            last_position = position;
        }
    }

    class GenericViewHolder extends RecyclerView.ViewHolder{

        GenericViewHolder(View itemView) {
            super(itemView);

            itemView.setHapticFeedbackEnabled(true);
        }

        private void clearAnimation(){
            itemView.clearAnimation();
        }
    }

    class LocationViewHolder extends GenericViewHolder{

        private TextView fullname,nickname;
        Switch fav;
        private View more_menu_view;
        View foreground;

        private LocationViewHolder(final View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.fullname);
            nickname = itemView.findViewById(R.id.nickname);
            fav = itemView.findViewById(R.id.switchFav);
            more_menu_view = itemView.findViewById(R.id.more_options);
            foreground = itemView.findViewById(R.id.locationCardView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    response._onClickLocation(getAdapterPosition());
                }
            });

            fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    response._onChangeFav(myLocations.get(getAdapterPosition()).getLatLng(), b);
                }
            });

            more_menu_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showMore(getAdapterPosition(), itemView, more_menu_view);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    showMore(getAdapterPosition(), itemView, more_menu_view);
                    return true;
                }
            });
        }
    }

    private void showMore(final int adapterposition, final View itemView,View anchorView) {

        PopupMenu menu = new PopupMenu(context, anchorView, Gravity.END);
        menu.inflate(R.menu.menu_main);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                response._onClickMenuItems(item, adapterposition, itemView);
                return true;
            }
        });
        menu.show();
    }

    private class AddViewHolder extends GenericViewHolder{

        private AddViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    response._onClickAddNewPlace();
                }
            });
        }
    }
}
