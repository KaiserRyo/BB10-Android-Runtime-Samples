/**
 * Copyright (c) 2015 BlackBerry Limited.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.afrolkin.googlemapswebviewsample;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    LocationListener mLocationListener;
    LocationManager locationManager;
    WebView webView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        context = this;

        // References to layout elements
        webView = (WebView) findViewById(R.id.maps);
        Button submit = (Button) findViewById(R.id.submit);
        Button locate = (Button) findViewById(R.id.locate);
        final EditText location = (EditText) findViewById(R.id.location);

        // WebView configuration
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setJavaScriptEnabled(true);

        // Loads url of static map image of downtown New York City with a few markers on first start
        String url = "https://maps.googleapis.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=13&size=400x200&maptype=roadmap\n" +
                "&markers=color:blue%7Clabel:S%7C40.702147,-74.015794&markers=color:green%7Clabel:G%7C40.711614,-74.012318\n" +
                "&markers=color:red%7Clabel:C%7C40.718217,-73.998284&scale=2";

        // Some additional configuration to fit the WebView content across the screen on any device
        String data = "<html><body style=\"margin: 0; padding: 0\"><img id=\"resizeImage\" src=\"" + url + "\" width=\"100%\" alt=\"\" align=\"middle\" /></body></html>";
        webView.loadData(data, "text/html; charset=UTF-8", null);

        // Click listener for geocoding search
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasText(location)) {
                    if (mLocationListener != null) {
                        locationManager.removeUpdates(mLocationListener);
                    }

                    // Generate a url for the WebView using the desired location
                    String url = "https://maps.googleapis.com/maps/api/staticmap?center=" + location.getText().toString() + "&size=400x200&maptype=roadmap&zoom=13&scale=2";

                    String data = "<html><body style=\"margin: 0; padding: 0\"><img id=\"resizeImage\" src=\"" + url + "\" width=\"100%\" alt=\"\" align=\"middle\" /></body></html>";
                    webView.loadData(data, "text/html; charset=UTF-8", null);
                } else {
                    Toast.makeText(context, "Location field blank", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Click listener for geolocation
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define a listener that responds to location updates
                mLocationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.
                        updateLocation(location);
                        // Prevent LocationManager from getting further location updates
                        locationManager.removeUpdates(this);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        Toast.makeText(context, "LocationManager Status changed to " + status, Toast.LENGTH_SHORT).show();
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

                // Register the listener with the Location Manager to receive location updates
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            }
        });
    }

    // Check if EditText is empty
    public static boolean hasText(EditText et) {
        String s = et.getText().toString();
        return !s.equals("");
    }

    // Update location of map WebView to Location object L
    private void updateLocation(Location l) {
        double latitude = l.getLatitude();
        double longitude = l.getLongitude();

        String url = "https://maps.googleapis.com/maps/api/staticmap?&scale=2&zoom=13&size=400x200&maptype=roadmap\n" +
                "&markers=color:red%7Clabel:Location%7C" + latitude + "," + longitude;

        String data = "<html><body style=\"margin: 0; padding: 0\"><img id=\"resizeImage\" src=\"" + url + "\" width=\"100%\" alt=\"\" align=\"middle\" /></body></html>";
        webView.loadData(data, "text/html; charset=UTF-8", null);
    }
}
