package com.shaposhnikov.bluetooththermometer.bluetooth;

import android.support.annotation.NonNull;

import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;

import java.util.ArrayList;

/**
 * Created by Kirill on 03.04.2016.
 */
public class ConnectionPool {

    private ArrayList<BluetoothConnection> connectionPool = new ArrayList<>();

    private static ConnectionPool instance;

    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    private ConnectionPool() {}

    public synchronized void addConnection(@NonNull BluetoothConnection connection) {
        connectionPool.add(connection);
    }

    public BluetoothConnection getConnectionByDevice(@NonNull BTDevice device) throws ThermometerException {
        return getConnectionByDeviceName(device.getDeviceName());
    }

    public BluetoothConnection getConnectionByDeviceName(@NonNull String deviceName) throws ThermometerException {
        for (BluetoothConnection connection : connectionPool) {
            if (deviceName.equals(connection.getConnectedDevice().getName())) {
                return connection;
            }
        }

        throw new ThermometerException(String.format("Could not found connection for device with name: %s [Pool size: %d]", deviceName, connectionPool.size()));
    }
}
