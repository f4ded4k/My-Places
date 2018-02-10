package com.indrajit.myplaces;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

abstract class SQLActivity extends AppCompatActivity {

    protected static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = SQLUtils.initiateDatabase(this);
    }

    protected void resetTable(){

        SQLUtils.resetTable(database);
    }

    protected AlertDialog showLoader(Context context,int id){

        return new AlertDialog.Builder(context)
                .setView(id)
                .setCancelable(false)
                .show();
    }
}
