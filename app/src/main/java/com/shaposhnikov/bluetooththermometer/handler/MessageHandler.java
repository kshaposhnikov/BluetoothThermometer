package com.shaposhnikov.bluetooththermometer.handler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.model.PairedDevices;
import com.shaposhnikov.bluetooththermometer.view.observable.UIObservable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Kirill on 02.04.2016.
 */
public class MessageHandler extends Handler {

    private final Context context;
    private final Map<Integer, UIObservable> observables;

    public MessageHandler(Context context) {
        this(context, Collections.EMPTY_MAP);
    }

    public MessageHandler(Context context, Map<Integer, UIObservable> observables) {
        this.context = context;
        this.observables = observables;
    }

    @Override
    public void handleMessage(Message msg) {
        if (HandlerConst.What.MESSAGE_TOAST == msg.what) {
            String message = (String) msg.getData().get(HandlerConst.BundleKey.TEXT_MESSAGE);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            if (msg.getData().keySet().size() == 1) {
                String bundleKey = msg.getData().keySet().iterator().next();
                observables.get(msg.what).execute(msg.getData().get(bundleKey));
            }
        }
    }

    public void sendTextMessage(String stringMessage, int what) {
        Message message = obtainMessage(what);
        Bundle bundle = new Bundle();
        bundle.putString(HandlerConst.BundleKey.TEXT_MESSAGE, stringMessage);
        message.setData(bundle);
        sendMessage(message);
    }
}
