package com.shaposhnikov.bluetooththermometer.bluetooth;

import android.support.annotation.NonNull;

import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kirill on 29.03.2016.
 */
public class DeviceCache {

    private final static Set<BTDevice> devices = new HashSet<>();

    public static synchronized BTDevice getDevice(@NonNull String name) throws ThermometerException {
        for (BTDevice device : devices) {
            if (name.equals(device.getDeviceName())) {
                return device;
            }
        }

        throw new ThermometerException(String.format("Could not found device by name: %s ", name));
    }

    public static synchronized void addDevice(BTDevice... btDevices) {
        for (BTDevice device : btDevices) {
            if (!devices.contains(device)) {
                devices.add(device);
            }
        }
    }

    public static synchronized void deleteDevice(String name) throws ThermometerException {
        devices.remove(getDevice(name));
    }
}
