package com.indrajit.myplaces;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

abstract class SQLActivity extends AppCompatActivity {

    protected static SQLiteDatabase database;
    protected static ArrayList<String> locationlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationlist = new ArrayList<>();

        database = SQLUtils.initiateDatabase(this);

        //database = openOrCreateDatabase("Database", MODE_PRIVATE, null);

        //database.execSQL("CREATE TABLE IF NOT EXISTS locations(lat DOUBLE, lon DOUBLE, nickname VARCHAR(20), fullname VARCHAR(50), fav INTEGER(1) DEFAULT 0, priority INTEGER(2) DEFAULT 20)");

        updateEntireList();
    }

    protected void resetTable(){

        //database.execSQL("DELETE FROM locations");
        SQLUtils.resetTable(database);
    }

    protected void updateEntireList() {

        locationlist.add("Add a new place....");

        Cursor cursor = database.rawQuery("SELECT fullname FROM locations", null);

        if(cursor.moveToFirst()){


            do{

                locationlist.add(cursor.getString(0));

            } while(cursor.moveToNext());
        }

        cursor.close();
    }

    protected AlertDialog showLoader(Context context,int id){

        return new AlertDialog.Builder(context)
                .setView(id)
                .setCancelable(false)
                .show();
    }
}
