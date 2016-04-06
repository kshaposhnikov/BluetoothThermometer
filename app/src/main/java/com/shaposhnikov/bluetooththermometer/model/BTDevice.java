package com.shaposhnikov.bluetooththermometer.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Kirill on 29.03.2016.
 */
public class BTDevice {

    private DeviceStatus status;
    private final BluetoothDevice device;
    private final String deviceName;

    public BTDevice(BluetoothDevice device, DeviceStatus status) {
        this.device = device;
        this.status = status;
        this.deviceName = device.getName();
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
}
