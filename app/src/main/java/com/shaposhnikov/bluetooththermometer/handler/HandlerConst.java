package com.shaposhnikov.bluetooththermometer.handler;

/**
 * Created by Kirill on 02.04.2016.
 */
public class HandlerConst {

    public static class What {
        public static final int MESSAGE_TOAST = 0;
        public static final int BLUETOOTH_RESPONSE = 1;
        public static final int PAIRED_DEVICES = 2;
        public static final int DEVICES_FOUND = 3;
        public static final int DEVICES_NOT_FOUND = 4;
        public static final int DISCOVERY_FINISHED = 5;
    }

    public static class BundleKey {
        public static final String TEXT_MESSAGE = "TEXT_MESSAGE";
        public static final String SERIALIZABLE = "SERIALIZABLE";
    }
}
