package com.indrajit.myplaces;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.maps.model.LatLng;

public class EditInfoActivity extends SQLActivity {

    ActionBar actionbar;
    Intent start_intent;
    EditText editName;
    Switch favSwitch;
    int i;
    AlertDialog dialog;
    String hint;
    LatLng latLng;
    int fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        actionbar = getSupportActionBar();
        actionbar.setTitle("Details");
        actionbar.setDisplayHomeAsUpEnabled(true);
        start_intent = getIntent();
        i = start_intent.getIntExtra("i", -1);

        editName = findViewById(R.id.editName);
        favSwitch = findViewById(R.id.favSwitch);

        dialog = makeDialog();
        dialog.show();

        Cursor cursor = database.rawQuery("SELECT * FROM locations",null);
        if(cursor.getCount() > 0){
            cursor.moveToPosition(i - 1);
        }
        hint = cursor.getString(2);
        latLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
        fav = cursor.getInt(3);
        cursor.close();

        editName.setHint(hint);
        favSwitch.setChecked(fav == 1);

        dialog.dismiss();
    }

    public void onClickButtonCancel(View view) {

        finish();
    }

    public void onClickButtonSave(View view) {

        String new_name = editName.getText().toString();
        if(new_name.length() == 0){
            new_name = hint;
        }
        boolean fav_b = favSwitch.isChecked();
        int fav;
        if(fav_b){
            fav = 1;
        } else{
            fav = 0;
        }
        Object[] param = new Object[] {new_name, fav, latLng.latitude, latLng.longitude};
        String sql = "UPDATE locations SET name = ?,fav = ? WHERE lat=? AND lon=?";
        database.execSQL(sql, param);
        locationlist.set(i, new_name);
        finish();
    }

    private AlertDialog makeDialog(){

        return new AlertDialog.Builder(this)
                .setView(R.layout.activity_almostthere)
                .setCancelable(false)
                .create();
    }
}
