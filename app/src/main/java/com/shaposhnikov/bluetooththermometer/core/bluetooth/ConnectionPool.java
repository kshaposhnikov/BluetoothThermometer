package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Kirill on 03.04.2016.
 */
public class ConnectionPool {

    private static final ArrayList<BluetoothConnection> connectionPool = new ArrayList<>();

    public static synchronized void addConnection(@NonNull BluetoothConnection connection) {
        connectionPool.add(connection);
    }

    public static synchronized BluetoothConnection getConnectionByDevice(@NonNull BluetoothDevice device) {
        return getConnectionByDeviceName(device.getName());
    }

    public static synchronized BluetoothConnection getConnectionByDeviceName(@NonNull String deviceName) {
        for (BluetoothConnection connection : connectionPool) {
            if (deviceName.equals(connection.getConnectedDevice().getName())) {
                return connection;
            }
        }

        return null;
    }
}
