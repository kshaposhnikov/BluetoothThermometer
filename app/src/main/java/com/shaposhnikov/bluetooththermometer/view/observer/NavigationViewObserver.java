package com.shaposhnikov.bluetooththermometer.view.observer;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.shaposhnikov.bluetooththermometer.R;
import com.shaposhnikov.bluetooththermometer.model.BTDevice;
import com.shaposhnikov.bluetooththermometer.model.PairedDevices;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kirill on 10.04.2016.
 */
public class NavigationViewObserver implements Observer {

    private final NavigationView navigationView;

    public NavigationViewObserver(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof PairedDevices) {
            Menu navigationMenu = navigationView.getMenu();
            MenuItem communicationMenuItem = navigationMenu.getItem(0);
            SubMenu communicationsSubMenu = communicationMenuItem.getSubMenu();
            for (BTDevice device : ((PairedDevices) data).getPairedDevices()) {
                communicationsSubMenu.add(
                        R.id.communication_menu,
                        View.generateViewId(),
                        0,
                        device.getDeviceName()
                ).setIcon(R.drawable.ic_menu_paired_device);
            }
        }
    }
}
