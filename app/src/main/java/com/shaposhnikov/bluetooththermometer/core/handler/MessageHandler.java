package com.shaposhnikov.bluetooththermometer.core.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.shaposhnikov.bluetooththermometer.view.observable.ResponseViewObservable;

import java.util.Observable;

/**
 * Created by Kirill on 02.04.2016.
 */
public class MessageHandler extends Handler {

    private final Context context;
    private final Observable observable;

    public MessageHandler(Context context, Observable observable) {
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
                if (observable instanceof ResponseViewObservable) {
                    String response = (String) msg.getData().get(HandlerConst.BundleKey.TEXT_MESSAGE);
                    ((ResponseViewObservable) observable).addResponseToView(response);
                }

        }
    }
}
