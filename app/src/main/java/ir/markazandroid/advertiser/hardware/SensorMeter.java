package ir.markazandroid.advertiser.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coded by Ali on 9/15/2018.
 */
public class SensorMeter implements Closeable, SensorEventListener {

    private SensorManager sensorManager;

    private Map<String, String> registeredSensors;


    public SensorMeter(Context context) {
        Context context1 = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        registeredSensors = new HashMap<>();
    }

    public void init() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void close() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("t=")
                .append(event.timestamp)
                .append("a=")
                .append(event.accuracy);
        for (int i = 0; i < event.values.length; i++) {
            builder.append("v")
                    .append(i)
                    .append("=")
                    .append(event.values[i]);
        }
        registeredSensors.put(event.sensor.getName(), builder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getResults() {
        if (!registeredSensors.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : registeredSensors.entrySet()) {
                builder.append(entry.getKey())
                        .append(":")
                        .append(entry.getValue())
                        .append(";");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
        return "NA";
    }


}
