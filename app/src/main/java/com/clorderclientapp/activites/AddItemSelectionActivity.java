package com.clorderclientapp.activites;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.clorderclientapp.R;

public class AddItemSelectionActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView addItemBackImage, optionsImg;
    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_selection);
        initViews();
        listeners();
    }


    private void initViews() {
        addItemBackImage = (ImageView) findViewById(R.id.add_item_back);
        optionsImg = (ImageView) findViewById(R.id.options_img);
    }


    private void listeners() {
        addItemBackImage.setOnClickListener(this);
        optionsImg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_item_back:
                onBackPressed();
                break;
            case R.id.options_img:
                alertDialog = new AlertDialog.Builder(this).create();
                LayoutInflater layoutInflaterTime = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                View addNoteLayout = layoutInflaterTime.inflate(R.layout.layout_custom_options, null);
                alertDialog.setView(addNoteLayout);
                alertDialog.show();
                break;
        }

    }
}
