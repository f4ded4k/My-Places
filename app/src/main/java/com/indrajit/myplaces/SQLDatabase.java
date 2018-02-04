package com.indrajit.myplaces;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

class SQLDatabase {

    SQLiteDatabase database;

    static SQLiteDatabase initiateDatabase(Context context){

        SQLiteDatabase database =  context.openOrCreateDatabase("Locations", Context.MODE_PRIVATE, null);
        initiateTable(database);
        return database;
    }

    static private void initiateTable(SQLiteDatabase database){

        database.execSQL("CREATE TABLE IF NOT EXISTS locations(lat DOUBLE, lon DOUBLE, nickname VARCHAR(20), fullname VARCHAR(50), fav INTEGER(1) DEFAULT 0, priority INTEGER(2) DEFAULT 20)");
    }

    static void resetTable(SQLiteDatabase database){

        database.execSQL("DELETE FROM locations");
    }
}
