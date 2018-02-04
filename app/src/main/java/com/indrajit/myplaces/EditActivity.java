package com.indrajit.myplaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EditActivity extends Activity {

    TextView textView;
    int i;
    String hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        i = intent.getIntExtra("i", -1);
        hint = intent.getStringExtra("hint");
        textView = findViewById(R.id.editLocationText);
        textView.setHint(hint);
    }

    public void onClickButtonSave(View v){

        String new_name = textView.getText().toString();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("newname", new_name);
        intent.putExtra("i", i);
        startActivity(intent);
        finish();
    }

    public void onClickButtonCancel(View v){

        finish();
    }
}
