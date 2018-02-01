package com.indrajit.savelocations;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends SQLActivity {

    private FloatingActionButton extendButton, mapButton, deleteButton;
    private ArrayAdapter<String> adapter;
    private ConstraintLayout mainLayout;
    private Snackbar snackbar;
    private int global_i;
    private boolean expand;
    private CountDownTimer timer;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listView) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_main, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        switch(item.getItemId()){
            case R.id.itemEdit:
                openEditor();
                return true;

            case R.id.itemDelete:
                deleteLocationConfirmation();
                return true;

            case R.id.itemDirection:
                goToDirection();
                return true;

            case R.id.itemGmap:
                goToGmap();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expand=false;
        timer = new CountDownTimer(210,210) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                extendButton.setClickable(true);
            }
        };

        extendButton = findViewById(R.id.extendButton);
        mapButton = findViewById(R.id.mapButton);
        deleteButton = findViewById(R.id.deleteButton);
        ListView listView = findViewById(R.id.listView);
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
        finish();
    }

    private void switchActivityToMarker(int i){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("g_i", i);
        startActivity(intent);
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
                        onClickExtend(null);
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    private void deleteLocationConfirmation(){

        new AlertDialog.Builder(this)
                .setMessage("Are you sure?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog dialog = showLoader(MainActivity.this, R.layout.activity_delete);
                        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
                        if(cursor.getCount() > 0){

                            cursor.moveToPosition(global_i -1);
                            Object[] params = new Object[]{ cursor.getDouble(0), cursor.getDouble(1)};
                            locationlist.remove(global_i);
                            String sql = "DELETE FROM locations WHERE lat=? AND lon=?";
                            database.execSQL(sql, params);
                        }
                        cursor.close();
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

    public void onClickExtend(View view) {

        if (!expand) {

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
    }

    private void goToDirection() {

        double lat = 0, lon = 0;

        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
        if(cursor.getCount() > 0){

            cursor.moveToPosition(global_i -1);
            lat = cursor.getDouble(0);
            lon = cursor.getDouble(1);
        }
        cursor.close();

        String str = "https://www.google.com/maps/dir/?api=1&" + "&destination=" + String.valueOf(lat) + "," + String.valueOf(lon);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        startActivity(browserIntent);

    }

    private void goToGmap(){

        double lat = 0, lon = 0;

        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
        if(cursor.getCount() > 0){

            cursor.moveToPosition(global_i -1);
            lat = cursor.getDouble(0);
            lon = cursor.getDouble(1);
        }
        cursor.close();

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
}

