package com.shaposhnikov.bluetooththermometer.view.observer;

import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 10.04.2016.
 */
public class ThermometerTextViewObserver implements Observer {

    private static final String CELSIUS_DEGREE = "°C";

    private final TextView thermometerTextView;

    public ThermometerTextViewObserver(TextView thermometerTextView) {
        this.thermometerTextView = thermometerTextView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof String) {
            String response = ((String) data).trim();
            if (Double.parseDouble(response) > 0) {
                response = "+" + response + CELSIUS_DEGREE;
            } else if (Double.parseDouble(response) < 0) {
                response = "-" + response + CELSIUS_DEGREE;
            }
            thermometerTextView.setText(response);
        }
    }
}
