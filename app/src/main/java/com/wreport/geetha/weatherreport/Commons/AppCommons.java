package com.wreport.geetha.weatherreport.Commons;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppCommons {
    public static String LOGTEXT="com.weather.report";
    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= connectivityManager.getActiveNetworkInfo();
        return networkInfo.isConnected();
    }
}
