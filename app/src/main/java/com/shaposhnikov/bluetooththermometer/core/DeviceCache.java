package com.shaposhnikov.bluetooththermometer.core;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kirill on 29.03.2016.
 */
public class DeviceCache {

    private final static Set<BluetoothDevice> devices = new HashSet<>();

    public static synchronized BluetoothDevice getDevice(@NonNull String name) throws ThermometerException {
        for (BluetoothDevice device : devices) {
            if (name.equals(device.getName())) {
                return device;
            }
        }

        throw new ThermometerException(String.format("Could not found device by name: %s ", name));
    }

    public static synchronized void addDevice(BluetoothDevice... bluetoothDevices) {
        for (BluetoothDevice device : bluetoothDevices) {
            if (!devices.contains(device)) {
                devices.add(device);
            }
        }
    }

    public static synchronized void deleteDevice(String name) throws ThermometerException {
        devices.remove(getDevice(name));
    }
}
