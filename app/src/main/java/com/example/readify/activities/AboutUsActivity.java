package com.example.readify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.readify.R;
import com.google.android.material.button.MaterialButton;

public class AboutUsActivity extends AppCompatActivity {

    MaterialButton aboutustoget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            ((Window) window).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }
        setContentView(R.layout.about_us);


        aboutustoget = findViewById(R.id.aboutus_to_getstarted);

        aboutustoget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AboutUsActivity.this, GetstartedActivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}