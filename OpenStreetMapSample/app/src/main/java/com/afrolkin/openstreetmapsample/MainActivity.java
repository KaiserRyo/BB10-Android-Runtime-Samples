/**
 * Copyright (c) 2015 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.afrolkin.openstreetmapsample;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private MapView mapView;
    private IMapController mapController;
    LocationListener mLocationListener;
    LocationManager locationManager;
    Geocoder geocoder;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // Make status bar transparent if supported
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // Acquire a reference to the system Location Manager and create a new Geocoder
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());

        // References to layout elements
        Button submit = (Button) findViewById(R.id.submit);
        Button locate = (Button) findViewById(R.id.locate);
        final EditText location = (EditText) findViewById(R.id.location);
        mapView = (MapView) findViewById(R.id.mapview);

        // MapView initial config
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(25);

        // Centre map near to Hyde Park Corner, London on first start
        GeoPoint gPt = new GeoPoint(51500000, -150000);
        mapController.setCenter(gPt);

        // Click listener for geocoding search
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorMessage = "";
                List<Address> addresses = null;

                // Attempt to get location using geocoder
                try {
                    addresses = geocoder.getFromLocationName(location.getText().toString(), 1);
                } catch (IOException ioException) {
                    // Catch network or other I/O problems.
                    errorMessage = "service not available";
                    Log.e("ERROR", errorMessage, ioException);
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                    errorMessage = "invalid lat and long used";
                    Log.e("ERROR", errorMessage, illegalArgumentException);
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                }

                // Handle case where no address was found.
                if (addresses == null || addresses.size() == 0) {
                    if (errorMessage.isEmpty()) {
                        errorMessage = "no address found";
                        Log.e("ERROR", errorMessage);
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    Log.i("SUCCESS", "address found");

                    GeoPoint gPt = new GeoPoint(address.getLatitude(), address.getLongitude());
                    mapController.setCenter(gPt);
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
                        locationManager.removeUpdates(this);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
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

    // Update location of map to Location object l
    private void updateLocation(Location l) {
        double latitude = l.getLatitude();
        double longitude = l.getLongitude();
        GeoPoint gPt = new GeoPoint(latitude, longitude);

        mapController.setZoom(50);
        mapController.setCenter(gPt);
    }
}
