package com.example.planit.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.planit.R;

public class FragmentTransition {

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, int view, boolean addToBackStack) {
        FragmentTransaction transaction = activity.getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(view, fragment);

        if(addToBackStack){
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}
