package com.example.planit.utils;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class Utils {

    public static String getDate(long time_stamp_server) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd:mm");
        return formatter.format(time_stamp_server);
    }

    public static String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

}
