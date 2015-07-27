package com.ora.interactive.chalenge.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.adapters.NavigationAdapter;
import com.ora.interactive.chalenge.beans.Option;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.controller.OraInteractiveApp;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NavigationFragment extends Fragment implements AdapterView.OnItemClickListener{
    static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String LOG = NavigationFragment.class.getName();

    NavigationDrawerCallbacks mCallbacks;
    ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    @Bind(R.id.options)
    ListView mDrawerListView;
    @Bind(R.id.myPhoto)
    ImageView myPhoto;
    @Bind(R.id.myName)
    TextView myName;

    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    ArrayList<Option> options = new ArrayList<>();
    NavigationAdapter mAdapter;

    NavigationReceiver mReceiver;
    IntentFilter mFilter;

    public NavigationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLearnedDrawer = Utility.readBooleanToProfile(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        ButterKnife.bind(this, view);

        mDrawerListView = (ListView) view.findViewById(R.id.options);
        mDrawerListView.setOnItemClickListener(this);

        loadOptions();
        mAdapter = new NavigationAdapter(getActivity(), R.layout.navigation_option,
                options);
        mDrawerListView.setAdapter(mAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        updateOptions();

        mReceiver = new NavigationReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction(Config.NO_SESSION_APP);
        mFilter.addAction(Config.SESSION_APP);

        updateName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.navigation_menu, container, false);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param toolbar Toolbar to be used for this navigator
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);

        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        setUpActionBar();

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (!isAdded()) return;

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    Utility.writeBooleanToProfile(PREF_USER_LEARNED_DRAWER, true);
                }

                // calls onPrepareOptionsMenu()
                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!isAdded()) return;

                // calls onPrepareOptionsMenu()
                getActivity().supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    protected void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void selectItem(int position) {

        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }

        setOption(position);
    }

    public void setOption(int position) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);
        Log.i(LOG, "onResume()");
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        Log.i(LOG, "onPause()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected List<Option> loadOptions() {
        ArrayList<Option> options = new ArrayList<>();
        Resources resources = getResources();

        if (Utility.readBooleanToProfile(Config.IS_USER_LOGGED, false)) {
            options.add(new Option(Config.HOME,
                    resources.getString(R.string.home)));
            options.add(new Option(Config.MENU,
                    resources.getString(R.string.menu)));
            options.add(new Option(Config.PROFILE,
                    resources.getString(R.string.my_profile)));
        } else {
            options.add(new Option(Config.SIGN_IN,
                    resources.getString(R.string.oSignIn)));
            options.add(new Option(Config.SIGN_UP,
                    resources.getString(R.string.oSignUp)));
        }
        return options;
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public final ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setOption(options.get(position).getId());
    }

    public void updateOptions() {
        mAdapter.clear();
        mAdapter.addAll(loadOptions());
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    private class NavigationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateOptions();
            if (intent.getAction().equals(Config.NO_SESSION_APP)) {
                setOption(Config.SIGN);
            } else if (intent.getAction().equals(Config.SESSION_APP)) {
                setOption(Config.HOME);
                OraInteractiveApp.getApp().loadServices();
                updateName();
            }
        }
    }

    protected void updateName() {
        myName.setText(Utility.readStringToProfile(Config.FIRST_NAME, "Me"));
    }
}
