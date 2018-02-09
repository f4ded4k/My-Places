package com.indrajit.myplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends SQLActivity {

    private FloatingActionButton extendButton, mapButton, deleteButton;
    private ArrayAdapter<String> adapter;
    private ConstraintLayout mainLayout;
    private Snackbar snackbar;
    private int global_i;
    private boolean expand;
    private RecyclerView recyclerView;
    private ListView listView;
    private LocationAdapter locationAdapter;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listView) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_main, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.toggleView:
                if(recyclerView.getVisibility() == View.INVISIBLE && listView.getVisibility() == View.VISIBLE){

                    recyclerView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                    item.setTitle("Show as cards");
                } else if(recyclerView.getVisibility() == View.VISIBLE && listView.getVisibility() == View.INVISIBLE){

                    recyclerView.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.VISIBLE);
                    item.setTitle("Show as list");
                }
                return true;
            case R.id.exit:
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
            }

            @Override
            public void _onClickMenuItems(MenuItem item, int i) {

                global_i = i;
                switch(item.getItemId()){
                    case R.id.itemEdit:
                        openEditor();
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
                }
            }
        }, RecyclerDataFetcher.populateList(this));
        //recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(locationAdapter);
        expand=false;

        extendButton = findViewById(R.id.extendButton);
        mapButton = findViewById(R.id.mapButton);
        deleteButton = findViewById(R.id.deleteButton);
        listView = findViewById(R.id.listView);
        registerForContextMenu(listView);
        mainLayout = findViewById(R.id.mainlayout);
        snackbar = Snackbar.make(mainLayout, "Press back again to exit.", Snackbar.LENGTH_LONG);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationlist);
        adapter.setNotifyOnChange(true);

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                global_i = i;
                return i == 0;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(i == 0){
                    switchActivity();
                }

                else{

                    switchActivityToMarker(i);
                }
            }
        });

        extendButton.setAlpha((float) 0.0);
        extendButton.animate().alpha((float) 1.0).setDuration(800).start();

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
        intent.putExtra("g_i", i);
        startActivity(intent);
        finish();
    }

    public void onClickDelete(View v){

        Snackbar.make(mainLayout, "Are you sure? ", Snackbar.LENGTH_LONG)
                .setAction("YES, remove all", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        resetTable();
                        locationlist.clear();
                        updateEntireList();
                        adapter.notifyDataSetChanged();
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
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {

                        AlertDialog dialog = showLoader(MainActivity.this, R.layout.activity_delete);
                        Object[] params = new Object[]{ LocationAdapter.myLocations.get(i).getLat(), LocationAdapter.myLocations.get(i).getLon()};
                        locationlist.remove(global_i);
                        String sql = "DELETE FROM locations WHERE lat=? AND lon=?";
                        database.execSQL(sql, params);
                        LocationAdapter.myLocations.remove(i);
                        locationAdapter.notifyItemRemoved(i);
                        adapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void openEditor(){

        Intent intent = new Intent(getApplicationContext(), EditInfoActivity.class);
        intent.putExtra("i", global_i);
        startActivityForResult(intent, 5);
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
        extendButton.setClickable(false);
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

        /*if (!expand) {

            extendButton.setClickable(false);
            timer.start();
            expand = !expand;
            deleteButton.setAlpha((float) 0);
            mapButton.setAlpha((float) 0);
            mapButton.setClickable(true);
            deleteButton.setClickable(true);
            extendButton.animate().rotation(45).setDuration(200).start();
            deleteButton.animate().translationYBy(-extendButton.getHeight() - 30).alpha((float) 1).setDuration(200).start();
            mapButton.animate().translationYBy(-extendButton.getHeight() - 60 - deleteButton.getHeight()).alpha((float) 1).setDuration(200).start();

        } else {

            expand = !expand;
            extendButton.setClickable(false);
            timer.start();
            extendButton.animate().rotation(0).setDuration(200).start();
            deleteButton.animate().translationYBy(extendButton.getHeight() + 30).alpha((float) 0).setDuration(200).start();
            mapButton.animate().translationYBy(extendButton.getHeight() + 60 + deleteButton.getHeight()).alpha((float) 0).setDuration(200).start();
            mapButton.setClickable(false);
            deleteButton.setClickable(false);

        }
        */
    }
}

