package com.shaposhnikov.bluetooththermometer;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.shaposhnikov.bluetooththermometer.core.DeviceCache;
import com.shaposhnikov.bluetooththermometer.core.bluetooth.BluetoothWrapper;
import com.shaposhnikov.bluetooththermometer.core.bluetooth.Commands;
import com.shaposhnikov.bluetooththermometer.core.handler.MessageHandler;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.view.observable.NavigationViewObservable;
import com.shaposhnikov.bluetooththermometer.view.observable.ResponseViewObservable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

public class ThermometerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final Logger LOGGER = Logger.getLogger(ThermometerActivity.class.getName());

    private static final String CELSIUS_DEGREE = "°C";
    private String nameOfConnectedDevice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final BluetoothWrapper bluetoothWrapper = new BluetoothWrapper(this.getApplicationContext(), this);
        bluetoothWrapper.turnOn();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavigationViewObservable navigationViewObservable = new NavigationViewObservable(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (data instanceof Collection) {
                    generateMenuForPairedDevices(navigationView, (Collection) data);
                }
            }
        });

        navigationViewObservable.setPairedDevices(bluetoothWrapper);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setHorizontalScrollBarEnabled(true);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, -2),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6),
                new DataPoint(5, -2),
                new DataPoint(6, 5),
                new DataPoint(7, 3),
                new DataPoint(8, 2),
                new DataPoint(9, 6)
        });
        graph.addSeries(series);
    }

    public void execSingleMeasurement(View view) {
        try {
            BluetoothWrapper bluetoothWrapper = new BluetoothWrapper(this.getApplicationContext(), this);
            bluetoothWrapper.sendCommand(Commands.EXEC_SINGLE_MEASUREMENT, DeviceCache.getDevice(nameOfConnectedDevice));
        } catch (ThermometerException e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }

    public void execContinuousMeasurement(View view) {
        try {
            BluetoothWrapper bluetoothWrapper = new BluetoothWrapper(this.getApplicationContext(), this);
            bluetoothWrapper.sendCommand(Commands.EXEC_CONTINUOUS_MEASUREMENT, DeviceCache.getDevice(nameOfConnectedDevice));
        } catch (ThermometerException e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void generateMenuForPairedDevices(NavigationView navigationView, Collection<BluetoothDevice> devices) {
        Menu navigationMenu = navigationView.getMenu();
        MenuItem communicationMenuItem = navigationMenu.getItem(0);
        SubMenu communicationsSubMenu = communicationMenuItem.getSubMenu();
        for (BluetoothDevice device : devices) {
            communicationsSubMenu.add(
                    R.id.communication_menu,
                    View.generateViewId(),
                    0,
                    device.getName()
            ).setIcon(R.drawable.ic_menu_paired_device);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.thermometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        try {
            if (R.id.discover_devices != item.getItemId()) {
                BluetoothWrapper wrapper = new BluetoothWrapper(this.getApplicationContext(), this);
                nameOfConnectedDevice = (String) item.getTitle();

                ResponseViewObservable observable = new ResponseViewObservable(
                    new Observer() {
                        @Override
                        public void update(Observable observable, Object data) {
                            if (data instanceof String) {
                                String response = (String) data;
                                if (Double.parseDouble(response) > 0) {
                                    response = "+" + response + CELSIUS_DEGREE;
                                } else if (Double.parseDouble(response) < 0){
                                    response = "-" + response + CELSIUS_DEGREE;
                                }
                                ((TextView) findViewById(R.id.responseView)).setText(response);
                            }
                        }
                    }
                );

                MessageHandler handler = new MessageHandler(this.getApplicationContext(), observable);
                wrapper.connect(DeviceCache.getDevice(nameOfConnectedDevice), handler);
            }
        } catch (ThermometerException e) {
            Log.e(this.getClass().getName(), "Device not found in cache", e);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Couldn't get streams from socket", e);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
