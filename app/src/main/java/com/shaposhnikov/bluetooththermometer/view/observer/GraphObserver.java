package com.shaposhnikov.bluetooththermometer.view.observer;

import android.util.Log;

import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 10.04.2016.
 */
public class GraphObserver implements Observer {

    private final static int MAX_COUNT_POINTS = 100;

    private final BaseSeries<DataPoint> series;

    public GraphObserver(BaseSeries<DataPoint> series) {
        this.series = series;
    }

    @Override
    public void update(Observable observable, Object data) {
        DataPoint dataPoint = new DataPoint(series.getHighestValueX() + 1.0, Double.parseDouble(((String) data).trim()));
        Log.i("DataPoint", dataPoint.toString());
        series.appendData(dataPoint, true, MAX_COUNT_POINTS);
    }
}
