package com.clorderclientapp.activites;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.clorderclientapp.R;

public class TastyFoodScreenActivity extends AppCompatActivity implements View.OnClickListener {
    Button jhnPizzaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasty_food_activity);
        jhnPizzaBtn = (Button) findViewById(R.id.johnnies_pizza_btn);
        jhnPizzaBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.johnnies_pizza_btn:
                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class));
                break;

        }
    }
}
