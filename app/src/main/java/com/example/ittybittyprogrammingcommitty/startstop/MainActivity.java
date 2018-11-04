package com.example.ittybittyprogrammingcommitty.startstop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/* Imports for location */
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import java.util.Locale;


/* Imports for weather api */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements LocationListener{

    /* This is a test message for debugging. Shows coordinates and output of weather api */
    TextView message;

    TextView currentTemperatureField, currentWindSpeedField, textView3;
    /* Openweathermap api key */
    String OPEN_WEATHER_MAP_API = "3d58a04d89afa4a0dab92c4e6490991c";
    double longitude =  157.8583;
    double latitude = 21.3069;

    Location location;



    /* Makin buttons */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button Startbtn = (Button) findViewById(R.id.Start);


/* This below is location manager, will ignore for now and use predefined lat/long */
        /* This forces user to give permission for location, or fails */
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
/* End location mgr */

/* this line below begins the Weather API query */
        taskLoadUp("Dhaka, BD");


        /* When clicking start button, do following */
        Startbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TextView message = (TextView) findViewById(R.id.message);
                message.setText("Started");

                /* get the location, print */
                //longitude = location.getLongitude();
                //latitude = location.getLatitude();
                //message.setText("Latitude: " + latitude + "\n Longitude: " + longitude);

            }
        });


        Button Stopbtn = (Button) findViewById(R.id.Stop);
        Stopbtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                TextView message = (TextView)findViewById(R.id.message);
                message.setText("Stopped");
            }
        });


    }

    /* Begin overrides for LocationListener */

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {
        message.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            message.setText(message.getText() + "\n"+addresses.get(0).getAddressLine(0)+", "+
                    addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
        }catch(Exception e)
        {

        }

    }



/* Begin random stuff that was here on creation of generic main activity. Probably not used */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
/* end of defaults */



    /*
    !---------------- Beginning Weather API shenanigans ----------------!
     */

    public void taskLoadUp(String query) {
        if (WeatherAPI.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();

            /* ------------------------------
            This is where it breaks vvvvv commented out for now
           ----------------------------------
             */
            //task.execute(query);
            /* I have no idea where this execute thing is location. Searched here as well as WeatherAPI java */


        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }


    class DownloadWeather extends AsyncTask < String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        protected String doInBackground(String...args) {


            String xml = WeatherAPI.excuteGet("http://api.openweathermap.org/data/2.5/weather?lat=" +latitude + "&lon=" + longitude +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);

            //Below defaults code. Can use this to test if lat long not working
            // String xml = WeatherAPI.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + "Dhaka, BD" +
            //         "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            //Alternatively to test, can try to display this xml
            return xml;


        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    JSONObject wind = json.getJSONObject("wind");

                    DateFormat df = DateFormat.getDateTimeInstance();

                    //Pulling temperature and windspeed.
                    //This potion not yet tested due to brokenness
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                    currentWindSpeedField.setText("Windspeed: " + wind.getString("speed") + "mph");

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }
        }
    }






}
