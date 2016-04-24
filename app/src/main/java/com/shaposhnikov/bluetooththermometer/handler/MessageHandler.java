package com.shaposhnikov.bluetooththermometer.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.model.PairedDevices;
import com.shaposhnikov.bluetooththermometer.view.observable.UIObservable;

/**
 * Created by Kirill on 02.04.2016.
 */
public class MessageHandler extends Handler {

    private final Context context;
    private final UIObservable observable;

    public MessageHandler(Context context, UIObservable observable) {
        this.context = context;
        this.observable = observable;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HandlerConst.What.MESSAGE_TOAST:
                String message = (String) msg.getData().get(HandlerConst.BundleKey.TEXT_MESSAGE);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                break;
            case HandlerConst.What.BLUETOOTH_RESPONSE:
                String response = (String) msg.getData().get(HandlerConst.BundleKey.TEXT_MESSAGE);
                observable.execute(response);
                break;
            case HandlerConst.What.PAIRED_DEVICES:
                PairedDevices pairedDevices = (PairedDevices) msg.getData().get(HandlerConst.BundleKey.SERIALIZABLE);
                observable.execute(pairedDevices);
                break;
        }
    }
}
