package com.shaposhnikov.bluetooththermometer.view.observable;

import com.shaposhnikov.bluetooththermometer.core.bluetooth.BluetoothWrapper;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 30.03.2016.
 */
public class NavigationViewObservable extends Observable {

    public NavigationViewObservable(Observer observer) {
        addObserver(observer);
    }

    public void setPairedDevices(BluetoothWrapper wrapper) {
        setChanged();
        notifyObservers(wrapper.getPairedDevices());
    }
}
