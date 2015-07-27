package com.ora.interactive.chalenge.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.utilities.Utility;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuFragment extends FragmentBase {

    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        ButterKnife.bind(this, getView());
    }

    @OnClick({ R.id.editProfile, R.id.logOut })
    public void onClickView(View view) {
        switch(view.getId()) {
            case R.id.editProfile:
                break;
            case R.id.logOut:
                Utility.deleteUserProfile();
                Utility.sendUserLogout();
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
