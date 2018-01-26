package com.indrajit.savelocations;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends SQLActivity {

    ListView listView;
    FloatingActionButton deleteButton;
    ArrayAdapter<String> adapter;
    ConstraintLayout mainLayout;
    Snackbar snackbar;
    int global_i;
    Intent start_intent;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        switch(item.getItemId()){
            case R.id.itemEdit:
                openEditorDialog();
                return true;

            case R.id.itemDelete:
                deleteLocationConfirmation();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deleteButton = findViewById(R.id.deleteButton);
        listView = findViewById(R.id.listView);
        registerForContextMenu(listView);
        mainLayout = findViewById(R.id.mainlayout);
        snackbar = Snackbar.make(mainLayout, "Press back again to exit.", Snackbar.LENGTH_LONG);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationlist);

        listView.setAdapter(adapter);

        start_intent = getIntent();
        String newname = start_intent.getStringExtra("newname");
        if(newname != null && newname.length() > 0){
            int i = start_intent.getIntExtra("i", -1);
            updateName(newname, i);
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                global_i = i;
                return i == 0;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(i == 0){
                    switchActivity();
                }

                else{

                    //
                }
            }
        });

        deleteButton.setAlpha((float) 0.0);
        deleteButton.animate().alpha((float) 0.8).setDuration(1000).start();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(snackbar.isShown()){

            finish();
        } else{

            snackbar.show();
        }
    }

    private void switchActivity(){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickDelete(View v){

        Snackbar.make(mainLayout, "Are you sure? ", Snackbar.LENGTH_LONG)
                .setAction("YES, remove all", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        resetTable();
                        locationlist.clear();
                        updateEntireList();
                        adapter.notifyDataSetChanged();
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    void deleteLocationConfirmation(){

        new AlertDialog.Builder(this)
                .setMessage("Are you sure?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog dialog = showLoader(MainActivity.this, R.layout.activity_delete);
                        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations",null);
                        if(cursor.getCount() > 0){

                            cursor.moveToPosition(global_i -1);
                            Object[] params = new Object[]{ cursor.getDouble(0), cursor.getDouble(1)};
                            locationlist.remove(global_i);
                            String sql = "DELETE FROM locations WHERE lat=? AND lon=?";
                            database.execSQL(sql, params);
                        }
                        cursor.close();
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    void openEditorDialog(){

        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        intent.putExtra("i", global_i);
        intent.putExtra("hint", locationlist.get(global_i));
        startActivity(intent);
    }

    void updateName(String s, int i){

        AlertDialog dialog = showLoader(MainActivity.this, R.layout.activity_delete);
        locationlist.set(i, s);
        Cursor cursor = database.rawQuery("SELECT lat,lon FROM locations", null);
        if(cursor.getCount() > 0){

            cursor.moveToPosition(i - 1);
            Object[] param = new Object[]{s, cursor.getDouble(0), cursor.getDouble(1)};
            String sql = "UPDATE locations SET name=? WHERE lat=? AND lon=?";
            database.execSQL(sql,param);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        dialog.dismiss();
    }
}

