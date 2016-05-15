package com.shaposhnikov.bluetooththermometer.view.observer;

import android.view.View;
import android.widget.ProgressBar;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 15.05.2016.
 */
public class ProgressDiscoveredDevices implements Observer {

    private final ProgressBar progressBar;

    public ProgressDiscoveredDevices(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void update(Observable observable, Object data) {
        progressBar.setVisibility(View.INVISIBLE);
    }
}
