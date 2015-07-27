package com.ora.interactive.chalenge.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ora.interactive.chalenge.R;
import com.ora.interactive.chalenge.beans.RegistrationErrorWrapper;
import com.ora.interactive.chalenge.beans.RegistrationResponse;
import com.ora.interactive.chalenge.controller.Config;
import com.ora.interactive.chalenge.network.LoadServices;
import com.ora.interactive.chalenge.network.Service;
import com.ora.interactive.chalenge.utilities.Utility;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInFragment extends FragmentBase {
    private static final String LOG = SignInFragment.class.getName();
    @Bind(R.id.emailAccount)
    EditText emailAccount;
    @Bind(R.id.mailPassword)
    EditText mailPassword;

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        return fragment;
    }

    public SignInFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        ButterKnife.bind(this, view);
    }

    @OnClick({ R.id.sign })
    public void onClickView(View view) {
        switch(view.getId()) {
            case R.id.sign:
                if (isProcessing) return;
                Map map = new HashMap();
                map.put("email", emailAccount.getText().toString());
                map.put("password", mailPassword.getText().toString());
                login(map);
                break;
        }
    }

    protected void login(Map map) {
        Service mService = new Service();
        mService.setServiceCode(Config.POST_USER_LOGIN_CODE);
        mService.setServiceName(Config.POST_USER_LOGIN);
        mService.setServiceType(Config.POST);
        mService.setHeaders(Utility.getJsonAccess());
        mService.setServiceInput(map);
        mService.setNotificationTask(this);
        new LoadServices().loadOnExecutor(mService);
        openProgressBar(true);
    }

    @Override
    public void completed(Service response) {
        openProgressBar(false);

        switch(response.getServiceCode()) {

            case Config.POST_USER_LOGIN_CODE: {
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

                        // Move to User Session
                        Utility.sendUserInitiatedSession();
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
}
