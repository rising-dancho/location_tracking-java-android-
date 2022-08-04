package com.perez.gps_beta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    EditText lbl_longitude, lbl_latitude, lbl_address;
    Button bt_exit;
    String text_long, text_lat, text_address, sum_strings;


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //REQUEST LOCATION PERMISSION
                startService();
            }
        } else {
            //START THE LOCATION SERVICE
            startService();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    Toast.makeText(this, "This app needs permission to work properly", Toast.LENGTH_LONG).show();
                }
        }
    }

    public class LocationBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            //DISPLAYING LON AND LAT
            lbl_longitude = findViewById(R.id.lbl_longitude);
            lbl_latitude = findViewById(R.id.lbl_latitude);
            lbl_address = findViewById(R.id.lbl_address);
            bt_exit = findViewById(R.id.bt_copy);
            //DISABLE EDITTEXT
            lbl_longitude.setTextIsSelectable(false);
            lbl_longitude.setFocusable(false);
            lbl_longitude.setFocusableInTouchMode(false);
            lbl_longitude.setClickable(false);
            //DISABLE EDITTEXT
            lbl_latitude.setTextIsSelectable(false);
            lbl_latitude.setFocusable(false);
            lbl_latitude.setFocusableInTouchMode(false);
            lbl_latitude.setClickable(false);
            //DISABLE EDITTEXT
            lbl_address.setTextIsSelectable(false);
            lbl_address.setFocusable(false);
            lbl_address.setFocusableInTouchMode(false);
            lbl_address.setClickable(false);


            if (intent.getAction().equals("ACT_LOC")) {
                double latitude = intent.getDoubleExtra("latitude", 0f);
                double longitude = intent.getDoubleExtra("longitude", 0f);
                lbl_longitude.setText(Double.toString(longitude));
                lbl_latitude.setText(Double.toString(latitude));
                String address = getCompleteAddressString(latitude, longitude);
                lbl_address.setText(address);
            }


            //ONCLICK COPY BUTTON
            bt_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //COMPILE THE TEXTS
                    text_long = lbl_longitude.getText().toString();
                    text_lat = lbl_latitude.getText().toString();
                    //text_address = lbl_address.getText().toString();
                    sum_strings = "Latitude: " + text_lat +"\n\nLongitude: " + text_long /*" \n\nApprox. Address: " + text_address*/;

                    //COPY TO CLIPBOARD ALL TEXTS FROM EDITTEXTS
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("EditText", sum_strings);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Copied to Clipboard!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        //GETTING ADDRESS
        private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
            String strAdd = "";
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                    Log.w("address", strReturnedAddress.toString());
                } else {
                    Log.w("address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("address", "Can't get Address!");
            }
            return strAdd;
        }
    }

    //START LOCATING
    public void startService() {
        LocationBroadCastReceiver receiver = new LocationBroadCastReceiver();
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(receiver, filter);
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

}