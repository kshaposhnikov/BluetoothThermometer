package com.shaposhnikov.bluetooththermometer;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shaposhnikov.bluetooththermometer.bluetooth.BluetoothWrapper;
import com.shaposhnikov.bluetooththermometer.bluetooth.Commands;
import com.shaposhnikov.bluetooththermometer.bluetooth.DeviceCache;
import com.shaposhnikov.bluetooththermometer.handler.MessageHandler;
import com.shaposhnikov.bluetooththermometer.exception.ThermometerException;
import com.shaposhnikov.bluetooththermometer.view.observable.UIObservable;
import com.shaposhnikov.bluetooththermometer.view.observer.GraphObserver;
import com.shaposhnikov.bluetooththermometer.view.observer.NavigationViewObserver;
import com.shaposhnikov.bluetooththermometer.view.observer.ThermometerTextViewObserver;

import java.io.IOException;
import java.util.Collection;
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

        NavigationViewObserver navigationViewObserver = new NavigationViewObserver(navigationView);
        UIObservable<Collection<BluetoothDevice>> navigationViewObservable = new UIObservable<>(navigationViewObserver);

        bluetoothWrapper.getPairedDevices(new MessageHandler(this.getApplicationContext(), navigationViewObservable));
    }

    public void execSingleMeasurement(View view) {
        try {
            FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
            menu.collapse();
            BluetoothWrapper bluetoothWrapper = new BluetoothWrapper(this.getApplicationContext(), this);
            bluetoothWrapper.sendCommand(Commands.EXEC_SINGLE_MEASUREMENT, DeviceCache.getDevice(nameOfConnectedDevice));
        } catch (ThermometerException e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
        }
    }

    public void execContinuousMeasurement(View view) {
        try {
            FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
            menu.collapse();
            BluetoothWrapper bluetoothWrapper = new BluetoothWrapper(this.getApplicationContext(), this);
            bluetoothWrapper.sendCommand(Commands.EXEC_CONTINUOUS_MEASUREMENT, DeviceCache.getDevice(nameOfConnectedDevice));
        } catch (ThermometerException e) {
            Log.e(this.getClass().getName(), e.getMessage(), e);
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

                if (nameOfConnectedDevice != null) {
                    TextView textView = (TextView) findViewById(R.id.responseView);
                    ThermometerTextViewObserver thermometerTextViewObserver = new ThermometerTextViewObserver(textView);

                    GraphView graph = (GraphView) findViewById(R.id.graph);
                    setupGraph(graph);
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
                    graph.addSeries(series);
                    GraphObserver graphObserver = new GraphObserver(series);

                    UIObservable<String> uiObservable = new UIObservable<>(thermometerTextViewObserver, graphObserver);

                    MessageHandler handler = new MessageHandler(this.getApplicationContext(), uiObservable);
                    wrapper.connect(DeviceCache.getDevice(nameOfConnectedDevice), handler);
                } else {
                    Toast.makeText(this.getApplicationContext(), "Not connected to the selected device", Toast.LENGTH_SHORT);
                }
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

    private void setupGraph(GraphView graph) {
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(50.0);
        graph.getViewport().setYAxisBoundsManual(true);
    }
}
