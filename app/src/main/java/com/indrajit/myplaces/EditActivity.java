package com.indrajit.myplaces;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class EditActivity extends SQLActivity {

    private int position;
    private EditText viewNickname, viewFullname, viewLatlng;
    private Switch favSwitch;
    private MyLocation location;
    private ImageView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setSupportActionBar((Toolbar) findViewById(R.id.appBarEdit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewFullname = findViewById(R.id.editTextFullname);
        viewNickname = findViewById(R.id.editTextNickname);
        viewLatlng = findViewById(R.id.editTextLatLng);
        favSwitch = findViewById(R.id.switchFav);
        titleView = findViewById(R.id.titleImage);

        Intent start_intent = getIntent();
        position = start_intent.getIntExtra("position", -1);

        if(position != -1){

            location = LocationAdapter.myLocations.get(position);
            bindViews();

            URL url = null;
            try {
                url = new URL("http://maps.googleapis.com/maps/api/staticmap?zoom=17&size=600x400&maptype=roadmap&markers="
                        + String.valueOf(location.getLat())
                        + ","
                        + String.valueOf(location.getLon())
                        + "&key=AIzaSyDgRAzVoXrXXmZqt3zZ8ZjPB5sRYXoAROc");

            } catch (Exception ignored) {

                Toast.makeText(this, "dadadddadadaadad", Toast.LENGTH_SHORT).show();
            }

            StaticImageDownloadTask downloadTask = new StaticImageDownloadTask(new StaticImageDownloadTask.AsyncResponse() {
                @Override
                public void onTaskEnd(Bitmap bitmap) {

                    if(bitmap != null){

                        titleView.setImageBitmap(bitmap);
                    } else{

                        Toast.makeText(EditActivity.this, "Unable to fetch map...", Toast.LENGTH_SHORT).show();
                    }
                }
            }, url);

            downloadTask.execute();
        }


    }

    private void bindViews(){

        getSupportActionBar().setTitle(location.getNickname());
        viewLatlng.setText(String.format("%s, %s", String.valueOf(location.getLat()), String.valueOf(location.getLon())));
        favSwitch.setChecked(location.getFav() == 1);
        viewNickname.setHint(location.getNickname());
        viewFullname.setHint(location.getFullname());
    }

    public void onClickMap(View v){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("latlng_position", position);
        startActivity(intent);
    }

    public void onClickNicknameReset(View v){

        viewNickname.setText(location.getNickname());
    }

    public void onClickFullnameReset(View v){

        viewFullname.setText(location.getFullname());
    }

    public void onClickSaveEdit(View v){

        if(viewNickname.getText().toString().equals("")){

            viewNickname.setText(viewNickname.getHint());
        }

        if(viewFullname.getText().toString().equals("")){

            viewFullname.setText(viewFullname.getHint());
        }

        if(viewNickname.getText().toString().length() <= 3 || viewFullname.getText().toString().length() <= 8){

            Snackbar.make(getCurrentFocus(), "Don't they look too short?", Snackbar.LENGTH_LONG)
                    .setAction("Nah! Save 'em...", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int fav;
                            if( favSwitch.isChecked()){
                                fav = 1;
                            } else{
                                fav = 0;
                            }

                            SQLUtils.updateElement(database, new Object[] {viewFullname.getText().toString(), viewNickname.getText().toString(), fav, location.getLat(), location.getLon()});

                            startActivity(getSupportParentActivityIntent());
                        }
                    })
                    .show();
        } else{

            int fav;
            if( favSwitch.isChecked()){
                fav = 1;
            } else{
                fav = 0;
            }

            SQLUtils.updateElement(database, new Object[] {viewFullname.getText().toString(), viewNickname.getText().toString(), fav, location.getLat(), location.getLon()});


            Intent intent = getSupportParentActivityIntent();
            startActivity(intent);
        }
    }

    public void onClickCancelEdit(View v){

        startActivity(getSupportParentActivityIntent());
    }
}
