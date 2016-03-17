package com.datacomm.gpstrack;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public TextView latTxt, lngTxt;
    Button homeBtn, startBtn;
    boolean start = false, init;
    private GpsInfo gps;
    String CltName, serverIp;
    int portNum;
    Location prevLoc;
    Network socketNtw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initVal();


    }


    //initialize xml file view and button work
    public void initVal()  {

        //get input value from main page
        Intent i = getIntent();
        CltName = i.getStringExtra("name");
        serverIp = i.getStringExtra("ip");
        portNum = i.getIntExtra("port", 51234);

        socketNtw = new Network(serverIp, portNum);

        //initialize global value.
        init = true;
        prevLoc = null;
        gps = new GpsInfo(LocationActivity.this);

        //set xml view
        latTxt = (TextView) findViewById(R.id.latVal);
        lngTxt = (TextView) findViewById(R.id.lngVal);

        homeBtn = (Button) findViewById(R.id.mainBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                Intent i = new Intent(LocationActivity.this, MainActivity.class);
                gps.stopUsingGPS();
                startActivity(i);
            }
        });

        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = !start;
                if (start) {
                    gps.getLocation();
                    if(gps.GetLocationEnabled) {
                        init = true;
                        socketNtw.connect();
                        new MapUpdate().execute();
                        startBtn.setText("STOP");
                    }
                    else
                        start = false;

                } else {
                    gps.stopUsingGPS();
                    startBtn.setText("START");

                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //initialize map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng bcit = new LatLng(49.2485, -123.0014);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bcit, 10));
    }



    private void drawMarker(Location loc){
        LatLng curPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
        //first point move the camera
        if(init){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curPosition, 16));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
            init = !init;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(curPosition));

        mMap.addMarker(new MarkerOptions().position(curPosition)
                .snippet("Lat:" + loc.getLatitude() + "Lng:" + loc.getLongitude())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(CltName));
    }




    /*
      map update thread
      get location every 1 second
      compare new location to previous location.
      If it is same, not update.
      If not update 30 times, just update same spot again.
    */
    private class MapUpdate extends AsyncTask<String, Location, Location> {
        int cnt =  30;
        Location curLoc;

        @Override
        protected Location doInBackground(String... params) {
            while(true) {
                if (start) {
                    curLoc = gps.getLatLng();
                    if(prevLoc == null){
                        prevLoc = curLoc;
                    }
                    else if(cnt ==0 || (prevLoc.getLatitude() != curLoc.getLatitude() || prevLoc.getLongitude()!=curLoc.getLongitude())) {
                        cnt = 30;

                        String latStr = Double.toString(curLoc.getLatitude());
                        String lngStr = Double.toString(curLoc.getLongitude());

                        //send locationvariable to onProgressUpdate
                        publishProgress(curLoc);

                        socketNtw.send(latStr, lngStr, CltName);
                        prevLoc = curLoc;
                    }
                    else {
                        cnt--;
                    }

                    //wait for a second.
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        //update XML view
        @Override
        protected void onProgressUpdate(Location... loc){
            Location myloc = loc[0];
            if(myloc != null) {
                latTxt.setText(Double.toString(myloc.getLatitude()));
                lngTxt.setText(Double.toString(myloc.getLongitude()));
                drawMarker(myloc);

                prevLoc = myloc;

                //JUST FOR CHECKING PROGRESS
                String lat = Double.toString(myloc.getLatitude());
                String lng = Double.toString(myloc.getLongitude());

                String s = lat + ", " + lng;
                Log.e("TEST on Progress", s);
            }
        }

        @Override
        protected void onPostExecute(Location loc){
            if(loc != null) {
               //close socket?!?!
            }
        }
    }



}

