package com.indrajit.myplaces;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class OnClickMapTask extends AsyncTask<Void, Void, Integer>{

    private WeakReference<Context> context;
    private SQLiteDatabase database;
    private int fav;
    private AsyncResponse response;
    private LatLng latLng;

    interface AsyncResponse {

        void onFinish(LatLngInt latLngInt);
    }

    OnClickMapTask(Context context, int fav, AsyncResponse response, LatLng latLng) {

        this.context = new WeakReference<>(context);
        this.database = SQLUtils.initiateDatabase(context);
        this.fav = fav;
        this.response = response;
        this.latLng = latLng;
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        Geocoder geocoder = new Geocoder(context.get(), Locale.getDefault());
        String latitude = Double.toString(latLng.latitude);
        String longitude = Double.toString(latLng.longitude);

        long similarcount = DatabaseUtils.queryNumEntries(database,
                "locations", "(((lat - ?) * (lat - ?)) + ((lon - ?) * (lon - ?))) < 3.00e-8",
                new String[]{latitude,latitude,longitude,longitude});

        if(similarcount == 0){

            String nickname = "";StringBuilder fullname = new StringBuilder();

            try{

                List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);

                if(addressList != null && addressList.size() > 0){

                    Log.i("ADDRESS", addressList.get(0).toString());

                    if(addressList.get(0).getMaxAddressLineIndex() >= 0){

                        for(int i=0; i<= addressList.get(0).getMaxAddressLineIndex() - 1; i++) {
                            fullname.append(addressList.get(0).getAddressLine(i)).append(", ");
                        }
                        fullname.append(addressList.get(0).getAddressLine(addressList.get(0).getMaxAddressLineIndex()));

                    }  else {

                        if (addressList.get(0).getSubThoroughfare() != null) {
                            fullname.append(addressList.get(0).getSubThoroughfare()).append(" ");
                        }

                        if (addressList.get(0).getThoroughfare() != null) {
                            fullname.append(addressList.get(0).getThoroughfare()).append(", ");
                        }

                        if (addressList.get(0).getLocality() != null) {
                            fullname.append(addressList.get(0).getLocality()).append(", ");
                        }

                        if (addressList.get(0).getPostalCode() != null) {
                            fullname.append(addressList.get(0).getPostalCode()).append(", ");
                        }

                        if (addressList.get(0).getCountryName() != null) {
                            fullname.append(addressList.get(0).getCountryName());
                        }

                        if(fullname.toString().equals("")){

                            fullname = new StringBuilder("Unable to find location");
                        }
                    }

                    if(addressList.get(0).getThoroughfare() != null){

                        nickname += addressList.get(0).getThoroughfare() + ", ";
                    }
                    if(addressList.get(0).getLocality() != null){

                        nickname += addressList.get(0).getLocality() + ", ";
                    }
                    if(addressList.get(0).getPostalCode() != null){

                        nickname += addressList.get(0).getPostalCode();
                    }

                    if(nickname.equals("")){

                        nickname = "Lovely unknown place";
                    }
                } else{

                    fullname = new StringBuilder("Unable to find location");
                    nickname = "My lovely place";
                }

                Object[] params = new Object[]{latitude, longitude, nickname, fullname.toString(), fav};
                String sql = "INSERT INTO locations (lat,lon,nickname,fullname,fav) VALUES (?,?,?,?,?)";
                Log.i("SQL", Arrays.toString(params));

                database.execSQL(sql, params);

                return 0;

            } catch (Exception e) {

                return 1;
            }
        } else{

            return 2;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        response.onFinish(new LatLngInt(latLng,integer));
    }
}
