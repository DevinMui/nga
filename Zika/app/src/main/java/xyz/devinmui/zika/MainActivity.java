package xyz.devinmui.zika;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements com.mapbox.mapboxsdk.location.LocationListener {

    private MapView mapView;

    private MapboxMap map;

    int dataPoints = 0;

    Api mApi = new Api();

    Boolean data = false;

    LocationServices locationServices;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);

        MapboxAccountManager.start(this, "pk.eyJ1IjoiZGV2aW5tdWkiLCJhIjoiY2lyam0zOWMwMDAybGY5bTY0am5qbHdmOSJ9.ZEMl1ywHqfRO5MyMv3CHQA");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        locationServices = LocationServices.getLocationServices(MainActivity.this);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                enableLocation(true);
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude))
                                .zoom(14)
                                .build()
                ));
                mApi.get("/data", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        try {
                            JSONArray jsonArr = new JSONArray(res);
                            dataPoints = jsonArr.length();
                            for (int i = 0; i < jsonArr.length(); i++) {
                                JSONObject json = jsonArr.getJSONObject(i);
                                final String disease = json.getString("disease");
                                final double latitude = json.getDouble("lat");
                                final double longitude = json.getDouble("long");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        map.addMarker(new MarkerViewOptions()
                                                .position(new LatLng(latitude, longitude))
                                                .title(disease)
                                                .snippet(disease + " has cases here")
                                        );
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                UpdateTask task = new UpdateTask(context);
                task.execute();
            }
        });
    }

    private void enableLocation(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String email = "";
        String json = "{\"email\": \"" + email + "\", \"lat\": " + latitude + ", \"long\": " + longitude + "}";
        try {
            mApi.post("/geodata", json, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // kk thats all folks
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onFabPress(View v) {
        // add data
        if (data) {
            data = false;
            // wipe all annotations and stuff
            // add normal annotations
        } else {
            data = true;
            // add predictive annotations
        }

    }

    private class GeoTask extends AsyncTask<String, Void, String> {
        private Context mContext;

        public GeoTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            while(true) {
                // do stuff
                String email = "devinmui@yahoo.com";
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null;
                }
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String json = "{\"user_email\": \"" + email + "\", \"lat\": " + latitude + ", \"long\": " + longitude + " }";
                try {
                    mApi.post("/data", json, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class UpdateTask extends AsyncTask<String, Void, String> {
        private Context mContext;

        public UpdateTask (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            // do stuff
            while(true) {
                System.out.println("while loop");
                mApi.get("/data", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        try {
                            JSONArray jsonArr = new JSONArray(res);
                            if (jsonArr.length() > dataPoints) {
                                // reparse!
                                dataPoints = jsonArr.length();
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    final JSONObject json = jsonArr.getJSONObject(i);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                map.addMarker(new MarkerViewOptions()
                                                        .position(new LatLng(json.getDouble("lat"), json.getDouble("long")))
                                                        .title(json.getString("disease"))
                                                        .snippet(json.getString("disease") + " has cases here")
                                                );
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
    }
}
