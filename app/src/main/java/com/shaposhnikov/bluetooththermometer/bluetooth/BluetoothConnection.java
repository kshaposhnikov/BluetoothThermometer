package com.shaposhnikov.bluetooththermometer.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import com.shaposhnikov.bluetooththermometer.handler.HandlerConst;
import com.shaposhnikov.bluetooththermometer.handler.MessageHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Kirill on 03.04.2016.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class BluetoothConnection extends Thread implements AutoCloseable {

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private MessageHandler handler;

    public BluetoothConnection(BluetoothSocket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
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
                    handler.sendTextMessage(response.toString(), HandlerConst.What.BLUETOOTH_RESPONSE);
                    response.setLength(0);
                }
            } catch (IOException e) {
                Log.e(this.getClass().getName(), "Couldn't read stream", e);
                break;
            }
        }
    }

    public void write(byte command, MessageHandler handler) {
        try {
            this.handler = handler;
            outputStream.write(command);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Error when sending command", e);
        }
    }

    public BluetoothDevice getConnectedDevice() {
        return socket.getRemoteDevice();
    }

    @Override
    public void close() throws Exception {
        socket.close();
        inputStream.close();
        outputStream.close();
    }
}
