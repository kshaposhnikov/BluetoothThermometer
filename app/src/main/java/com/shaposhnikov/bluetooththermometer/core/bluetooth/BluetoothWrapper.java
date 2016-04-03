package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.core.DeviceCache;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Kirill on 27.03.2016.
 */
public class BluetoothWrapper {

    private final Context context;
    private final Activity activity;
    private final BluetoothAdapter adapter;

    public BluetoothWrapper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void turnOn() {
        if (!adapter.isEnabled()) {
            activity.startActivityForResult(new Intent(adapter.ACTION_REQUEST_ENABLE), 0);
        } else {
            Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void turnOff() {
        if (adapter.isEnabled()) {
            adapter.disable();
        } else {
            Toast.makeText(context, "Bluetooth already disabled", Toast.LENGTH_SHORT).show();
        }
    }

    public Collection<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        DeviceCache.addDevice(bondedDevices.toArray(new BluetoothDevice[bondedDevices.size()]));
        return bondedDevices;
    }

    public void connect(BluetoothDevice device, Handler handler) throws ThermometerException, IOException {
        adapter.cancelDiscovery();
        DeviceConnector deviceConnector = new DeviceConnector(device, handler);
        deviceConnector.start();

        assert deviceConnector.getSocket().isConnected();

        BluetoothConnection connection = new BluetoothConnection(deviceConnector.getSocket(), handler);
        connection.start();

        ConnectionPool.addConnection(connection);
    }

    public void sendCommand(byte command, BluetoothDevice connectedDevice) {
        BluetoothConnection bluetoothConnection = ConnectionPool.getConnectionByDevice(connectedDevice);
        bluetoothConnection.write(command);
    }

    public void getResponse() {

    }
}
