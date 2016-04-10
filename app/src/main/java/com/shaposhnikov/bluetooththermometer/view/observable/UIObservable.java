package com.shaposhnikov.bluetooththermometer.view.observable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 09.04.2016.
 */
public class UIObservable<T> extends Observable {

    public UIObservable(Observer... observers) {
        for (Observer observer : observers) {
            addObserver(observer);
        }
    }

    public void execute(T arg) {
        setChanged();
        notifyObservers(arg);
    }
}
