package com.example.patrik.apiarytest;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.GenericSignatureFormatError;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    TableLayout mainTable;
    TableRow row;
    HTTPRequest productRequest;
    HTTPRequest venueRequest;
    ArrayList<Venue> venueList;
    ArrayList<Product> productList;
    private static final int GPS_PERMISSION = 1;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                updateDistances(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        requestGPSPermission();

        preferences = getPreferences(MODE_PRIVATE);
        mainTable = (TableLayout)findViewById(R.id.mainTable);
        venueList = new ArrayList<>();
        productList = new ArrayList<>();
        getFromPreferences();




        productRequest = new HTTPRequest("getProducts");
        productRequest.execute("http://private-anon-d0ed5491d-mobile35.apiary-mock.com/products");
        venueRequest = new HTTPRequest("getVenues");
        venueRequest.execute("http://private-anon-d0ed5491d-mobile35.apiary-mock.com/venues");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            requestGPSPermission();
        }
    }

    public void requestGPSPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS_PERMISSION);

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void getFromPreferences() {
        Gson gson = new Gson();
        String jsonVenues = preferences.getString("venues", "");
        if (jsonVenues != "") {
            venueList = gson.fromJson(jsonVenues,
                    new TypeToken<ArrayList<Venue>>() {
                    }.getType());
        }
        String jsonProducts = preferences.getString("products", "");
        if (jsonProducts != "") {
            productList = gson.fromJson(jsonProducts,
                    new TypeToken<ArrayList<Product>>() {
                    }.getType());
        }
        if (venueList != null && productList != null)
        updateTable();


    }

    public void saveToPreferences(ArrayList array, String type) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(array);
        editor.putString(type, json);
        editor.commit();
    }

    /**public void createCustomOverlayView(View view) {
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int displayHeight = getResources().getDisplayMetrics().heightPixels;

        ViewGroup root = (ViewGroup)mainTable.getParent();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        RelativeLayout customOverlay = (RelativeLayout) layoutInflater.inflate(R.layout.overlay_view, null, false);
        TextView venueNameTextView = (TextView)customOverlay.findViewById(R.id.nameTextView);
        TextView productsTextView = (TextView)customOverlay.findViewById(R.id.productsTextView);
        TextView distanceTextView = (TextView)customOverlay.findViewById(R.id.distanceTextView);

        TextView viewNameTextView = (TextView)view.findViewById(R.id.venueNameTextView);
        TextView viewProductsTextView = (TextView)customOverlay.findViewById(R.id.productsTextView);
        TextView viewDistanceTextView = (TextView)customOverlay.findViewById(R.id.distanceTextView);

        venueNameTextView.setText(viewNameTextView.getText().toString());
        productsTextView.setText(viewProductsTextView.getText().toString());
        distanceTextView.setText(viewDistanceTextView.getText().toString());

        root.addView(customOverlay);
    }**/
    public void createCustomRowView(Venue venue, ArrayList<String> productsAvailable) {

        int displayWidth = getResources().getDisplayMetrics().widthPixels;

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        RelativeLayout customRow = (RelativeLayout)layoutInflater.inflate(R.layout.custom_row, null, false);

        TextView venueNameTextView = (TextView)customRow.findViewById(R.id.venueNameTextView);
        venueNameTextView.setMaxWidth(displayWidth / 2);
        TextView productsTextView = (TextView)customRow.findViewById(R.id.productsTextView);
        productsTextView.setMaxWidth(displayWidth / 2);

        String productsAvailableString = new String();
        if (productsAvailable.size() > 0) {
            for (int i = 0; i < productsAvailable.size(); i++) {
                productsAvailableString += productsAvailable.get(i);
                if (i + 1 < productsAvailable.size()) {
                    productsAvailableString += ", ";
                }
            }
        } else {
            productsAvailableString = "No products";
        }

        venueNameTextView.setText(venue.name);

        productsTextView.setText(productsAvailableString);

        mainTable.addView(customRow);
    }

    public void updateTable() {

        mainTable.removeAllViews();

        if (venueList.size() > 0 && productList.size() > 0) {
            for (int i = 0; i < venueList.size(); i++) {
                Venue venue = venueList.get(i);
                ArrayList<String> productsAvailable = new ArrayList<>();
                for (int j = 0; j < venue.categoryList.size(); j++) {
                    for (int k = 0; k < productList.size(); k++) {
                        Product product = productList.get(k);
                        if (product.categoryList.contains(venue.categoryList.get(j)) && !productsAvailable.contains(product.name)) {
                            productsAvailable.add(product.name);
                        }
                    }
                }
                createCustomRowView(venue, productsAvailable);
            }
        }
    }

    public void updateDistances(Location location) {

        for (int i = 0; i < venueList.size(); i++) {
            Location venueLocation = new Location("");
            venueLocation.setLatitude(venueList.get(i).latitude);
            venueLocation.setLongitude(venueList.get(i).longitude);
            int distance = (int)location.distanceTo(venueLocation);
            try {
                RelativeLayout customRow = (RelativeLayout) mainTable.getChildAt(i);
                TextView distanceTextView = (TextView) customRow.findViewById(R.id.distanceTextView);
                distanceTextView.setText(Integer.toString(distance) + "m");
            } catch (Exception e) {

            }
        }

    }

    public class HTTPRequest extends AsyncTask<String, Void, String> {
        JSONArray responseJSONArray;
        String request;
        public HTTPRequest(String request){
            this.request = request;
        }


        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url;
            try {
                url = new URL(params[0]);

                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bin = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bin.readLine();
                StringBuffer sb = new StringBuffer();
                while (line != null) {
                    sb.append(line);
                    sb.append("\r\n");
                    line = bin.readLine();
                }
                inputStream.close();
                String jsonString = sb.toString();

                responseJSONArray = new JSONArray(jsonString);

                Log.v("jsonArray", responseJSONArray.toString());
                Log.v("update", "doinbackgroud started");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.v("postExecute", "got here");


                switch (request) {
                    case "getProducts":
                        productList.clear();
                        break;
                    case "getVenues":
                        venueList.clear();
                        break;
                    default:
                }

                for (int i = 0; i < responseJSONArray.length(); i++) {
                    JSONObject responseJSONObject = responseJSONArray.getJSONObject(i);
                    String name = responseJSONObject.getString("name");
                    JSONArray categoriesJSON = responseJSONObject.getJSONArray("category_ids");
                    ArrayList<Integer> categoryList = new ArrayList<>();
                    for (int j = 0; j < categoriesJSON.length(); j++) {
                        try {
                            categoryList.add(categoriesJSON.getInt(j));
                        } catch (Exception e) {

                        }
                    }
                    switch (request) {
                        case "getProducts":

                            Product product = new Product(name, categoryList);
                            productList.add(product);
                            Log.v("jsonResponse", name);
                            break;
                        case "getVenues":
                            double longitude = responseJSONObject.getDouble("longitude");
                            double latitude = responseJSONObject.getDouble("latitude");

                            Venue venue = new Venue(name, longitude, latitude, categoryList);
                            venueList.add(venue);
                            Log.v("jsonResponse", name);
                            break;
                        default:
                        Log.v("hej", "nej");
                    }
                }

                updateTable();

            } catch (Exception e) {

            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveToPreferences(venueList, "venues");
        saveToPreferences(productList, "products");
    }
    public JSONObject getJsonObject(InputStream inputStreamObject) {

        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
            return jsonObject;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
