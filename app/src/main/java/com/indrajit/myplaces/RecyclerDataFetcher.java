package com.indrajit.myplaces;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

class RecyclerDataFetcher {

    static ArrayList<MyLocation> populateList(Context context) {

        ArrayList<MyLocation> myLocations = new ArrayList<>();
        myLocations.add(new MyLocation("Add a place", null, 0, 0, 0, 0));

        SQLiteDatabase database = SQLUtils.initiateDatabase(context);
        Cursor cursor = database.rawQuery("SELECT * FROM locations", null);

        if (cursor.moveToFirst()) {

            do {
                myLocations.add(new MyLocation(
                        cursor.getString(3),
                        cursor.getString(2),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getDouble(0),
                        cursor.getDouble(1)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return myLocations;
    }
}
