package com.example.myapplication;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

/**
 *  This is the BT(Bluetooth) Activity in charge of connecting to the already paired devices to send the user input to send commands
 *  to the robot.It is also in charge of uploading data to the server according to the commands of the user.
 */

public class BT extends AppCompatActivity {
    /**
     * Declaration of several UI components which are used to interact with the
     * Bluetooth-related functionality in the app
     */
    String id="MED";
    long time=System.currentTimeMillis()/1000;
    private SensorManager mSensorManager;
    float light;
    String action;
    private  Button listen,send, listDevices,up,down,left,right;
    private ListView listView;
    private TextView msg_box,status;
    private EditText writeMsg;
    /**
     * Declaration of several variables related to Bluetooth communication
     */
    private BluetoothAdapter bluetoothAdapter;
    private  BluetoothDevice[] btArray;



    SendReceive sendReceive;

    /**
     * The  several constant variables representing
     * different states of the Bluetooth connection and message communication process.
     */
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    /**
     * The REQUEST_ENABLE_BLUETOOTH variable is
     * used as a request code when enabling Bluetooth on the device.
     */
    int REQUEST_ENABLE_BLUETOOTH=1;

    /**
     *  a constant string variable APP_NAME to be used
     *  as the name of the app when creating a Bluetooth connection.
     */
    private static final String APP_NAME = "BTChat";


