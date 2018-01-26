package com.indrajit.savelocations;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MapsActivity extends SQLActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    FusedLocationProviderClient client;
    LocationRequest request;
    LocationCallback callback;
    AlertDialog dialog;

    void blahblah(String blah){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        client = LocationServices.getFusedLocationProviderClient(this);
        request = new LocationRequest();
        request.setInterval(500).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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

                setCurrentLocation();

            } else {

                //ask to continue without current location
                createDialogBox("Would you like to continue without location?");
            }
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

                setCurrentLocation();
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

                OnMapClickTask task = new OnMapClickTask();

                task.execute(latLng);
            }
        });
    }

    void setCurrentLocation() {

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

    @SuppressLint("StaticFieldLeak")
    public class OnMapClickTask extends AsyncTask<LatLng, Void, LatLngInt> {

        @Override
        protected LatLngInt doInBackground(LatLng... latLngs) {

            String latitude = Double.toString(latLngs[0].latitude);
            String longitude = Double.toString(latLngs[0].longitude);

            long similarcount = DatabaseUtils.queryNumEntries(database,"locations", "(((lat - ?) * (lat - ?)) + ((lon - ?) * (lon - ?))) < 3.00e-8",
                    new String[]{latitude,latitude,longitude,longitude});

            if(similarcount == 0){

                String address;

                try {

                    List<Address> addressList = geocoder.getFromLocation(latLngs[0].latitude, latLngs[0].longitude, 1);

                    address = "";

                    if (addressList != null && addressList.size() > 0) {

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

            if(i == 0){

                mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                Toast.makeText(MapsActivity.this, "Added!", Toast.LENGTH_SHORT).show();
            } else if(i == 1){

                Toast.makeText(MapsActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(MapsActivity.this, "The location is already in the list...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LatLngInt{

        LatLng l;
        Integer i;

        LatLngInt(LatLng l, Integer i){

            this.l = l;
            this.i = i;
        }

        Integer getI(){

            return i;
        }

        LatLng getL(){

            return l;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switchActivity();
    }

    void switchActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    void createDialogBox(String s){

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
}
