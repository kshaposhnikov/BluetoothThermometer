package com.shaposhnikov.bluetooththermometer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shaposhnikov.bluetooththermometer.bluetooth.BluetoothWrapper;
import com.shaposhnikov.bluetooththermometer.bluetooth.DeviceCache;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.handler.HandlerConst;
import com.shaposhnikov.bluetooththermometer.handler.MessageHandler;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.view.observable.UIObservable;
import com.shaposhnikov.bluetooththermometer.view.observer.DiscoveredDevicesObserver;
import com.shaposhnikov.bluetooththermometer.view.observer.ProgressDiscoveredDevices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kirill on 08.05.2016.
 */
public class DiscoveredDevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovered_devices);

        final ArrayAdapter<String> devices = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.list_discovered_devices);
        listView.setAdapter(devices);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.device_discovering_indicator);

        Map<Integer, UIObservable> observableMap = new HashMap<>();
        UIObservable deviceObservable = new UIObservable().addObservers(new DiscoveredDevicesObserver(devices));
        UIObservable progressBarObservable = new UIObservable().addObservers(new ProgressDiscoveredDevices(progressBar));
        observableMap.put(HandlerConst.What.DEVICES_FOUND, deviceObservable);
        observableMap.put(HandlerConst.What.DEVICES_NOT_FOUND, deviceObservable);
        observableMap.put(HandlerConst.What.DISCOVERY_FINISHED, progressBarObservable);

        final MessageHandler messageHandler = new MessageHandler(this.getApplicationContext(), observableMap);

        final BluetoothWrapper wrapper = new BluetoothWrapper(this, messageHandler);
        wrapper.discoveredDevices();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    BTDevice device = DeviceCache.getDevice((String) ((TextView) view).getText());
                    wrapper.connect(device);
                    finish();
                } catch (ThermometerException e) {
                    Log.e(this.getClass().getName(), "Device not found in cache", e);
                } catch (IOException e) {
                    Log.e(this.getClass().getName(), "Couldn't get streams from socket", e);
                }
            }
        });
    }
}