    //UUID(Universal Unique Identifier) of the device
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * This is the onCreate method
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // hide native UI bar
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }


        mSensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        /*
          This is used to request and check permissions for Bluetooth connectivity and scanning
         */
        try {
            if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BT.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }
            if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BT.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    return;
                }
            }
        } catch (NullPointerException e) {
            // Handle the null reference exception here.
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);

        findViewByIdes();
        //Get a handle to the default local Bluetooth adapter.
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
/*
  if the bluetoothAdapter is not enabled,this method will enable it so bluetooth is always on
 */
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BLUETOOTH);
        }

        implementListeners();
    }

    private SensorEventListener listener = new SensorEventListener() {
        /**
         *
         * @param event the {@link android.hardware.SensorEvent SensorEvent}.
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The value of the first subscript in the values array is the current light intensity
            float value = event.values[0];
            light=value;


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

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(listener);
        }

    }

    /**
     * Class which is in charge of uploading data to the server
     */
    private class UploadDataAsyncTask extends AsyncTask<String, Void, Void> {
        /**
         * sends data to a web server using an HTTP GET request in an asynchronous manner
         * @param params The parameters of the task.
         *
         * @return
         */

        @Override
        protected Void doInBackground(String... params) {
            try {
                // Construct the URL with the required parameters
                String urlParameters = "idproject=" + URLEncoder.encode(params[0], "UTF-8")
                        + "&timestamp=" + URLEncoder.encode(params[1], "UTF-8")
                        + "&lux=" + URLEncoder.encode(params[2], "UTF-8")
                        + "&action=" + URLEncoder.encode(params[3], "UTF-8");


                URL url = new URL("http://cabani.free.fr/ise/adddata.php?idproject=%s&timestamp=%s&lux=%s&action=%s" + urlParameters);

                // Open the connection and send the data
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("HTTP error code " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


            private void implementListeners() {




     listDevices.setOnClickListener(new View.OnClickListener() {


            /**
             * This method is used to populate the listDevices list view with the names
             * of the bonded bluetooth devices after checking and requesting permissions for
             * Bluetooth connectivity and scanning
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                /*
                  this  is used to check and request permissions for Bluetooth connectivity and scanning
                 */
                try {
                    if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(BT.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            return;
                        }
                    }
                    if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(BT.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                            return;
                        }
                    }
                } catch (NullPointerException e) {
                    // Handle the null reference exception here.
                }
                //object bt is used to store the bonded Bluetooth devices
                Set<BluetoothDevice> bt=bluetoothAdapter.getBondedDevices();
                //An array of String objects called strings is created with a size equal to the number of bonded Bluetooth devices
                String[] strings=new String[bt.size()];
                //An array of BluetoothDevice objects called btArray is created with a size equal to the number of bonded Bluetooth devices.
                btArray=new BluetoothDevice[bt.size()];
                int index=0;
                /*
                  If there are bonded Bluetooth devices, a for loop is used to iterate over
                  each device in the Set and add its name to the strings array, and
                  the device itself to the btArray array.
                 */
                if( bt.size()>0)
                {
                    for(BluetoothDevice device : bt)
                    {
                        btArray[index]= device;
                        strings[index]=device.getName();
                        index++;
                    }
                    /*
                      An ArrayAdapter object called arrayAdapter is created to bind
                      the strings array to the list view.
                     */
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    /*
                      The listView is populated with the names of the bonded Bluetooth
                      devices using the setAdapter method of the ListView class.
                     */
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            /**
             * This method is to start a Bluetooth server on a separate thread
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * This is used to initiate a Bluetooth client connection when an item in the
             * list is clicked
             * @param adapterView The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked (this
             *            will be a view provided by the adapter)
             * @param i The position of the view in the adapter.
             * @param l The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass=new ClientClass(btArray[i]);
                clientClass.start();

                status.setText("Connecting");
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            /**
             * this is an optional way used to send a typed message
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String string= String.valueOf(writeMsg.getText());



                sendReceive.write(string.getBytes());
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            /**
             * TThis sends the command "go" when the button is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String s1= "hgo";
                sendReceive.write(s1.getBytes());
                action="Forward";
                new UploadDataAsyncTask().execute(id, String.valueOf(time),String.valueOf(light), action);







            }

        });
        down.setOnClickListener(new View.OnClickListener() {
            /**
             * This sends the command "Backward" when the button is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String s2= "hback";
                action="Backward";
                sendReceive.write(s2.getBytes());
                new UploadDataAsyncTask().execute(id, String.valueOf(time),String.valueOf(light), action);





            }

        });
        left.setOnClickListener(new View.OnClickListener() {
            /**
             * This sends the command "left" when the button is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String s3= "hleft";
                action="Left";
                sendReceive.write(s3.getBytes());
                new UploadDataAsyncTask().execute(id, String.valueOf(time),String.valueOf(light), action);

            }

        });
        right.setOnClickListener(new View.OnClickListener() {
            /**
             * This sends the command "right" when the button is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String s4= "hright";
                action="Right";
                sendReceive.write(s4.getBytes());
                new UploadDataAsyncTask().execute(id, String.valueOf(time),String.valueOf(light), action);


            }

        });






    }

    Handler handler=new Handler(new Handler.Callback() {
        /**
         * The Handler object is used to update the UI with different
         * messages based on the state of a Bluetooth connection.
         * The object receives the messages from a thread, and depending on the
         * type of message,it updates the status
         * msg_box displays the command which is received by the connected device
         * @param msg A {@link android.os.Message Message} object
         * @return
         */
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    /**
     * findViewByIdes is the method that finds the View by the ID it is given
     */
    private void findViewByIdes() {
        listen=findViewById(R.id.listen);
        send=findViewById(R.id.send);
        listView=findViewById(R.id.listview);
        msg_box =findViewById(R.id.msg);
        status=findViewById(R.id.status);
        writeMsg=findViewById(R.id.writemsg);
        listDevices=findViewById(R.id.listDevices);
        up=findViewById(R.id.up);
        down=findViewById(R.id.down);
        left=findViewById(R.id.left);
        right=findViewById(R.id.right);
    }

    /**
     * This is a nested class called ServerClass that extends thread and is
     * used to create a Bluetooth server socket to listen for incoming connections
     */
    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            /*
              this  is used to check and request permissions for Bluetooth connectivity and scanning
             */
            try {
                if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BT.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BT.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                        return;
                    }
                }
            } catch (NullPointerException e) {
                // Handle the null reference exception here.
            }
            try {
                /*
                  The server socket is created in the constructor using
                   constructor using listenUsingRfcommWithServiceRecord() method
                   with the application name and a UUID as parameters.
                 */
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
              The run() method is used to wait for incoming connections by continuously calling
              the accept() method on the server socket until a connection is established.
              During this time, the handler is used to send messages to the UI thread to update the status of the connection.
              Once a connection is established, a new SendReceive thread is created to handle the communication over the Bluetooth socket.
             */
        public void run()
        {

            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }
    /**
     This is a nested class called the ClientClass that extends the Thread class.
     This class is in charge of establishing a Bluetooth Connection as a client.
     It contains two instance variables called device and socket which represent
     the Bluetooth device and socket respectively.
    */
    private class ClientClass extends Thread

    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        /**
         *This contains a constructor that takes a BluetoothDevice object as an argument
         * and initializes the device variable with it
         * @param device1 is a BluetoothDevice type object
         */
        public ClientClass (BluetoothDevice device1)
        {
            /*
              this  is used to check and request permissions for Bluetooth connectivity and scanning
             */
            try {
            if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BT.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }
            if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(BT.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    return;
                }
            }
        } catch (NullPointerException e) {
            // Handle the null reference exception here.
        }
            device=device1;

            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * The run method is responsible for connecting and creating the socket to the device.
         * When the connection is successful,it sends a message to the handler object indicating
         * that the connection has been established.It also creates a new SendReceive object with
         * the socket and starts it. If the connection fails,a message is sent to the handler object
         * indicating that the connection has failed.
         *
         */
        public void run()
        {
            /*
              this  is used to check and request permissions for Bluetooth connectivity and scanning
             */
            try {
                if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BT.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                if (ContextCompat.checkSelfPermission(BT.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(BT.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                        return;
                    }
                }
            } catch (NullPointerException e) {
                // Handle the null reference exception here.
            }
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * The SendReceive class which extends Thread is responsible for handling
     * the sending and receiving of data over the Bluetooth connection
     */
    class SendReceive extends Thread
    {
        /**
         * bluetoothSocket represents the Bluetooth socket used for communication
         * inputStream represents the input streams of the socket.
         * outputStream represents the output streams of the socket.
         */
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        /**
         *This takes in a BluetoothSocket object as a parameter and initializes the
         * member variables.It retrieves the input and output streams of the socket
         * and assigns them to the inputStream and outputStream variables.
         * @param socket A socket is an endpoint for communication between two machines.
         */
        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        /**
         * The run() method is responsible for reading incoming
         * data from the inputStream and sending it to the handler for processing.
         */
        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            /*
               The while loop is used to continuously read incoming data and sends the
               data to the handler using a Message object with the what parameter set
               to STATE_MESSAGE_RECEIVED.
             */
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * The write() method is used to send data over the outputStream.
         *
         * @param bytes is the taken parameter which is a byte array and is written to the outputStream.
         */
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
