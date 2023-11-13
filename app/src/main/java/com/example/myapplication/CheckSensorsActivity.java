package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ListView;
import java.util.List;

/**
 * This is a public class called CheckSensorsActivity
 */
public class CheckSensorsActivity extends AppCompatActivity {
    /**
     * This is the onCreate method
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sensors);
        /**
         * this method is used to hide the action bar if the activity has an
         * action bar
         */
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        /**
         * The SensorManager instance is created by calling the
         * getSystemService() method with the SENSOR_SERVICE constant as an argument.
         */
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /**
         * the getSensorList() method is called on the SensorManager
         * instance with the Sensor.TYPE_ALL constant as an argument, which
         * returns a list of all available sensors.
         */
        List<Sensor> sensors  = sensorManager.getSensorList(Sensor.TYPE_ALL);
        /**
         * ListView instance is created with the ID list
         */
        ListView list= findViewById(R.id.list);
        /**
         * The custom adapter (SensorsAdapter) is set on it using the
         * setAdapter() method. The adapter is responsible for providing
         * the data to the ListView to display the sensors' information
         * in a customized way using the row_item layout.
         */
        list.setAdapter(new SensorsAdapter(this, R.layout.row_item, sensors));
    }
}