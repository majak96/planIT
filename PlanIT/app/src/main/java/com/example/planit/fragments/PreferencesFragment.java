package com.example.planit.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.planit.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
