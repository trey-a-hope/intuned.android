package com.intuned.app.home;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.intuned.app.R;

import Configuration.AppConfig;

public class NotificationsFragmentController extends Fragment {
    private final String LOGTAG = "NotificationsFragmentController";
    private HomeController activity;
    private Firebase firebase;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (HomeController) getActivity();
        initFirebase();
        initUI(inflater, container);
        initObjects();
        setValues();
        return view;
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
    }

    private void initUI(LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_notifications_fragment_controller, container, false);
    }


    private void initObjects() {
    }

    private void setValues() {
    }


}
