package com.shaposhnikov.bluetooththermometer.view.observable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 03.04.2016.
 */
public class ResponseViewObservable extends Observable {

    public ResponseViewObservable(Observer observer) {
        addObserver(observer);
    }

    public void addResponseToView(String response) {
        setChanged();
        notifyObservers(response);
    }
}
