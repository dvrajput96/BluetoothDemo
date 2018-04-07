package com.example.pc.bluetoothdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 12;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private TextView txtOnOff;
    private Switch onOff;
    private RecyclerView recyclerView;
    private RecyclerView rvNearByDevicesList;
    private TextView txtNoPair;
    private ImageView ivBackArrow;

    private boolean mScanning;
    private Handler mHandler;
    private RecyclerAdapter mLeDeviceListAdapter;
    private RecyclerAvailListAdapter recyclerAvailListAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private RelativeLayout avail;
    private RelativeLayout pair;
    private RelativeLayout paired;

    private ArrayList<DeviceData> pairedDevicesList;
    private ArrayList<String> availDevicesMacId;
    private ArrayList<String> availDevicesName;
    private ArrayList<DeviceData> deviceDatas;

    private BluetoothDevice mBluetoothDevice;

    private int pos = -1;


    private BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(context, "Paired", Toast.LENGTH_SHORT).show();

                    if (pos != -1) {
                        deviceDatas.get(pos).setIsConnect(2);
                        recyclerAvailListAdapter.addPairing(deviceDatas);
                    }

//                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macID);

                    intent = new Intent(MainActivity.this, ChatActivity.class);
                   /* intent.putExtra("ID", macID);
                    intent.putExtra("Name", name);*/
                    startActivity(intent);

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "Unpair", Toast.LENGTH_SHORT).show();
                }
                recyclerAvailListAdapter.notifyDataSetChanged();
            }


        }
    };


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String s = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // A Bluetooth device was found
                // Getting device information from the intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.i("TAG", "Device found: " + device.getName() + " - MAC : " + device.getAddress());

                /* also we can set constructor in model method and call below method to set data
                deviceDatas.add(new DeviceData(device.getName(), device.getAddress()));*/

                DeviceData deviceData = new DeviceData();
                deviceData.setMacID(device.getAddress());
                deviceData.setName(device.getName());

                if (isContainPairDevice(deviceData)) {
                    return;
                } else {
                    if (isContainNearbyDevice(deviceData)) {
                        return;
                    } else {
                        deviceDatas.add(deviceData);
                    }
                }

                recyclerAvailListAdapter.notifyDataSetChanged();
            }
        }
    };
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // BLE device was found, we can get its information now
                    Log.i("TAG", "BLE device found: "
                            + device.getName() + "; MAC " + device.getAddress());
                    Log.d("TAG", "==" + availDevicesName);
                    Log.d("TAG", "==" + availDevicesMacId);

                }
            });
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    // Bluetooth is disconnected, do handling here
                    hideList(true);
                    paired.setVisibility(View.GONE);
                    clearRecyclerView();
                } else {
                    hideList(false);
                    if (checkLocationPermission()) {
                        discovery();
                        //mBluetoothAdapter.startLeScan(mLeScanCallback);
                        scanLeDevice(true);
                        paired.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo!
                    Toast.makeText(MainActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    // finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu); // inflate your menu resource

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dis: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        onOff.setChecked(true);
                        txtOnOff.setText("On");
                        // txtScan.setVisibility(View.VISIBLE);
                        paired.setVisibility(View.VISIBLE);
                        listofDevices();
                        if (checkLocationPermission()) {
                            discovery();
                            //mBluetoothAdapter.startLeScan(mLeScanCallback);
                            scanLeDevice(true);
                            rvNearByDevicesList.setVisibility(View.VISIBLE);
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        onOff.setChecked(false);
                        txtOnOff.setText("Off");
                        clearRecyclerView();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void initView() {

        txtOnOff = (TextView) findViewById(R.id.txtonoff);
        onOff = (Switch) findViewById(R.id.onoffswitch);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        avail = (RelativeLayout) findViewById(R.id.available);
        pair = (RelativeLayout) findViewById(R.id.pair);
        paired = (RelativeLayout) findViewById(R.id.paired);
        txtNoPair = (TextView) findViewById(R.id.txtnopairdevice);
        rvNearByDevicesList = (RecyclerView) findViewById(R.id.recyclelist);
        avail.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        rvNearByDevicesList.setVisibility(View.GONE);
        paired.setVisibility(View.GONE);
        pair.setVisibility(View.GONE);
        mHandler = new Handler();

        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        recyclerView.setHasFixedSize(true);
        rvNearByDevicesList.setHasFixedSize(true);

        availDevicesMacId = new ArrayList<>();
        availDevicesName = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        rvNearByDevicesList.setLayoutManager(layoutManager1);

        deviceDatas = new ArrayList<>();
        recyclerAvailListAdapter = new RecyclerAvailListAdapter(MainActivity.this, deviceDatas);
        rvNearByDevicesList.setAdapter(recyclerAvailListAdapter);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (mBluetoothAdapter.isEnabled()) {
            onOff.setChecked(true);
            txtOnOff.setText("On");
            if (checkLocationPermission()) {
                discovery();
                // mBluetoothAdapter.startLeScan(mLeScanCallback);
                scanLeDevice(true);
                rvNearByDevicesList.setVisibility(View.VISIBLE);
                avail.setVisibility(View.VISIBLE);
                paired.setVisibility(View.VISIBLE);
            }
        }

        listofDevices();

        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensures Bluetooth is available on the device and it is enabled. If not,
                // displays a dialog requesting user permission to enable Bluetooth.
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    clearRecyclerView();
                    pair.setVisibility(View.GONE);
                    avail.setVisibility(View.GONE);
                    paired.setVisibility(View.GONE);
                    rvNearByDevicesList.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearRecyclerView();
        unregisterReceiver(mReceiver);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(mPairReceiver);
    }

    private void discovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            // Bluetooth is already in modo discovery mode, we cancel to restart it again
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    public boolean checkLocationPermission() {
        if ((ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        }
    }

    private void hideList(boolean isHidden) {
        if (isHidden) {
            onOff.setChecked(false);
            txtOnOff.setText("Off");
            pair.setVisibility(View.GONE);
            avail.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            rvNearByDevicesList.setVisibility(View.GONE);
        } else {
            onOff.setChecked(true);
            txtOnOff.setText("On");
            pair.setVisibility(View.VISIBLE);

            avail.setVisibility(View.VISIBLE);
            listofDevices();
            discovery();
            scanLeDevice(true);
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            recyclerView.setVisibility(View.VISIBLE);
            rvNearByDevicesList.setVisibility(View.VISIBLE);
        }
    }

    private boolean isContainPairDevice(DeviceData deviceData) {

        if (pairedDevicesList != null) {
            for (DeviceData data : pairedDevicesList) {
                if (data.getMacID().equals(deviceData.getMacID())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isContainNearbyDevice(DeviceData deviceData) {
        if (deviceDatas != null) {
            for (DeviceData data : deviceDatas) {
                if (data.getMacID().equals(deviceData.getMacID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearRecyclerView() {
        int size = this.deviceDatas.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                //this.deviceDatas.remove(0);
                this.deviceDatas.clear();
            }
            recyclerAvailListAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    private void scanLeDevice(boolean isScan) {
        if (isScan) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    private void listofDevices() {

        //get MAC Address of Paired devicesGONE
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {
            pair.setVisibility(View.VISIBLE);
            avail.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            //get Name of Paired devices
            pairedDevicesList = new ArrayList<>();

            for (BluetoothDevice b : pairedDevices) {
                pairedDevicesList.add(new DeviceData(b.getName(), b.getAddress()));
            }

            if (pairedDevicesList == null) {
                txtNoPair.setVisibility(View.VISIBLE);
            } else {
                txtNoPair.setVisibility(View.GONE);
            }

            Log.d("TAG", "===" + pairedDevices);

            mLeDeviceListAdapter = new RecyclerAdapter(this, pairedDevicesList);
            recyclerView.setAdapter(mLeDeviceListAdapter);
        }
    }

    private void pairDevice(BluetoothDevice device, int position) {

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void unPairDevice(BluetoothDevice device) {

        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void openData(int position, String macID) {

        pos = position;

        Log.d("TAG", "Coming incoming address : " + macID + "\nPosition : " + position);
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macID);
        if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            unPairDevice(mBluetoothDevice);
        } else {
            Toast.makeText(this, "Pairing...", Toast.LENGTH_SHORT).show();
            pairDevice(mBluetoothDevice, position);
        }

        deviceDatas.get(position).setIsConnect(1);
        recyclerAvailListAdapter.addPairing(deviceDatas);
    }

    public void openDetails(final int position, String macID, String name) {

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macID);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.drawable.ic_error_black_24dp);
        builder.setTitle("Paired Device");
        builder.setMessage("\t\t\t   Do you really want to unpair ?");
        if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            builder.setNegativeButton("FORGET",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            // DO TASK
                            unPairDevice(mBluetoothDevice);

                            pairedDevicesList.remove(position);
                            mLeDeviceListAdapter.notifyDataSetChanged();

                            if (pairedDevicesList.size() == 0) {
                                txtNoPair.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                txtNoPair.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    });

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            // DO TASK
                        }
                    });
        }

        builder.show();


       /* // Set `EditText` to `dialog`. You can add `EditText` from `xml` too.
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        final AlertDialog dialog = builder.create();
        dialog.show();
        // Initially disable the button
        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        // OR you can use here setOnShowListener to disable button at first
        // time.

        // Now set the textchange listener for edittext
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });*/


    }

}
