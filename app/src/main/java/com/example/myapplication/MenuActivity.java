package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/**
 * The MenuActivity is the main screen after the splash screen to navigate through the app
 * according to the wants of the user
 */

public class MenuActivity extends AppCompatActivity {
    /**
     * Declaration of several UI components which are used to interact with the
     * Navigation-related functionality in the app
     */
    Button btButton;
    //Button to go to the BluetoothControl Screen
    Button BrightButton;
    //Button to go to the BrightnessControl Screen
    Button sensorsButton;
    //Button to go to the list of sensors
    Button aboutUsButton;
    //Button to go to the AboutUs Screen


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
        setContentView(R.layout.activity_menu);
        /**
         * hides the action bar
         */
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        /**
         *findViewById is the method that finds the View by the ID it is given
         */
        btButton = findViewById(R.id.btButton);
        BrightButton=findViewById(R.id.BrightButton);
        sensorsButton = findViewById(R.id.sensorsButton);
        aboutUsButton = findViewById(R.id.aboutUsButton);
        /**
         * switch to the BluetoothControl screen when the btButton is clicked
         */
        btButton.setOnClickListener(view -> {
            Intent intent1 = new Intent(MenuActivity.this, BT.class);
            startActivity(intent1);

        });
        /**
         * switch to the BrightnessControl screen when the BrightnessButton is clicked
         */
        BrightButton.setOnClickListener(view -> {
            Intent intent1 = new Intent(MenuActivity.this, ParametersActivity.class);
            startActivity(intent1);

        });
        /**
         * switch to the CheckSensorsActivity screen when the sensorsButton is clicked
         */

        sensorsButton.setOnClickListener(view -> {
            Intent intent2 = new Intent(MenuActivity.this, CheckSensorsActivity.class);
            startActivity(intent2);

        });
        /**
         * switch to the AboutUsActivity screen when the aboutUsButton is clicked
         */

        aboutUsButton.setOnClickListener(view -> {
            Intent intent3 = new Intent(MenuActivity.this, AboutUsActivity.class);
            startActivity(intent3);

        });
    }

}