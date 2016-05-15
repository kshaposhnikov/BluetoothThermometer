package com.shaposhnikov.bluetooththermometer.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.R;
import com.shaposhnikov.bluetooththermometer.RequestConstants;
import com.shaposhnikov.bluetooththermometer.handler.HandlerConst;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.handler.MessageHandler;
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
    private final MessageHandler handler;
    private final CustomBroadcastReceiver broadcastReceiver;

    public BluetoothWrapper(final Activity activity, final MessageHandler handler) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        this.broadcastReceiver = new CustomBroadcastReceiver(handler);
    }

    public void turnOn() {
        if (!adapter.isEnabled()) {
            activity.startActivityForResult(new Intent(adapter.ACTION_REQUEST_ENABLE), RequestConstants.REQUEST_ENABLE_BLUETOOTH);
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

    public void discoveredDevices() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
            context.unregisterReceiver(broadcastReceiver);
        }

        context.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        adapter.startDiscovery();
    }

    public void connect(final BTDevice device, final MessageHandler handler) throws ThermometerException, IOException {
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
                            handler.sendTextMessage("Connected to " + device.getDeviceName(), HandlerConst.What.MESSAGE_TOAST);
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

    private static class CustomBroadcastReceiver extends BroadcastReceiver {

        private final static String STATUS = "DISCOVERY_FINISHED";

        private final MessageHandler handler;
        private boolean isDeviceFound = false;

        public CustomBroadcastReceiver(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BTDevice device = new BTDevice(intent.<BluetoothDevice>getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), DeviceStatus.FREE);
                DeviceCache.addDevice(device);
                isDeviceFound = true;
                handler.sendTextMessage(device.getDeviceName(), HandlerConst.What.DEVICES_FOUND);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                if (!isDeviceFound) {
                    handler.sendTextMessage(context.getString(R.string.devices_not_found), HandlerConst.What.DEVICES_NOT_FOUND);
                }

                handler.sendTextMessage(STATUS, HandlerConst.What.DISCOVERY_FINISHED);
            }
        }
    }
}
