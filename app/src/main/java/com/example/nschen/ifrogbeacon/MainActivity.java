package com.example.nschen.ifrogbeacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.SensorEventListener;

import com.powenko.ifroglab_bt_lib.ifrog;

import java.util.ArrayList;

import static com.example.nschen.ifrogbeacon.R.id.image;

public class MainActivity extends AppCompatActivity implements ifrog.ifrogCallBack, SensorEventListener {
    private ListView listView1;
    Button statusText;
    private ImageView image;
    private TextView tvHeading;
    private Button btn;

    private boolean nextStatus = true;//first is true
    String[] testValues= new String[]{	"Apple","Banana","Orange","Tangerine"};
    String[] testValues2= new String[]{	"Red","Yello","Orange","Yello"};


    // record the compass picture angle turned
    private float currentDegree = 0f;
    private float beaconDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    private rowdata adapter;
    private ifrog mifrog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView1=(ListView) findViewById(R.id.listView1);   //取得listView1
        //ListAdapter adapter = createAdapter();
        statusText = (Button)findViewById(R.id.status);
        BTinit();

        image = (ImageView) findViewById(R.id.compass);
        btn =(Button)findViewById(R.id.resetBtn);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree-beaconDegree,
                -degree-beaconDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public void resetDirection(View v) {
        beaconDegree = currentDegree;
    }

    private void SetupList(){
        adapter=new rowdata(this,testValues,testValues2);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener(){      //選項按下反應
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String item = testValues[position];      //哪一個列表
                Toast.makeText(MainActivity.this, item + " selected", Toast.LENGTH_LONG).show();       //顯示訊號
            }
        } );

    }

    public void BTinit(){
        mifrog=new ifrog();
        mifrog.setTheListener(this);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"this Device doean't support Bluetooth BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mifrog.InitCheckBT(bluetoothManager) == null) {
            Toast.makeText(this,"this Device doean't support Bluetooth BLE", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mifrog.scanLeDevice(true,100000);
    }


    ArrayList<String> Names = new ArrayList<String>();
    ArrayList<String> Address = new ArrayList<String>();


    @Override
    public void BTSearchFindDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String t_address= device.getAddress();//有找到裝置的話先抓Address
        int index=0;
        int min = 10000;
        boolean t_NewDevice=true;
        for(int i=0;i<Address.size();i++){
            String t_Address2=Address.get(i);
            if(t_Address2.compareTo(t_address)==0){//如果address和列表中的address一模一樣
                t_NewDevice=false;//登記說他不是新的device
                index=i;//把index記起來
                break;
            }
        }
        if(device.getName() != null){
            if(t_NewDevice==true){//如果是新的advice
                Address.add(t_address);
                //null can appear
                Names.add(device.getName()+" RSSI="+Integer.toString(rssi)+" d="+Math.round(calculateDistance(rssi)*10)+"cm");//抓名字然後放進列表
                testValues = Names.toArray(new String[Names.size()]);
                testValues2 =Address.toArray(new String[Address.size()]);

            }else{//如果不是新的device
                Names.set(index,device.getName()+" RSSI="+Integer.toString(rssi)+" d="+Math.round(calculateDistance(rssi)*10)+"cm");//更改device名字，RSSI:藍芽4.0裡面可以知道訊號強度
                testValues = Names.toArray(new String[Names.size()]);//放進array
            }
            Log.d("myTag", "陣列"+Names);
            Log.d("myTag", "陣列"+testValues);
            Log.d("myTag", "陣列"+testValues2);

        }


        SetupList();//更新畫面
    }


    public double calculateDistance(int rssi){
        float txPower = -59;//hard coded power value. Usually ranges between -59 to -65
        if(rssi == 0){
            return -1.0;
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else{
            double distance = (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return distance;
        }
    }

    public void btnStartorStop(View v){
        //mifrog.scanLeDevice(true,10000);
        if(!nextStatus){//now -> start; nextStatus is true
            mifrog.scanLeDevice(nextStatus, 3600000);//按一次找1hr until you stop it
            nextStatus = true;
            statusText.setText("Start");
            //BTSearchFindDevicestatus(!nextStatus);
        }
        else{//now -> stop; nextStatus is false
            mifrog.scanLeDevice(nextStatus, 3600000);//stop it
            nextStatus = false;// = next time will be false
            statusText.setText("Stop");
            //BTSearchFindDevicestatus(!nextStatus);
        }

    }

    @Override
    public void BTSearchFindDevicestatus(boolean arg0) {
        if(arg0==false){
            Toast.makeText(getBaseContext(),"Stop Search", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(),"Start Search",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mifrog.BTSearchStop();
    }
}
