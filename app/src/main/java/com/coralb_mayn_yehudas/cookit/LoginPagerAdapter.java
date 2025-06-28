package com.coralb_mayn_yehudas.cookit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * manages the fragments for the login/signup tabs in ViewPager2.
 * returns either the LoginFragment or SignupFragment based on the selected tab position.
 */
public class LoginPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor for the adapter, accepting the host activity.
     * get fragmentActivity the activity hosting the ViewPager2
     */
    public LoginPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Returns the appropriate fragment for the given tab position.
     * get position the index of the selected tab (0 for login, 1 for signup)
     * return a Fragment instance (LoginFragment or SignupFragment)
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new LoginFragment(); // login tab
        } else {
            return new SignupFragment(); // Signup tab
        }
    }

    /**
     * Returns the total number of tabs.
     * return 2 (login and signup)
     */
    @Override
    public int getItemCount() {
        return 2; // We have 2 tabs
    }
}
