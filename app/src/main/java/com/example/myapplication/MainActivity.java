package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the MainActivity which is responsible for the splash screen
 */
public class MainActivity extends AppCompatActivity {
    //Activity objects
    Handler handler;
    SensorManager sensorManager;

    /**
     * This the onCreate method
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         *  the default instance of each sensor type is retrieved from the system service SENSOR_SERVICE by
         * calling getSystemService(Context.SENSOR_SERVICE) method. The presence of the sensors need to be verified so
         * the instances of each sensor are needed.
         */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor sensorAccelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorGyroscope=sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor sensorOrientation=sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        /**
         * Hides the action bar if it is present in the activity
         */
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        /**
         * if any of the mentioned sensors aren't detected the app won't proceed to the main screen and will
         * display a message saying that the sensors aren't detected
         *
         */
        if(sensorLight!=null && sensorAccelerometer!=null && sensorGyroscope!=null && sensorOrientation!=null){
        handler =new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);}
        else{
            Toast.makeText(MainActivity.this, "No sensors detected,unable to start the app!",
                    Toast.LENGTH_LONG).show();
        }

    }
}