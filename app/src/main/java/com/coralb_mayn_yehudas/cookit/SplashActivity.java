package com.coralb_mayn_yehudas.cookit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish(); // Important to prevent relapse (rollback)
        }, SPLASH_DISPLAY_LENGTH);
    }
}
