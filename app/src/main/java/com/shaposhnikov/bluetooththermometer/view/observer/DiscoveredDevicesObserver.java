package com.shaposhnikov.bluetooththermometer.view.observer;

import android.widget.ArrayAdapter;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 15.05.2016.
 */
public class DiscoveredDevicesObserver implements Observer {

    private final ArrayAdapter<String> devices;

    public DiscoveredDevicesObserver(ArrayAdapter<String> devices) {
        this.devices = devices;
    }

    @Override
    public void update(Observable observable, Object data) {
        devices.add((String) data);
        devices.notifyDataSetChanged();
    }
}
