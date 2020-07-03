package com.example.planit.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.example.planit.R;

public class LocationDialog extends AlertDialog.Builder {

    public LocationDialog(Context context) {
        super(context);

        setUpDialog();
    }

    private void setUpDialog() {
        setTitle(R.string.location_disabled_title);
        setMessage(R.string.location_disabled_message);
        setCancelable(false);

        setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //go to settings to turn on location
                getContext().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.dismiss();
            }
        });

        setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

    }

    public AlertDialog prepareDialog() {
        AlertDialog dialog = create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
