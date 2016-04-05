package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;

import java.util.ArrayList;

/**
 * Created by Kirill on 03.04.2016.
 */
public class ConnectionPool {

    private static final ArrayList<BluetoothConnection> connectionPool = new ArrayList<>();

    public static synchronized void addConnection(@NonNull BluetoothConnection connection) {
        connectionPool.add(connection);
    }

    public static synchronized BluetoothConnection getConnectionByDevice(@NonNull BluetoothDevice device) throws ThermometerException {
        return getConnectionByDeviceName(device.getName());
    }

    public static synchronized BluetoothConnection getConnectionByDeviceName(@NonNull String deviceName) throws ThermometerException {
        for (BluetoothConnection connection : connectionPool) {
            if (deviceName.equals(connection.getConnectedDevice().getName())) {
                return connection;
            }
        }

        throw new ThermometerException(String.format("Could not found connection for device with name: %s ", deviceName));
    }
}
