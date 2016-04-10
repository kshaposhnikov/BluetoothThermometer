package com.shaposhnikov.bluetooththermometer.model;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kirill on 11.04.2016.
 */
public class PairedDevices implements Serializable {

    private List<BTDevice> pairedDevices = new ArrayList<>();

    public PairedDevices(BTDevice... btDevices) {
        Collections.addAll(this.pairedDevices, btDevices);
    }

    public List<BTDevice> getPairedDevices() {
        return pairedDevices;
    }
}
