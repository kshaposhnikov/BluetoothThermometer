package com.shaposhnikov.bluetooththermometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shaposhnikov.bluetooththermometer.bluetooth.BluetoothWrapper;
import com.shaposhnikov.bluetooththermometer.bluetooth.DeviceCache;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.model.DeviceStatus;

/**
 * Created by Kirill on 08.05.2016.
 */
public class DiscoveredDevicesActivity extends AppCompatActivity {

    private ArrayAdapter<String> devices;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String acton = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(acton)) {
                BTDevice device = new BTDevice(intent.<BluetoothDevice>getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), DeviceStatus.FREE);
                DeviceCache.addDevice(device);
                devices.add(device.getDeviceName());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(acton)) {
                if (devices.isEmpty()) {
                    devices.add(getString(R.string.devices_not_found));
                    devices.notifyDataSetChanged();
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovered_devices);

        devices = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.list_discovered_devices);
        listView.setAdapter(devices);


        final BluetoothWrapper wrapper = new BluetoothWrapper(this);
        wrapper.discoveredDevices(broadcastReceiver);
    }
}
