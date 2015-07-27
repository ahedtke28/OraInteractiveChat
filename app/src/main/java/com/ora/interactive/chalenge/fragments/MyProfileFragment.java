package com.ora.interactive.chalenge.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.beans.RegistrationErrorWrapper;
import com.ora.interactive.chalenge.beans.RegistrationResponse;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.controller.OraInteractiveApp;
import com.ora.interactive.chalenge.network.LoadServices;
import com.ora.interactive.chalenge.network.Service;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyProfileFragment extends FragmentBase {
    private static final String LOG = MyProfileFragment.class.getName();
    @Bind(R.id.emailAccount)
    EditText emailAccount;
    @Bind(R.id.firstName)
    EditText firstName;
    @Bind(R.id.mailPassword)
    EditText mailPassword;

    public static MyProfileFragment newInstance() {
        MyProfileFragment fragment = new MyProfileFragment();
        return fragment;
    }

    public MyProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_profile_fragment, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        View view = getView();
        ButterKnife.bind(this, view);

        // Fill initial data
        emailAccount.setText(Utility.readStringToProfile(Config.USER_EMAIL, ""));
        firstName.setText(Utility.readStringToProfile(Config.FIRST_NAME, ""));
    }

    @OnClick({ R.id.update })
    public void onClickView(View view) {
        switch(view.getId()) {
            case R.id.update:
                updateUserProfile();
                break;
        }
    }

    public void updateUserProfile() {
        if (isProcessing) return;
        Map map = new HashMap();
        map.put("email", emailAccount.getText().toString());
        map.put("name", firstName.getText().toString());
        map.put("password", mailPassword.getText().toString());
        updateAccount(map);
    }

    protected void updateAccount(Map map) {
        Service mService = new Service();
        mService.setServiceCode(Config.POST_USER_CURRENT_CODE);
        mService.setServiceName(Config.PAG_USER_CURRENT);
        mService.setServiceType(Config.PUT);
        mService.setHeaders(Utility.getJsonAccessAndAuthorization(Utility.
                readStringToProfile(Config.USER_TOKEN, "")));
        mService.setServiceInput(map);
        mService.setNotificationTask(this);
        new LoadServices().loadOnExecutor(mService);
        openProgressBar(true);
    }

    @Override
    public void completed(Service response) {
        openProgressBar(false);

        switch(response.getServiceCode()) {

            case Config.POST_USER_CURRENT_CODE: {
                Object convert;
                RegistrationResponse rr;
                RegistrationErrorWrapper errorWrapper;

                try {
                    convert = Utility.parseJSON(response.getOutput(),
                            RegistrationResponse.class);
                    if (convert == null) {
                        convert = Utility.parseJSON(response.getOutput(),
                                RegistrationErrorWrapper.class);

                        // Error Query
                        if (convert != null) {
                            errorWrapper = (RegistrationErrorWrapper) convert;
                            Utility.showToast(errorWrapper.getError().getMessage());
                        } else {
                            Utility.showToast("server not responded");
                        }
                    } else {
                        Utility.showToast("Successful call");

                        // Successful query
                        Utility.storeUserProfile((RegistrationResponse) convert);
                    }
                } catch (Exception e) {
                    Utility.showToast("server not responded");
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile, menu);
        showGlobalContextActionBar(OraInteractiveApp.getApp().getResources()
                .getString(R.string.edit_profile));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_save:
                updateUserProfile();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
