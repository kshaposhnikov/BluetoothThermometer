package com.shaposhnikov.bluetooththermometer.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.handler.HandlerConst;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.model.DeviceStatus;
import com.shaposhnikov.bluetooththermometer.model.PairedDevices;
import com.shaposhnikov.bluetooththermometer.util.DeviceConverter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Created by Kirill on 27.03.2016.
 */
public class BluetoothWrapper {

    private final static int CONNECTION_TIMEOUT = 1000 * 10;
    private final static int WAITING_PAIRED_DEVICES_TIMEOUT = 1000 * 60;

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

    public Collection<BluetoothDevice> getPairedDevices(final Handler handler) {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            BTDevice[] pairedDevices = DeviceConverter.toBTDevices(bondedDevices.toArray(new BluetoothDevice[bondedDevices.size()]));
            DeviceCache.addDevice(pairedDevices);
            sendPairedDevices(handler, new PairedDevices(pairedDevices));
            return bondedDevices;
        } else {
            new Thread() {
                @Override
                public void run() {
                    long endTime = System.currentTimeMillis() + WAITING_PAIRED_DEVICES_TIMEOUT;
                    while (System.currentTimeMillis() < endTime) {
                        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
                        if (bondedDevices.size() > 0) {
                            BTDevice[] pairedDevices = DeviceConverter.toBTDevices(bondedDevices.toArray(new BluetoothDevice[bondedDevices.size()]));
                            DeviceCache.addDevice(pairedDevices);
                            sendPairedDevices(handler, new PairedDevices(pairedDevices));
                            break;
                        }
                    }
                }
            }.start();
        }
        return Collections.EMPTY_SET;
    }

    public void connect(final BTDevice device, final Handler handler) throws ThermometerException, IOException {
        adapter.cancelDiscovery();
        device.setStatus(DeviceStatus.CONNECTING);
        DeviceConnector deviceConnector = new DeviceConnector(device, handler);
        deviceConnector.start();

        final BluetoothSocket socket = deviceConnector.getSocket();

        new Thread() {
            @Override
            public void run() {
                long stopTime = System.currentTimeMillis() + CONNECTION_TIMEOUT;
                while (System.currentTimeMillis() < stopTime) {
                    if (socket != null && socket.isConnected()) {
                        BluetoothConnection connection = null;
                        try {
                            connection = new BluetoothConnection(socket, handler);
                            connection.start();
                            ConnectionPool.getInstance().addConnection(connection);
                            device.setStatus(DeviceStatus.CONNECTED);
                            sendTextMessage("Connected to " + device.getDeviceName(), handler);
                        } catch (IOException e) {
                            Log.e(this.getClass().getName(), "Couldn't instantiate streams", e);
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (Exception e1) {
                                    Log.e(this.getClass().getName(), "Couldn't close socket", e);
                                }
                            }
                        } finally {
                            break;
                        }
                    }
                }
            }
        }.start();
    }

    public void sendCommand(byte command, BTDevice connectedDevice) throws ThermometerException {
        if (DeviceStatus.CONNECTED.equals(connectedDevice.getStatus())) {
            BluetoothConnection bluetoothConnection = ConnectionPool.getInstance().getConnectionByDevice(connectedDevice);
            bluetoothConnection.write(command);
        }
    }

    private void sendPairedDevices(Handler handler, PairedDevices pairedDevices) {
        Message message = handler.obtainMessage(HandlerConst.What.PAIRED_DEVICES);
        Bundle bundle = new Bundle();
        bundle.putSerializable(HandlerConst.BundleKey.SERIALIZABLE, pairedDevices);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void sendTextMessage(String stringMessage, Handler handler) {
        Message message = handler.obtainMessage(HandlerConst.What.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(HandlerConst.BundleKey.TEXT_MESSAGE, stringMessage);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
