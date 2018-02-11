package com.indrajit.myplaces;

import android.Manifest;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends SQLActivity implements OnMapReadyCallback, OnClickMapTask.AsyncResponse, PlaceSelectionListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationRequest request;
    private LocationCallback callback;
    private AlertDialog dialog;
    private Intent start_intent;
    private ProgressBar workingBar;
    private Toast mapToast;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        workingBar = findViewById(R.id.workingBar);
        workingBar.setAlpha((float) 0);

        client = LocationServices.getFusedLocationProviderClient(this);
        request = new LocationRequest();
        request.setInterval(500).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1);

        start_intent = getIntent();

        SupportPlaceAutocompleteFragment placeAutocompleteFragment = (SupportPlaceAutocompleteFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_search);

        placeAutocompleteFragment.setOnPlaceSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                dialog.dismiss();

                Location l = locationResult.getLastLocation();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 18));
            }
        };

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivity();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {

            if (resultCode == -1) {

                goToAskedLocation();

            } else {

                createDialogBox("Would you like to continue without location?");
            }
        }
    }

    private void goToAskedLocation() {

        int i = start_intent.getIntExtra("latlng_position", -1);
        if (i == -1) {

            setCurrentLocation();

        } else {

            setMarkerLocation(i);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        View v = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 50, 50);
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
                } else {

                    createDialogBox("Opps! Looks like your phone is unable to access current location.\nContinue without location? ");
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                showBar();

                new OnClickMapTask(MapsActivity.this, 0, MapsActivity.this, latLng).execute();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                showBar();
                new OnClickMapTask(MapsActivity.this, 1, MapsActivity.this, latLng).execute();
            }
        });

        //Log.i("TAB", String.valueOf(placeAutocompleteFragment.getView().getHeight()));
    }

    void setMarkerLocation(int i) {

        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);

                LatLng latLng = LocationAdapter.myLocations.get(i).getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                //mMap.addCircle(new CircleOptions().center(latLng).strokeColor(Color.TRANSPARENT).fillColor(R.color.mapCircle).radius(15).visible(true));
            }
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);

                LatLng latLng = LocationAdapter.myLocations.get(i).getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                //mMap.addCircle(new CircleOptions().center(latLng).strokeColor(Color.BLUE).fillColor(R.color.mapCircle).radius(15).visible(true));


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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            setCurrentLocation();

        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

            Snackbar.make(findViewById(R.id.mapslayout), "We need your permission to access your current location.....", BaseTransientBottomBar.LENGTH_LONG).show();

            setCurrentLocation();

        } else {

            setCurrentLocation();
        }
    }

    private void populateMarkers() {

        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations", null);

        if (cursor.moveToFirst()) {

            LatLng latLng;

            do {

                latLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    public void onFinish(LatLngInt latLngInt) {

        int i = latLngInt.getI();
        LatLng l = latLngInt.getL();

        if (i == 0) {

            mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            if (mapToast != null) {
                mapToast.cancel();
            }
            mapToast = Toast.makeText(MapsActivity.this, "Added!", Toast.LENGTH_SHORT);
            mapToast.show();

        } else if (i == 1) {

            if (mapToast != null) {
                mapToast.cancel();
            }
            mapToast = Toast.makeText(MapsActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT);
            mapToast.show();

        } else {

            if (mapToast != null) {
                mapToast.cancel();
            }
            mapToast = Toast.makeText(MapsActivity.this, "The location is already in the list...", Toast.LENGTH_SHORT);
            mapToast.show();
        }

        hideBar();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        switchActivity();
    }

    private void switchActivity() {

        navigateUpTo(getParentActivityIntent());
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void createDialogBox(String s) {

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

    private void showBar() {

        workingBar.animate().alpha((float) 1).setDuration(500).start();
    }

    private void hideBar() {

        workingBar.animate().alpha((float) 0).setDuration(500).start();
    }

    @Override
    public void onPlaceSelected(Place place) {

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));
    }

    @Override
    public void onError(Status status) {

        if (status.hasResolution()) {

            try {
                status.startResolutionForResult(this, 9);

            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(this, "Something went wrong! Try again.", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(this, "Something went wrong! Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
