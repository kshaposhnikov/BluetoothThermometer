package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.core.DeviceCache;
import com.shaposhnikov.bluetooththermometer.core.handler.HandlerConst;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.model.DeviceStatus;
import com.shaposhnikov.bluetooththermometer.util.DeviceConverter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Kirill on 27.03.2016.
 */
public class BluetoothWrapper {

    private final static int TIMEOUT = 1000 * 3;

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
        DeviceCache.addDevice(DeviceConverter.toBTDevices(bondedDevices.toArray(new BluetoothDevice[bondedDevices.size()])));
        return bondedDevices;
    }

    public void connect(BTDevice device, Handler handler) throws ThermometerException, IOException {
        adapter.cancelDiscovery();
        device.setStatus(DeviceStatus.CONNECTING);
        DeviceConnector deviceConnector = new DeviceConnector(device, handler);
        deviceConnector.start();

        final BluetoothSocket socket = deviceConnector.getSocket();
        long stopTime = System.currentTimeMillis() + TIMEOUT;
        while (System.currentTimeMillis() < stopTime) {
            if (socket != null && socket.isConnected()) {
                BluetoothConnection connection = new BluetoothConnection(socket, handler);
                connection.start();
                ConnectionPool.getInstance().addConnection(connection);
                device.setStatus(DeviceStatus.CONNECTED);
                sendTextMessage("Connected to " + device.getDeviceName(), handler);
                break;
            }
        }
    }

    public void sendCommand(byte command, BTDevice connectedDevice) throws ThermometerException {
        if (DeviceStatus.CONNECTED.equals(connectedDevice.getStatus())) {
            BluetoothConnection bluetoothConnection = ConnectionPool.getInstance().getConnectionByDevice(connectedDevice);
            bluetoothConnection.write(command);
        }
    }

    private void sendTextMessage(String stringMessage, Handler handler) {
        Message message = handler.obtainMessage(HandlerConst.What.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(HandlerConst.BundleKey.TEXT_MESSAGE, stringMessage);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
