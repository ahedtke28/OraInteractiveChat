package com.ora.interactive.chalenge.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.fragments.HomeFragment;
import com.ora.interactive.chalenge.fragments.MenuFragment;
import com.ora.interactive.chalenge.fragments.MyProfileFragment;
import com.ora.interactive.chalenge.fragments.NavigationFragment;
import com.ora.interactive.chalenge.fragments.SignFragment;
import com.ora.interactive.chalenge.interfaces.OnFragmentInteractionListener;
import com.ora.interactive.chalenge.utilities.Utility;


public class MainActivity extends AppCompatActivity implements
        NavigationFragment.NavigationDrawerCallbacks,
        OnFragmentInteractionListener {

    static final String LOG = MainActivity.class.getSimpleName();

    NavigationFragment mNavigationFragment;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mNavigationFragment = (NavigationFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        // Setup drawer toggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Setup toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Set up the drawer.
        mNavigationFragment.setUp(R.id.navigation_drawer, mDrawerLayout, mToolbar);
        mDrawerToggle = mNavigationFragment.getDrawerToggle();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            if (Utility.readBooleanToProfile(Config.IS_USER_LOGGED, false)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment.newInstance(), HomeFragment.class.getSimpleName())
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SignFragment.newInstance(),  SignFragment.class.getSimpleName())
                        .commit();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int id) {
        Fragment fragment = null;
        String tag = "";

        switch(id) {
            case Config.SIGN_IN:
                fragment = getSupportFragmentManager().findFragmentByTag(SignFragment
                        .class.getSimpleName());
                if (fragment != null) {
                    SignFragment sg = (SignFragment)fragment;
                    sg.moveToPage(0);
                }
                return;
            case Config.SIGN_UP:
                fragment = getSupportFragmentManager().findFragmentByTag(SignFragment
                        .class.getSimpleName());
                if (fragment != null) {
                    SignFragment sg = (SignFragment) fragment;
                    sg.moveToPage(1);
                }
                return;
            case Config.SIGN:
                tag = SignFragment.class.getSimpleName();
                fragment = SignFragment.newInstance();
                break;
            case Config.HOME:
                tag = HomeFragment.class.getSimpleName();
                fragment = HomeFragment.newInstance();
                break;
            case Config.MENU:
                tag = MenuFragment.class.getSimpleName();
                fragment = MenuFragment.newInstance();
                break;
            case Config.PROFILE:
                tag = MyProfileFragment.class.getSimpleName();
                fragment = MyProfileFragment.newInstance();
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container,
                    fragment, tag).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Bundle data) {
        switch (data.getInt(Config.COMMAND, 0)) {
            case Config.INDETERMINATE_PROGRESS_BAR:
                openIndeterminateBar(data.getBoolean(Config.OPEN_INDETERMINATE_PROGRESS_BAR, false));
                break;
            default:
                break;
        }
    }

    public void openIndeterminateBar(boolean open) {
        setProgressBarIndeterminateVisibility(open);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
