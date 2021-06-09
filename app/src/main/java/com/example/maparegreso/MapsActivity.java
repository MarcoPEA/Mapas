package com.example.maparegreso;

import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    Boolean Actualpositoon = true;
    JSONObject jso;
    Double lonini=20.153698, latini=-101.189113, lonfin = 20.138839, latfin = -101.183222;
    LatLng position;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng casa = new LatLng(lonfin, latfin);
        mMap.addMarker(new MarkerOptions()
                .position(casa)
                .title("trabajo"));
        LatLng trabajo = new LatLng(lonini, latini);
        googleMap.addMarker(new MarkerOptions()
                .position(trabajo)
                .title("casa"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(casa);
        mMap.animateCamera(cameraUpdate);
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(lonfin, latfin))
                .zoom(18)
                .tilt(67)
                .bearing(90).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(20.153698, -101.189113),
                        new LatLng(20.153400, -101.188113),
                        new LatLng(20.153130, -101.187113),
                        new LatLng(20.143698, -101.186113),
                        new LatLng(20.143900, -101.185113),
                        new LatLng(20.138839, -101.183222)));

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+latini+","+lonini+"&destination="+latfin+","+lonfin+"&key="+"AIzaSyAaGqpcHnzdAXe4iNqiEmAUy5PWfoV9UM4"+"";
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    jso = new JSONObject(response);
                    trasaruta(jso);
                    Log.i("JsonRuta:", ""+response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    private void trasaruta(JSONObject jso) {

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            jRoutes = jso.getJSONArray("routes");
            for (int i=0; i<jRoutes.length();i++){

                jLegs = ((JSONObject)jRoutes.get(i)).getJSONArray("legs");

                for (int j=0; j<jLegs.length(); j++){

                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");

                    for (int k=0; k<jSteps.length(); k++){
                        String polyline = ""+((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");

                        Log.i("end",""+polyline);
                        List<LatLng> list = PolyUtil.decode(polyline);
                        mMap.addPolyline(new PolylineOptions().addAll(list).color(Color.GREEN).width(5));
                    }

                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lonfin, latfin), 15));
clienteFusedLocation();
    }

    private void clienteFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d("MIXUBICA", "Lat: " + location.getLatitude() +
                                    " , lon" + location.getLongitude());
                            Toast.makeText(getBaseContext(), "Lat: " + location.getLatitude() +
                                    " , lon" + location.getLongitude(), Toast.LENGTH_LONG)
                                    .show();
                            latini = location.getLatitude();
                            lonini = location.getLongitude();
                            LatLng ubi = new LatLng(latini, lonini);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(ubi);
                            CameraPosition cameraPosition = CameraPosition.builder()
                                    .target(new LatLng(20.153857, -101.188215))
                                    .zoom(18)
                                    .tilt(67)
                                    .bearing(90).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                });
    }

}

