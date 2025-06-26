package com.coralb_mayn_yehudas.cookit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LoginPagerAdapter loginPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Dark mode
        boolean isDarkMode = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        LocaleHelper.applySavedLocale(this); // language
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        loginPagerAdapter = new LoginPagerAdapter(this);
        viewPager.setAdapter(loginPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getString(R.string.login_tab));
            } else {
                tab.setText(getString(R.string.signup_tab));
            }
        }).attach();
    }
}
