package com.shaposhnikov.bluetooththermometer.util;

import android.bluetooth.BluetoothDevice;

import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.model.DeviceStatus;

import java.util.Collection;

/**
 * Created by Kirill on 07.04.2016.
 */
public class DeviceConverter {

    public static BTDevice[] toBTDevices(BluetoothDevice... devices){
        BTDevice[] resultArray = new BTDevice[devices.length];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = toBTDevice(devices[i]);
        }
        return resultArray;
    }

    public static BTDevice toBTDevice(BluetoothDevice device) {
        return new BTDevice(device, DeviceStatus.FREE);
    }
}
