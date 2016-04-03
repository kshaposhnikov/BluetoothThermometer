package com.shaposhnikov.bluetooththermometer.core.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shaposhnikov.bluetooththermometer.core.handler.HandlerConst;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Kirill on 03.04.2016.
 */
public class BluetoothConnection extends Thread {

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Handler handler;

    public BluetoothConnection(BluetoothSocket socket, Handler handler) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.handler = handler;
    }

    public void run() {

        byte[] buffer = new byte[128];
        StringBuilder response = new StringBuilder();
        while (true) {
            try {
                int bufferLength = inputStream.read(buffer);
                String strBuffer = new String(buffer, 0, bufferLength);
                response.append(strBuffer);

                if (strBuffer.contains("\n")) {
                    Message message = handler.obtainMessage(HandlerConst.What.BLUETOOTH_RESPONSE);
                    Bundle bundle = new Bundle();
                    bundle.putString(HandlerConst.BundleKey.TEXT_MESSAGE, response.toString());
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                Log.e(this.getClass().getName(), "Couldn't read stream", e);
            }
        }
    }

    public void write(byte command) {
        try {
            outputStream.write(command);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Error when sending command", e);
        }
    }

    public BluetoothDevice getConnectedDevice() {
        return socket.getRemoteDevice();
    }
}
