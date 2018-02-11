package com.indrajit.myplaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.net.URL;

public class EditActivity extends SQLActivity {

    private int position;
    private EditText viewNickname, viewFullname, viewLatlng;
    private Switch favSwitch;
    private MyLocation location;
    private ImageView titleView;
    private TextInputLayout layoutNickname, layoutFullname;
    private CardView mapCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setSupportActionBar((Toolbar) findViewById(R.id.appBarEdit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapCardView = findViewById(R.id.staticMapView);
        viewFullname = findViewById(R.id.editTextFullname);
        viewNickname = findViewById(R.id.editTextNickname);
        viewLatlng = findViewById(R.id.editTextLatLng);
        favSwitch = findViewById(R.id.switchFav);
        titleView = findViewById(R.id.staticMapImageView);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingLayoutEdit);
        layoutNickname = findViewById(R.id.layoutNickname);
        layoutFullname = findViewById(R.id.layoutFullname);


        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);

        Intent start_intent = getIntent();
        position = start_intent.getIntExtra("position", -1);

        if(position != -1){

            location = LocationAdapter.myLocations.get(position);
            bindViews();

            URL url;

            try {

                url = new URL("http://maps.googleapis.com/maps/api/staticmap?zoom=19&size=640x500&maptype=roadmap&scale=2&markers="
                        + Double.toString(location.getLat())
                        + ","
                        + Double.toString(location.getLon())
                        + "&key=AIzaSyDgRAzVoXrXXmZqt3zZ8ZjPB5sRYXoAROc");

                StaticImageDownloadTask downloadTask = new StaticImageDownloadTask(new StaticImageDownloadTask.AsyncResponse() {
                    @Override
                    public void onTaskEnd(Bitmap bitmap) {

                        if(bitmap != null){

                            titleView.setImageBitmap(bitmap);
                            Animation animator = AnimationUtils.loadAnimation(EditActivity.this, android.R.anim.fade_in);
                            mapCardView.startAnimation(animator);


                        } else{

                            Toast.makeText(EditActivity.this, "Unable to fetch map...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, url);

                downloadTask.execute();

            } catch (Exception ignored) {

                Toast.makeText(this, "Opps! That's a bug...", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void bindViews(){

        getSupportActionBar().setTitle(location.getNickname());
        viewLatlng.setText(String.format("%s, %s", String.valueOf(location.getLat()), String.valueOf(location.getLon())));
        favSwitch.setChecked(location.getFav() == 1);
        layoutNickname.setHint(location.getNickname());
        layoutFullname.setHint(location.getFullname());
    }

    public void onClickMap(View v){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("latlng_position", position);
        startActivity(intent);
        finish();
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

                            goToParent();
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


            goToParent();
        }
    }

    public void onClickCancelEdit(View v){

        goToParent();
    }

    @Override
    public void onBackPressed() {

        goToParent();
    }

    private void goToParent(){

        navigateUpTo(getParentActivityIntent());
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {

        goToParent();
        return true;
    }
}
