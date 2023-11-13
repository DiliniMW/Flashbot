package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is the AboutUsActivity responsible for the representation of the user
 */
public class AboutUsActivity extends AppCompatActivity {
    /**
     * Declaration of several UI components which are used to show
     * specific information to the user of the App
     */
    ImageView car1;
    //shows the representation of a creator
    ImageView car2;
    //shows the representation of a creator
    ImageView car3;
    //shows the representation of a creator

    TextView t1;
    //shows the name of a creator
    TextView t2;
    //shows the name of a creator
    TextView t3;
    //shows the name of a creator

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
        setContentView(R.layout.activity_aboutus);
        /**
         *findViewById is the method that finds the View by the ID it is given
         */
        car1 = findViewById(R.id.firstMemberImageView);
        car2 = findViewById(R.id.secondMemberImageVIew);
        car3 = findViewById(R.id.thirdMemberImageView);
        t3 = findViewById(R.id.secondMemberTextView);
        t1 = findViewById(R.id.firstMemberTextView);
        t2 = findViewById(R.id.thirdMemberTextView);
        /**
         * hides the Action bar
         */

        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}