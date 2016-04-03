package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shaposhnikov.bluetooththermometer.core.handler.HandlerConst;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Kirill on 02.04.2016.
 */
public class DeviceConnector extends Thread {

    private final BluetoothDevice device;
    private final BluetoothSocket socket;

    private Handler handler;

    public DeviceConnector(BluetoothDevice device, Handler handler) {
        this.device = device;
        this.socket = createRfcommSocket(device);
        this.handler = handler;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void run() {
        try {
            assert socket != null;

            socket.connect();
            sendTextMessage("Connected to " + device.getName());
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                sendTextMessage("Failed to close connection");
                Log.e(this.getClass().getName(), "Failed to close connection", e);
            }
            sendTextMessage("Failed connection");
            Log.e(this.getClass().getName(), "Failed connection", e);
        }
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private BluetoothSocket createRfcommSocket(BluetoothDevice device) {
        try {
            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            aclass[0] = Integer.TYPE;
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            return (BluetoothSocket) method.invoke(device, aobj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(this.getClass().getName(), "Couldn't invoke method createRfcommSocket of device " +
                    "with class " + device.getClass().getName(), e);
        }

        return null;
    }

    private void sendTextMessage(String stringMessage) {
        Message message = handler.obtainMessage(HandlerConst.What.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(HandlerConst.BundleKey.TEXT_MESSAGE, stringMessage);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
