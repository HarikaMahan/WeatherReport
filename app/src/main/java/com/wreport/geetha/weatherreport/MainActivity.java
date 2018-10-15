package com.wreport.geetha.weatherreport;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wreport.geetha.weatherreport.Commons.AppCommons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener {
    Context context;
    TextView textView,countrytv,descriptiontv;
    TextView tvtemperature,tvpressure,tvhumidity,tvwindspeed,tvdate;
    Button showLoactionbtn;
    LocationManager locationManager;
    Double longitude, latitude;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    String provider="";
    private RequestQueue requestQueue;
    LinearLayout linearLayer;
    ProgressBar progressBar;
    ImageView imageView;
    private String URL="https://api.openweathermap.org/data/2.5/weather?appid=d89973b8c1e88fab82a770c6ad8a14fa&lat=%f&lon=%f&units=metric";
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(AppCommons.LOGTEXT,"gpslistener");
            setLatLong(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);

        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(AppCommons.LOGTEXT,"networklistener");
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
            setLatLong(location);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        textView = findViewById(R.id.textView);
        countrytv = findViewById(R.id.tv_country);
        descriptiontv = findViewById(R.id.tv_description);
        tvtemperature=findViewById(R.id.tv_temp);
        tvpressure=findViewById(R.id.tv_pressure);
        tvhumidity=findViewById(R.id.tv_humidity);
        tvwindspeed=findViewById(R.id.tv_wind);
        tvdate=findViewById(R.id.tv_date);
        imageView = findViewById(R.id.imageView);
        progressBar=findViewById(R.id.progressBar);
        linearLayer=findViewById(R.id.linearLayer);
        showLoactionbtn=findViewById(R.id.button);

        requestQueue= getRequestQueue();
        getLocation(textView);
        progressBar.setVisibility(View.VISIBLE);
        linearLayer.setVisibility(View.VISIBLE);

    }
    private void setLatLong(Location location){
        Log.d(AppCommons.LOGTEXT,"setLatLong");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        textView.setText("Lotitude-" + latitude + "\nLogitude-" + longitude);
        URL=String.format(URL,latitude,longitude);
        Log.d(AppCommons.LOGTEXT,"setLatLong-URL-"+URL);
        getWeatherReport(textView,URL);
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
    public void getWeatherReport(View view,String url) {
        if (AppCommons.isConnected(context)) {
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(url,null,this,this  );
            requestQueue.add(jsonObjectRequest);

        } else {
            Snackbar.make(view, "No internet Connection Available", Snackbar.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            linearLayer.setVisibility(View.GONE);
        }
    }


    public void getLocation(View view) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        linearLayer.setVisibility(View.VISIBLE);
        showLoactionbtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            showLoactionbtn.setEnabled(true);
        }

       // Toast.makeText(context, " Gps enabled-"+gps_enabled+"\n network enabled- "+network_enabled,     Toast.LENGTH_LONG).show();

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled){
            Toast.makeText(context, " Gps or network is not enabled ",     Toast.LENGTH_LONG).show();
            linearLayer.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            showLoactionbtn.setEnabled(true);
        }
        if (network_enabled ){
            locationListener=locationListenerNetwork;
            provider=LocationManager.NETWORK_PROVIDER;
            showLocation(provider);
        }
        else if(gps_enabled) {
            locationListener=locationListenerGps;
            provider=LocationManager.GPS_PROVIDER;
            showLocation(provider);
              }

    }


    public Location showLocation(String provider) {

        Log.d(AppCommons.LOGTEXT,"showLocation-"+provider);

        Location location=null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
        }else {
             location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
        return location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1111: if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED
                            && grantResults[1]== PackageManager.PERMISSION_GRANTED)
            {
                Log.d(AppCommons.LOGTEXT,"in permission granted-"+provider);
                showLocation(provider);
            }else{
                Toast.makeText(context, "Permission is not granted", Toast.LENGTH_SHORT).show();
            }
                break;
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d(AppCommons.LOGTEXT,"json response"+response.toString());
        showLoactionbtn.setEnabled(true);
        try {
            progressBar.setVisibility(View.GONE);
            linearLayer.setVisibility(View.GONE);
            JSONObject mainJsonObj=response.getJSONObject("main");
            String areaname=response.getString("name");
            long dateval=response.getLong("dt");
            JSONObject sysJsonObj=response.getJSONObject("sys");
            JSONObject windJsonObj=response.getJSONObject("wind");
            JSONArray weatherArrJsonObj=response.getJSONArray("weather");
            JSONObject jsonWeatherObject=weatherArrJsonObj.getJSONObject(0);
            Log.d(AppCommons.LOGTEXT,jsonWeatherObject.toString());
            Log.d(AppCommons.LOGTEXT,mainJsonObj.toString());
            Log.d(AppCommons.LOGTEXT,"sys"+sysJsonObj.toString());
            Log.d(AppCommons.LOGTEXT,"wind"+windJsonObj.toString());
            Log.d(AppCommons.LOGTEXT,"dt-"+dateval);
            String imageurl="http://openweathermap.org/img/w/"+jsonWeatherObject.getString("icon")+".png";
            Log.d(AppCommons.LOGTEXT,imageurl);
            ImageRequest ir = new ImageRequest(imageurl, new Response.Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    imageView.setImageBitmap(response);

                }
            }, 0, 0, null, null);
            requestQueue.add(ir);

            WeatherPojo weatherPojo=new WeatherPojo();
            weatherPojo.setCityName(areaname);
            weatherPojo.setCountryName(sysJsonObj.getString("country"));
            weatherPojo.setWind(windJsonObj.getString("speed"));
            weatherPojo.setTemp(mainJsonObj.getString("temp"));
            weatherPojo.setHumidity(mainJsonObj.getString("humidity"));
            weatherPojo.setPressure(mainJsonObj.getString("pressure"));
            weatherPojo.setDescription(jsonWeatherObject.getString("description"));
            weatherPojo.setIcon(imageurl);
            weatherPojo.setSunrise(""+ getTime(sysJsonObj.getString("sunrise")));
            weatherPojo.setSunset(""+ getTime(sysJsonObj.getString("sunrise")));
            descriptiontv.setText(weatherPojo.getDescription());
            countrytv.setText(weatherPojo.getCityName()+", "+weatherPojo.getCountryName());
            tvtemperature.setText("Temperature : "+weatherPojo.getTemp()+" °C");
            tvhumidity.setText("Humidity : "+weatherPojo.getHumidity()+"%");
            tvpressure.setText("Pressure : "+weatherPojo.getPressure()+"hPa");
            tvwindspeed.setText("Wind Speed : "+weatherPojo.getWind()+" m/s");
            tvdate.setText("Date : "+getDateAndTime(dateval));




        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            linearLayer.setVisibility(View.GONE);
            showLoactionbtn.setEnabled(true);
            Log.d(AppCommons.LOGTEXT,"jsonresponse-error->>"+e.toString());
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        showLoactionbtn.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        linearLayer.setVisibility(View.GONE);
        Log.d(AppCommons.LOGTEXT,"volley -error->>"+error.toString());


    }
    private String getDateAndTime(long dateval){
        java.util.Date date=new java.util.Date(dateval*1000);
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
        String dateText = date_format.format(date);
        Log.d(AppCommons.LOGTEXT,"date()->>"+dateText);
        return dateText;
    }
    private String getTime(String timeval){
        long time=Long.parseLong(timeval);
        java.util.Date date=new java.util.Date(time*1000);
        SimpleDateFormat date_format = new SimpleDateFormat("hh.mm aa");
        String dateText = date_format.format(date);
        Log.d(AppCommons.LOGTEXT,"getTime->>"+dateText);
        return dateText;
    }


}
