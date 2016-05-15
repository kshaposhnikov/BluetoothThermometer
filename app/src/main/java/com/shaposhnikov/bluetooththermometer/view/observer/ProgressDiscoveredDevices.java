package com.shaposhnikov.bluetooththermometer.view.observer;

import android.view.View;
import android.widget.ProgressBar;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 15.05.2016.
 */
public class ProgressDiscoveredDevices implements Observer {

    private final static String STATUS = "DISCOVERY_FINISHED";

    private final ProgressBar progressBar;

    public ProgressDiscoveredDevices(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (STATUS.equals(data)) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
