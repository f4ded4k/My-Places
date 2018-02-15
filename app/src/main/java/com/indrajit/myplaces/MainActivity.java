package com.indrajit.myplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends SQLActivity implements SwipeRefreshLayout.OnRefreshListener {

    private FloatingActionButton extendButton, mapButton, deleteButton;
    private RecyclerView recyclerView;
    private Snackbar snackbar;
    private boolean expand;
    private LocationAdapter locationAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.appBar));

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = findViewById(R.id.recyclerView);
        locationAdapter = new LocationAdapter(this, new LocationAdapter.onRespondListener() {
            @Override
            public void _onClickAddNewPlace() {
                switchActivity();
            }

            @Override
            public void _onClickLocation(int position) {

                switchActivityToMarker(position);
            }

            @Override
            public void _onChangeFav(LatLng latLng, boolean checked) {

                SQLUtils.changeFav(database, latLng, checked);
                LocationAdapter.myLocations = RecyclerDataFetcher.populateList(MainActivity.this);
            }

            @Override
            public void _onClickMenuItems(MenuItem item, int i, View view) {

                switch(item.getItemId()){
                    case R.id.itemEdit:
                        getEditor(i);
                        break;
                    case R.id.itemDelete:
                        deleteLocationConfirmation(i);
                        break;
                    case R.id.itemDirection:
                        goToDirection(i);
                        break;
                    case R.id.itemGmap:
                        goToGmap(i);
                        break;
                    case R.id.itemShare:
                        startShareIntent(i);
                        break;
                }
            }
        }, RecyclerDataFetcher.populateList(this));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(locationAdapter);

        initiateGesture();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!recyclerView.canScrollVertically(1) && recyclerView.computeVerticalScrollOffset() != 0){

                    if(deleteButton.getVisibility() == View.VISIBLE) {

                        deleteButton.setVisibility(View.INVISIBLE);
                        mapButton.setVisibility(View.INVISIBLE);
                        extendButton.animate().scaleX(0).scaleY(0).setDuration(300).setInterpolator(new AccelerateInterpolator()).start();
                    }
                } else {

                    if (deleteButton.getVisibility() == View.INVISIBLE) {

                        mapButton.setVisibility(View.VISIBLE);
                        extendButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                        extendButton.animate().scaleX(1).scaleY(1).setDuration(300).setInterpolator(new AccelerateInterpolator()).start();
                    }
                }
            }
        });
        expand=false;

        extendButton = findViewById(R.id.extendButton);
        mapButton = findViewById(R.id.mapButton);
        deleteButton = findViewById(R.id.deleteButton);
        snackbar = Snackbar.make(recyclerView, "Press back again to exit.", Snackbar.LENGTH_LONG);

        getInternetPermission();
    }

    @Override
    public void onBackPressed() {

        if(snackbar.isShown()){
            finish();

        } else if(expand){
            onClickExtend(null);
        } else{
            snackbar.show();
        }
    }

    private void switchActivity(){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void switchActivityToMarker(int i){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("latlng_position", i);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    public void onClickDelete(View v){

        Snackbar.make(recyclerView, "Are you sure? ", Snackbar.LENGTH_LONG)
                .setAction("YES, remove all", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        resetTable();
                        LocationAdapter.myLocations = RecyclerDataFetcher.populateList(MainActivity.this);
                        locationAdapter.notifyDataSetChanged();
                        onClickExtend(null);

                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    private void deleteLocationConfirmation(final int i){

        new AlertDialog.Builder(this)
                .setMessage("Are you sure?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {

                        locationAdapter.notifyItemChanged(i);

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {

                        AlertDialog dialog = showLoader(MainActivity.this, R.layout.activity_delete);
                        Object[] params = new Object[]{ LocationAdapter.myLocations.get(i).getLat(), LocationAdapter.myLocations.get(i).getLon()};
                        String sql = "DELETE FROM locations WHERE lat=? AND lon=?";
                        database.execSQL(sql, params);
                        LocationAdapter.myLocations.remove(i);
                        locationAdapter.notifyItemRemoved(i);

                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                        locationAdapter.notifyItemChanged(i);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5){

            recreate();
        }
    }

    public void onClickMap(View view) {

        switchActivity();
    }

    private void goToDirection(int i) {

        double lat = LocationAdapter.myLocations.get(i).getLat(), lon = LocationAdapter.myLocations.get(i).getLon();

        String str = "https://www.google.com/maps/dir/?api=1&" + "&destination=" + String.valueOf(lat) + "," + String.valueOf(lon);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        startActivity(browserIntent);

    }

    private void goToGmap(int i){

        double lat = LocationAdapter.myLocations.get(i).getLat(), lon = LocationAdapter.myLocations.get(i).getLon();

        String str = "https://www.google.com/maps/search/?api=1" + "&query=" + String.valueOf(lat) + "," + String.valueOf(lon);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        startActivity(browserIntent);
    }
     private void getInternetPermission(){

        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 3);
        }
     }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 3){

            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 3);
            }
        }
    }

    private void animateFAB() {

        SpringAnimation rotateExtend, yDelete, yMap, aDelete, aMap;
        rotateExtend = new SpringAnimation(extendButton, DynamicAnimation.ROTATION);
        yDelete = new SpringAnimation(deleteButton, DynamicAnimation.TRANSLATION_Y);
        yMap = new SpringAnimation(mapButton, DynamicAnimation.TRANSLATION_Y);
        aDelete = new SpringAnimation(deleteButton, DynamicAnimation.ALPHA);
        aMap = new SpringAnimation(mapButton, DynamicAnimation.ALPHA);
        deleteButton.setClickable(false);
        mapButton.setClickable(false);

        if (!expand) {

            expand = !expand;
            rotateExtend.animateToFinalPosition(45);
            yDelete.animateToFinalPosition(-extendButton.getHeight() -30);
            yMap.animateToFinalPosition(-60 -extendButton.getHeight() -deleteButton.getHeight());
            aDelete.animateToFinalPosition(1);
            aMap.animateToFinalPosition(1);
            rotateExtend.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                    extendButton.setClickable(true);
                    deleteButton.setClickable(true);
                    mapButton.setClickable(true);
                }
            });

        }else {

            expand = !expand;
            rotateExtend.animateToFinalPosition(0);
            yDelete.animateToFinalPosition(0);
            yMap.animateToFinalPosition(0);
            aDelete.animateToFinalPosition(0);
            aMap.animateToFinalPosition(0);
            rotateExtend.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                    extendButton.setClickable(true);
                    deleteButton.setClickable(true);
                    mapButton.setClickable(true);
                }
            });
        }
    }
    public void onClickExtend(View view) {

        animateFAB();
    }

    private void getEditor(int i){

        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        intent.putExtra("position", i);
        startActivity(intent);
    }

    private void startShareIntent(int i){

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        sharingIntent.setType("text/plain");
        String uri = "https://www.google.com/maps/search/?api=1" +
                "&query=" +
                String.valueOf(LocationAdapter.myLocations.get(i).getLat()) +
                "," +
                String.valueOf(LocationAdapter.myLocations.get(i).getLon());
        String shareSub = LocationAdapter.myLocations.get(i).getFullname();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareSub + "\n" + uri);
        startActivity(Intent.createChooser(sharingIntent, "Share location using..."));
    }

    private void initiateGesture(){

        ItemTouchHelper.SimpleCallback myCallback = new RecyclerViewMotionHelper(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, new RecyclerViewMotionHelper.RecyclerViewMotionListener() {
            @Override
            public void onSwipe(RecyclerView.ViewHolder holder, int direction) {

                if(direction == ItemTouchHelper.LEFT) {
                    deleteLocationConfirmation(holder.getAdapterPosition());
                }
                else{
                    getEditor(holder.getAdapterPosition());
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(myCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.refreshItem){

            swipeRefreshLayout.setRefreshing(true);

            recreateRecyclerView();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {


        recreateRecyclerView();
    }

    private void recreateRecyclerView() {

        LocationAdapter.myLocations = RecyclerDataFetcher.populateList(this);

        locationAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}

