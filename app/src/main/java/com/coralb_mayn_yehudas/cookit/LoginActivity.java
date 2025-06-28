package com.coralb_mayn_yehudas.cookit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Login activity hosts the login and signup interface using a tabbed layout.
 * It initializes dark mode and locale preferences before displaying the UI.
 */
public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout; // UI component for switching between login/signup tabs
    private ViewPager2 viewPager; // Container for fragment swiping (login/signup)
    private LoginPagerAdapter loginPagerAdapter; // Adapter to handle the fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // apply dark mode setting based on user preferences
        boolean isDarkMode = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        LocaleHelper.applySavedLocale(this); // apply saved language preference before layout is drawn
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up the pager adapter for login and signup fragments
        loginPagerAdapter = new LoginPagerAdapter(this);
        viewPager.setAdapter(loginPagerAdapter);

        // Connect the tab layout with the view pager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getString(R.string.login_tab)); // login tab
            } else {
                tab.setText(getString(R.string.signup_tab)); // Sign Up tab
            }
        }).attach();
    }
}
