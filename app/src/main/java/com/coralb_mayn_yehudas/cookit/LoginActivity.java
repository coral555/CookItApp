package com.coralb_mayn_yehudas.cookit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LoginPagerAdapter loginPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this); // 🟢 חשוב: החלת שפה לפני הטעינה
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        loginPagerAdapter = new LoginPagerAdapter(this);
        viewPager.setAdapter(loginPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getString(R.string.login_tab));  // 🟢 נמשך מהstrings.xml
            } else {
                tab.setText(getString(R.string.signup_tab)); // 🟢 תרגום אוטומטי
            }
        }).attach();
    }
}
