package com.indrajit.myplaces;

import android.Manifest;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends SQLActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationRequest request;
    private LocationCallback callback;
    private AlertDialog dialog;
    private Intent start_intent;
    private ProgressBar workingBar;
    private Toast mapToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i("GEOCODER", String.valueOf(Geocoder.isPresent()));
        workingBar = findViewById(R.id.workingBar);
        workingBar.setAlpha((float) 0);

        client = LocationServices.getFusedLocationProviderClient(this);
        request = new LocationRequest();
        request.setInterval(500).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1);

        start_intent = getIntent();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        callback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                dialog.dismiss();

                Location l = locationResult.getLastLocation();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(),l.getLongitude()), 18));
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2){

            if(resultCode == -1){

                goToAskedLocation();

            } else {

                //ask to continue without current location
                createDialogBox("Would you like to continue without location?");
            }
        }
    }

    private void goToAskedLocation(){

        int i = start_intent.getIntExtra("g_i", -1);
        if( i == -1){

            setCurrentLocation();

        } else{

            setMarkerLocation(i);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        populateMarkers();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                goToAskedLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {

                    try {

                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this, 2);

                    } catch (IntentSender.SendIntentException sendEx) {

                        //ask to continue without current location
                        createDialogBox("Opps! Unable to locate you!.\nContinue without location? ");
                    }
                } else{

                    createDialogBox("Opps! Looks like your phone is unable to access current location.\nContinue without location? ");
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                showBar();

                OnMapClickTask task = new OnMapClickTask();

                try {

                    task.execute(latLng);

                } catch (Exception e) {

                    Toast.makeText(MapsActivity.this, "Opps! something went wrong!", Toast.LENGTH_SHORT).show();
                    hideBar();
                }
            }
        });
    }

    void setMarkerLocation(int i) {

        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
                dialog = showLoader(this, R.layout.activity_almostthere);

                Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
                LatLng latLng = null;
                if(cursor.getCount() > 0){
                    cursor.moveToPosition(i - 1);
                    latLng = new LatLng(cursor.getDouble(0),cursor.getDouble(1));
                }
                cursor.close();

                dialog.dismiss();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                mMap.addCircle(new CircleOptions().center(latLng).strokeColor(Color.TRANSPARENT).fillColor(R.color.mapCircle).radius(15).visible(true));
            }
        } else {

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mMap.setMyLocationEnabled(true);
                dialog = showLoader(this, R.layout.activity_almostthere);

                Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
                LatLng latLng = null;
                if(cursor.getCount() > 0){
                    cursor.moveToPosition(i - 1);
                    latLng = new LatLng(cursor.getDouble(0),cursor.getDouble(1));
                }
                cursor.close();
                dialog.dismiss();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                mMap.addCircle(new CircleOptions().center(latLng).strokeColor(Color.TRANSPARENT).fillColor(R.color.mapCircle).radius(15).visible(true));


            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void setCurrentLocation() {

        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);

                client.requestLocationUpdates(request, callback, null);

                dialog = showLoader(this, R.layout.activity_dialog);

            }
        } else {

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mMap.setMyLocationEnabled(true);

                client.requestLocationUpdates(request, callback, null);

                dialog = showLoader(this, R.layout.activity_dialog);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            setCurrentLocation();

        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){

            Snackbar.make(findViewById(R.id.mapslayout), "We need your permission to access your current location.....", BaseTransientBottomBar.LENGTH_LONG).show();

            setCurrentLocation();

        } else{

            setCurrentLocation();
        }
    }

    private void populateMarkers(){

        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);

        if(cursor.moveToFirst()){

            LatLng latLng;

            do{

                latLng = new LatLng(cursor.getDouble(0),cursor.getDouble(1));
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

            }while(cursor.moveToNext());

            cursor.close();
        }
    }

    private class OnMapClickTask extends AsyncTask<LatLng, Void, LatLngInt> {

        @Override
        protected LatLngInt doInBackground(LatLng... latLngs) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            String latitude = Double.toString(latLngs[0].latitude);
            String longitude = Double.toString(latLngs[0].longitude);

            long similarcount = DatabaseUtils.queryNumEntries(database,"locations", "(((lat - ?) * (lat - ?)) + ((lon - ?) * (lon - ?))) < 3.00e-8",
                    new String[]{latitude,latitude,longitude,longitude});

            if(similarcount == 0 && !isCancelled()){

                String address;

                try {

                    List<Address> addressList = geocoder.getFromLocation(latLngs[0].latitude, latLngs[0].longitude, 1);

                    address = "";

                    if (addressList != null && addressList.size() > 0 && !isCancelled()) {

                        if(addressList.get(0).getMaxAddressLineIndex() >= 0){

                            address = addressList.get(0).getAddressLine(0);

                        } else {

                            if (addressList.get(0).getSubThoroughfare() != null) {
                                address += addressList.get(0).getSubThoroughfare() + " ";
                            }

                            if (addressList.get(0).getThoroughfare() != null) {
                                address += addressList.get(0).getThoroughfare() + ", ";
                            }

                            if (addressList.get(0).getLocality() != null) {
                                address += addressList.get(0).getLocality() + ", ";
                            }

                            if (addressList.get(0).getPostalCode() != null) {
                                address += addressList.get(0).getPostalCode() + ", ";
                            }

                            if (addressList.get(0).getCountryCode() != null) {
                                address += addressList.get(0).getCountryCode();
                            }
                        }

                    } else {

                        address = "Address not found";
                    }

                    String sql = "INSERT INTO locations(lat,lon,name) VALUES (?,?,?)";

                    database.execSQL(sql, new Object[]{ latitude, longitude, address});

                    locationlist.add(address);

                    return new LatLngInt(latLngs[0],0);

                } catch (Exception e){

                    //e.printStackTrace();
                    return new LatLngInt(latLngs[0],1);
                }


            } else{

                return new LatLngInt(latLngs[0],2);
            }
        }

        @Override
        protected void onPostExecute(LatLngInt latLngInt) {
            super.onPostExecute(latLngInt);

            int i = latLngInt.getI();
            LatLng l = latLngInt.getL();

            hideBar();

            if(i == 0){

                mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                if(mapToast != null) {
                    mapToast.cancel();
                }
                mapToast = Toast.makeText(MapsActivity.this, "Added!", Toast.LENGTH_SHORT);
                mapToast.show();

            } else if(i == 1){

                if(mapToast != null) {
                    mapToast.cancel();
                }
                mapToast = Toast.makeText(MapsActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT);
                mapToast.show();

            } else {

                if(mapToast != null) {
                    mapToast.cancel();
                }
                mapToast = Toast.makeText(MapsActivity.this, "The location is already in the list...", Toast.LENGTH_SHORT);
                mapToast.show();

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switchActivity();
    }

    private void switchActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void createDialogBox(String s){

        new AlertDialog.Builder(this)
                .setMessage(s)
                .setPositiveButton("Yes, continue anyway...", null)
                .setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switchActivity();
                    }
                })
                .show();
    }

    private void showBar(){

        workingBar.animate().alpha((float) 1).setDuration(500).start();
    }

    private void hideBar(){

        workingBar.animate().alpha((float) 0).setDuration(500).start();
    }
}
