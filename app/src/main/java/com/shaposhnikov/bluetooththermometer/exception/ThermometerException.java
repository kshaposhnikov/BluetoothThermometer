package com.shaposhnikov.bluetooththermometer.exception;

/**
 * Created by Kirill on 30.03.2016.
 */
public class ThermometerException extends Exception {

    public ThermometerException(String message) {
        super(message);
    }

    public ThermometerException(String message, Throwable e) {
        super(message, e);
    }
}
