package com.datacomm.gpstrack;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;



public class GpsInfo extends Service implements LocationListener {
    private static final long MIN_DISTANCE_UPDATES = 2;
    private static final long MIN_TIME_UPDATES = 3000;

    private final Context mContext;
    boolean GPSEnabled = false;
    static boolean NetworkEnabled = false;
    boolean GetLocationEnabled = false;

    private Location mylocation;




    protected LocationManager locManager = null;

    //constructor - initialize context
    public GpsInfo(Context c) {
        this.mContext = c;
    }

    //start to get location
    public void getLocation() {
        //permission check
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }

        try {

            //check if the service is possible or not
            locManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATES,MIN_DISTANCE_UPDATES,this);
            GPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            NetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.e("CHECK", "getLoc");
            Criteria cr = new Criteria();

            String provider = locManager.getBestProvider(cr, false);

            //if GPS turned off
            if (!GPSEnabled) {
                showSettingsAlert();
            }

            //if any one possible to read
            if (GPSEnabled || NetworkEnabled) {
                this.GetLocationEnabled = true;

                //gps is priority
                if (GPSEnabled) {
                    Log.e("CHECK", "GPS ENABLE");
                    if (mylocation == null) {
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this);
                        if (locManager != null) {
                            mylocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
                //if gps cannot get value try network provider
                if ((!GPSEnabled || mylocation==null) && NetworkEnabled) {
                    Log.e("CHECK", "reqLoc NW");
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this);
                    if (locManager == null) {
                        mylocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
            else if(provider!=null && !provider.equals("")){
                mylocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locManager.requestLocationUpdates(provider, MIN_DISTANCE_UPDATES, MIN_TIME_UPDATES, this);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    //stop updating
    public void stopUsingGPS() {
        if (locManager != null) {
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return ;
            }
            GetLocationEnabled = false;
            locManager.removeUpdates(GpsInfo.this);
        }
    }
    public static boolean getNetwork(){
        return NetworkEnabled;
    }
    //return current location
    public Location getLatLng(){
        return mylocation;
    }


    //open setting menu when the location service is off
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS use setting");
        alertDialog.setMessage("GPS setting on?");

        //check setting Location
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
        //cancle;
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


    public boolean isGetLocation(){
        return this.GetLocationEnabled;
    }


    //update location every 1000ms(=MIN_TIME_UPDATES) or at least 1 m apart(MIN_DISTANCE_UPDATES)
    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Toast.makeText(mContext, "Provider Out of Service", Toast.LENGTH_LONG);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Toast.makeText(mContext, "Provider Temporarily Unavailable", Toast.LENGTH_LONG);
                break;
            case LocationProvider.AVAILABLE:
                Toast.makeText(mContext, "Provider Available", Toast.LENGTH_LONG);
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


