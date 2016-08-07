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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

import ch.uepaa.p2pkit.P2PKitClient;
import ch.uepaa.p2pkit.StatusResultHandling;
import ch.uepaa.p2pkit.discovery.InfoTooLongException;
import ch.uepaa.p2pkit.discovery.P2PListener;
import ch.uepaa.p2pkit.discovery.entity.Peer;
import ch.uepaa.p2pkit.internal.P2PKitStatusCallback;
import ch.uepaa.p2pkit.internal.StatusResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements com.mapbox.mapboxsdk.location.LocationListener {

    private MapView mapView;

    private MapboxMap map;

    int dataPoints = 0;

    JSONObject p2pdata;

    Boolean low_data = false;

    Api mApi = new Api();

    Boolean data = false;

    LocationServices locationServices;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);

        final ch.uepaa.p2pkit.StatusResult result = P2PKitClient.isP2PServicesAvailable(this);
        if (result.getStatusCode() == StatusResult.SUCCESS) {
            P2PKitClient client = P2PKitClient.getInstance(this);
            client.enableP2PKit((ch.uepaa.p2pkit.P2PKitStatusCallback) mStatusCallback, "6265c2d1b4d94942bfc1caf995c86885");
        } else {
            StatusResultHandling.showAlertDialogForStatusError(this, result);
        }

        P2PKitClient.getInstance(context).getDiscoveryServices().addP2pListener(mP2pDiscoveryListener);
        try {
            P2PKitClient.getInstance(this).getDiscoveryServices().setP2pDiscoveryInfo(p2pdata.toString().getBytes()); // all the data you have
        } catch (InfoTooLongException e) {
            Log.e("P2PListener", "The discovery info is too long");
        }
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
                            for(int i=0;i<jsonArr.length();i++){
                                JSONObject json = jsonArr.getJSONObject(i);
                                final String email = json.getString("email");
                                final String disease = json.getString("disease");
                                final double latitude = json.getDouble("lat");
                                final double longitude = json.getDouble("long");
                                JSONArray p2pArr = p2pdata.getJSONArray("previously_connected");
                                p2pArr.put(p2pArr.length(), json);
                                p2pdata.put("previously_connected", p2pArr);
                                try {
                                    P2PKitClient.getInstance(context).getDiscoveryServices().setP2pDiscoveryInfo(p2pdata.toString().getBytes()); // all the data you have
                                } catch (InfoTooLongException e) {
                                    Log.e("P2PListener", "The discovery info is too long");
                                }
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
    private final P2PListener mP2pDiscoveryListener = new P2PListener() {
        @Override
        public void onP2PStateChanged(final int state) {
            Log.d("P2PListener", "State changed: " + state);
        }

        @Override
        public void onPeerDiscovered(final Peer peer) {
            Log.d("P2PListener", "Peer discovered: " + peer.getNodeId() + " with info: " + new String(peer.getDiscoveryInfo()));
            String str = new String(peer.getDiscoveryInfo());
            try {
                JSONObject json = new JSONObject(str);
                // { "previously_connected": [ {} ], "user": { "email": ... }, "low_data": true/false }

                JSONArray jsonArr = p2pdata.getJSONArray("previously_connected");
                jsonArr.put(jsonArr.length(), json.getJSONObject("user"));
                JSONArray prevArr = json.getJSONArray("previously_connected");
                JSONArray combined = concatArray(prevArr, jsonArr);
                p2pdata.put("previously_connected", combined);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onPeerLost(final Peer peer) {
            Log.d("P2PListener", "Peer lost: " + peer.getNodeId());
        }

        @Override
        public void onPeerUpdatedDiscoveryInfo(Peer peer) {
            Log.d("P2PListener", "Peer updated: " + peer.getNodeId() + " with new info: " + new String(peer.getDiscoveryInfo()));
            String str = new String(peer.getDiscoveryInfo());
            try {
                JSONObject json = new JSONObject(str);
                // { "previously_connected": [ {} ], "user": { "email": ... }, "low_data": true/false }

                JSONArray jsonArr = p2pdata.getJSONArray("previously_connected");
                jsonArr.put(jsonArr.length(), json.getJSONObject("user"));
                JSONArray prevArr = json.getJSONArray("previously_connected");
                JSONArray combined = concatArray(prevArr, jsonArr);
                p2pdata.put("previously_connected", combined);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProximityStrengthChanged(Peer peer) {
            Log.d("P2pListener", "Peer " + peer.getNodeId() + " changed proximity strength: " + peer.getProximityStrength());
        }
    };

    private JSONArray concatArray(JSONArray arr1, JSONArray arr2)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }

    private final P2PKitStatusCallback mStatusCallback = new P2PKitStatusCallback() {
        @Override
        public void onEnabled() {
            // ready to start discovery
        }

        @Override
        public void onSuspended() {
            // p2pkit is temporarily suspended
        }

        @Override
        public void onResumed() {
            // coming back from a suspended state
        }

        @Override
        public void onDisabled() {
            // p2pkit has been disabled
        }

        @Override
        public void onError(StatusResult statusResult) {
            // enabling failed, handle statusResult
        }
    };

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

    public void onFabPress(View v){
        // add data
        if(data){
            data = false;
            // wipe all annotations and stuff
            // add normal annotations
        } else {
            data = true;
            // add predictive annotations
        }

    }

    public void onLowDataPress(View v){
        if(low_data){
            Toast.makeText(v.getContext(), "Low data mode is off",
                    Toast.LENGTH_LONG).show();
            low_data = false;
        } else {
            Toast.makeText(v.getContext(), "Low data mode is on",
                    Toast.LENGTH_LONG).show();
            low_data = true;
        }
    }

    private class GeoTask extends AsyncTask<String, Void, String> {
        private Context mContext;

        public GeoTask (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            // do stuff
            return null;
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
                                    JSONArray p2pArr = p2pdata.getJSONArray("previously_connected");
                                    p2pArr.put(p2pArr.length(), json);
                                    p2pdata.put("previously_connected", p2pArr);
                                    try {
                                        P2PKitClient.getInstance(mContext).getDiscoveryServices().setP2pDiscoveryInfo(p2pdata.toString().getBytes()); // all the data you have
                                    } catch (InfoTooLongException e) {
                                        Log.e("P2PListener", "The discovery info is too long");
                                    }
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
