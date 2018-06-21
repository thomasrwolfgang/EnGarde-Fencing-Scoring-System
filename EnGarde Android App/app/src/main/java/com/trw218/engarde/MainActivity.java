package com.trw218.engarde;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt1;
    private BluetoothGattService bluetoothGattService1;
    private BluetoothGattCharacteristic bl1HitTypeChar;
    private BluetoothGattDescriptor bl1HitTypeDescriptor;
    private BluetoothGattCharacteristic bl1HitTimeChar;
    private BluetoothGattDescriptor bl1HitTimeDescriptor;
    private BluetoothGatt bluetoothGatt2;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    int REQUEST_ENABLE_BT = 1234;

    ArrayList<String> devicesDiscovered = new ArrayList<String>();
    ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    ArrayAdapter<String> adapter;

    private static final long SCAN_PERIOD = 10000;

    //Views
    ListView deviceList1; // = (ListView) findViewById(R.id.DeviceList1);
    ListView deviceList2; // = (ListView) findViewById(R.id.DeviceList2);
    TextView leftFencerText;
    Button scanButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        leftFencerText = (TextView) findViewById(R.id.LeftFencerInfo);
        leftFencerText.setVisibility(View.INVISIBLE);

        deviceList1 = (ListView) findViewById(R.id.DeviceList1);
        deviceList1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Attempting to connect to device", Toast.LENGTH_SHORT).show();
                bluetoothGatt1 = devices.get(position).connectGatt(getApplicationContext(), true, btleGattCallback);

            }
        });
        deviceList2 = (ListView) findViewById(R.id.DeviceList2);
        deviceList2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Attempting to connect to device", Toast.LENGTH_SHORT).show();
                bluetoothGatt2 = devices.get(position).connectGatt(getApplicationContext(), true, btleGattCallback);
            }
        });
        scanButton = (Button) findViewById(R.id.StartScanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScan();
            }
        });

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                devicesDiscovered);

        deviceList1.setAdapter(adapter);
        deviceList2.setAdapter(adapter);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    private void startScan() {
        leftFencerText.setVisibility(View.INVISIBLE);
        leftFencerText.setText("");
        deviceList1.setVisibility(View.VISIBLE);
        devicesDiscovered.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.startScan(mLeScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
                Toast.makeText(getApplicationContext(), "Stopped Scanning", Toast.LENGTH_SHORT).show();
            }
        }, SCAN_PERIOD);
    }

    private void stopScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        });
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice() != null && result.getDevice().getName() != null) {
                boolean inList = false;
                for (String string : devicesDiscovered) {
                    if (string.equals(result.getDevice().getName())) {
                        inList = true;
                    }
                }
                if (!inList) {
                    devices.add(result.getDevice());
                    devicesDiscovered.add(result.getDevice().getName());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };


    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leftFencerText.append("Hit Detected\n");
                        }
                    });
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            final byte[] data = characteristic.getValue();
            if(data != null && characteristic.getUuid().toString().equals("544d1401-53c0-4416-a1f8-ed418de6a65a")) {
                final char hitType = (char)data[0];
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(hitType == 'y')
                            leftFencerText.append("On Target Hit Detected\n");
                        else if(hitType == 'n')
                            leftFencerText.append("Off Target Hit Detected\n");
                        else
                            leftFencerText.append("Unknown Hit Detected\n");
                        }
                });

            }
            else if(data != null && characteristic.getUuid().toString().equals("544d1402-53c0-4416-a1f8-ed418de6a65a")) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leftFencerText.append("Attempting to add time data\n");
                        leftFencerText.append("Hit Time" + data.toString() + "\n");
                    }
                });
            }
            else if(data == null){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leftFencerText.append("Read null Data from characteristic" + characteristic.getUuid().toString() + "\n");
                    }
                });
            }
            else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leftFencerText.append("Read data from unknown charactersitc\n");
                    }
                });
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            leftFencerText.setVisibility(View.INVISIBLE);
                            leftFencerText.setText("");
                            deviceList1.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "Device Disconnected", Toast.LENGTH_SHORT);
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            leftFencerText.setVisibility(View.VISIBLE);
                            deviceList1.setVisibility(View.INVISIBLE);
                            leftFencerText.append("device connected\n");
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt1.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Encountered an unknown state", Toast.LENGTH_SHORT);
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    leftFencerText.append("device services have been discovered\n");
                }
            });
            displayGattServices(bluetoothGatt1.getServices());
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            leftFencerText.append("No gatt service found\n");
            return;
        }

        for(BluetoothGattService gattService : gattServices) {
            final String uuid = gattService.getUuid().toString();
            if(uuid.equals("544d1400-53c0-4416-a1f8-ed418de6a65a")) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leftFencerText.append("EnGarde Service found\n");
                    }
                });

                bluetoothGattService1 = gattService;

                new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    final String charUuid = gattCharacteristic.getUuid().toString();
                    if(charUuid.equals("544d1401-53c0-4416-a1f8-ed418de6a65a")) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                leftFencerText.append("Hit Type Characteristic found\n");
                            }
                        });
                        bl1HitTypeChar = gattCharacteristic;
                        bluetoothGatt1.readCharacteristic(bl1HitTypeChar);
                        /*
                        bluetoothGatt1.setCharacteristicNotification(bl1HitTypeChar, true);

                        bl1HitTypeDescriptor = gattCharacteristic.getDescriptor(gattCharacteristic.getUuid());
                        bl1HitTypeDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt1.writeDescriptor(bl1HitTimeDescriptor);
                        */

                    }
                    /*
                    else if(charUuid.equals("544d1402-53c0-4416-a1f8-ed418de6a65a")) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                leftFencerText.append("Hit Time Characteristic found\n");
                            }
                        });
                        bl1HitTimeChar = gattCharacteristic;
                        bluetoothGatt1.readCharacteristic(bl1HitTimeChar);


                        bluetoothGatt1.setCharacteristicNotification(bl1HitTimeChar, true);

                        bl1HitTimeDescriptor = gattCharacteristic.getDescriptor(gattCharacteristic.getUuid());
                        bl1HitTimeDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt1.writeDescriptor(bl1HitTimeDescriptor);

6
                    }
                    */
               }

            }



        }
    }


}
