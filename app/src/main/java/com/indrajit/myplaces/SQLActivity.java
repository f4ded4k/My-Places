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

        updateEntireList();
    }

    protected void resetTable(){

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
