package com.ora.interactive.chalenge.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.adapters.SignAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignFragment extends FragmentBase implements ViewPager.OnPageChangeListener {
    SignAdapter mAdapter;
    @Bind(R.id.viewPager)
    ViewPager vViewPager;
    @Bind(R.id.signIn)
    TextView signIn;
    @Bind(R.id.signUp)
    TextView signUp;

    public static SignFragment newInstance() {
        SignFragment fragment = new SignFragment();
        return fragment;
    }

    public SignFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        View view = getView();
        ButterKnife.bind(this, view);

        mAdapter = new SignAdapter(getFragmentManager());

        vViewPager.setAdapter(mAdapter);
        vViewPager.addOnPageChangeListener(this);
        selectPage(0);
    }

    @OnClick({ R.id.signIn, R.id.signUp})
    public void onButtonClick(View button) {
        switch(button.getId()) {
            case R.id.signIn:
                moveToPage(0);
                break;
            case R.id.signUp:
                moveToPage(1);
                break;
        }
    }

    public void moveToPage(int page) {
        vViewPager.setCurrentItem(page);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        selectPage(position);
    }

    public void selectPage(int position) {
        Resources resources = getResources();

        switch (position) {
            case 0:
                signIn.setTextColor(resources.getColor(R.color.white));
                signUp.setTextColor(resources.getColor(R.color.gray));
                break;

            case 1:
                signIn.setTextColor(resources.getColor(R.color.gray));
                signUp.setTextColor(resources.getColor(R.color.white));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
