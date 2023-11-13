package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * The parametersActivity is in charge of handling the automatic and the manual brightness mode
 * of the user's device
 */

public class ParametersActivity extends AppCompatActivity {
    /**
     * Declaration of several UI components which are used to interact with the
     * Brightness-related functionality in the app
     */

    private SeekBar brightnessSeekBar;
    //lets the user control the brightness of the screen manually
    private TextView lightLevel;
    //shows the light intensity sensed by the light sensor

    private SensorManager sensorManager;
    //SensorManager lets you access the device's sensors
    private ContentResolver cResolver;
    // Content resolver used as a handle to the system's settings
    private Window window;
    // window object that will store a reference to the current window
    CheckBox brightnessCheckBox;
    //when the brightnessCheckBox is ticked, automatic brightness settings will take over

    private int brightness;
    // Variable to store brightness value @param brightness


    private WindowManager.LayoutParams layoutParams;


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
        setContentView(R.layout.activity_parameters);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // hide native UI bar
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        //if there is no light sensor then the checkbox will be grayed
        if(sensor==null){
            brightnessCheckBox.setEnabled(false);
        }
        // UI components
        brightnessSeekBar  = findViewById(R.id.brightnessSeekBar);
        brightnessCheckBox = findViewById(R.id.brightnessCheckBox);

        lightLevel=findViewById(R.id.lightLevel);

        cResolver=getContentResolver(); // Get the  content Resolver
        window=getWindow(); // Get the current window

        // -------------------------- seekbar settings --------------------------

        //Set the  seekbar range between 0 and 255
        brightnessSeekBar.setMax(255);
        //set the seekbar progress to 1
        brightnessSeekBar.setKeyProgressIncrement(1);
        layoutParams = getWindow().getAttributes();

        //Get the brightness of the current system
        brightness= Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0);

        //Set the progress of the seek bar based on the system's brightness
        brightnessSeekBar.setProgress(brightness);

        brightnessCheckBox.setOnClickListener(view -> {
            /**
             * if the brightnessCheckBox is ticked the brightnessSeekBar in charge of the manual brightness mode
             * will be disabled and the automatic brightness mode will be activated
             */
            if(brightnessCheckBox.isChecked()){
                brightnessSeekBar.setEnabled(false);
            } else {
                brightnessSeekBar.setEnabled(true);

                brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {
                    /**
                     * //Nothing handled here
                     * @param seekBar The SeekBar in which the touch gesture began
                     */
                    public void onStopTrackingTouch(SeekBar seekBar){}

                    /**
                     *  //Nothing handled here
                     * @param seekBar The SeekBar in which the touch gesture began
                     */
                    public void onStartTrackingTouch(SeekBar seekBar){}

                    /**
                     * This method changes the brightness of the device according to the level which is set
                     * on the brightness seekbar
                     * @param seekBar The SeekBar whose progress has changed
                     * @param progress The current progress level.
                     * @param fromUser True if the progress change was initiated by the user.
                     */
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                    {
                        //Set the minimal brightness level
                        //if seek bar is 20 or any value below
                        boolean canWrite=Settings.System.canWrite(ParametersActivity.this);
                        if(canWrite){
                            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,progress);
                        } else{
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            startActivity(intent);
                        }
                        if(progress<=20)
                        {
                            brightness=20; //Set the brightness to 20
                        }
                        else //brightness is greater than 20
                        {
                            brightness = progress; //Set brightness variable based on the progress bar
                        }
                    }
                });
            }
        });

    }

    private SensorEventListener listener = new SensorEventListener() {
        /**
         * The method in charge of handling the automatic brightness mode
         * it also gets the value of the light intensity sensed by the light sensor
         * @param event the {@link android.hardware.SensorEvent SensorEvent}.
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The value of the first subscript in the values array is the current light intensity
            float value = event.values[0];
            lightLevel.setText("Current light level is " + value + " lx");
            //automatic brightness mode
            if (brightnessCheckBox.isChecked()){
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    float lightLevel = event.values[0];

                    if (lightLevel < 10) {
                        layoutParams.screenBrightness = 0.1f;
                    } else if (lightLevel < 1000) {
                        layoutParams.screenBrightness = 0.5f;
                    } else {
                        layoutParams.screenBrightness = 1.0f;
                    }

                    getWindow().setAttributes(layoutParams);
                }


            }

        }

        /**
         * nothing is handled here
         * @param sensor the object which is a type Sensor
         * @param accuracy The new accuracy of this sensor, one of
         *         {@code SensorManager.SENSOR_STATUS_*}
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };







    /**
     * This is a cleanup method that is called when the Activity is being
     * destroyed and ensures that any registered SensorEventListeners are
     * properly unregistered to avoid issues with battery life and memory
     * management
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }

    }

}