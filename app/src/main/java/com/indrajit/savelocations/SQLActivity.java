package com.indrajit.savelocations;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class SQLActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayList<String> locationlist;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        geocoder = new Geocoder(this, Locale.getDefault());
        locationlist = new ArrayList<>();

        database = this.openOrCreateDatabase("Database", MODE_PRIVATE, null);

        database.execSQL("CREATE TABLE IF NOT EXISTS locations(lat DOUBLE, lon DOUBLE, name VARCHAR(50))");

        updateEntireList();
    }

    void resetTable(){

        database.execSQL("DELETE FROM locations");
    }

    void updateEntireList() {

        locationlist.add("Add a new place....");

        Cursor cursor = database.rawQuery("SELECT name FROM locations", null);

        if(cursor.moveToFirst()){


            do{

                locationlist.add(cursor.getString(0));

            } while(cursor.moveToNext());
        }

        cursor.close();
    }

    AlertDialog showLoader(Context context,int id){

        return new AlertDialog.Builder(context)
                .setView(id)
                .setCancelable(false)
                .show();
    }
}
