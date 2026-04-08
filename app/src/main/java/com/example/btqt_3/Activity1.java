package com.example.btqt_3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity1 extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private ImageView imgLight;
    private TextView txtStatus, txtSensorValue;
    private boolean isLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        imgLight = findViewById(R.id.imgLight);
        txtStatus = findViewById(R.id.txtStatus);
        txtSensorValue = findViewById(R.id.txtSensorValue);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        txtSensorValue.setText("Khoảng cách: " + distance + " cm");

        // Khi tay ở gần (thường < 5cm tùy thiết bị)
        if (distance < proximitySensor.getMaximumRange()) {
            toggleLight();
        }
    }

    private void toggleLight() {
        isLightOn = !isLightOn;
        if (isLightOn) {
            imgLight.setImageResource(android.R.drawable.btn_star_big_on); // Giả lập đèn sáng
            txtStatus.setText("TRẠNG THÁI: ĐANG BẬT");
            txtStatus.setTextColor(androidx.pdf.ink.view.colorpalette.model.Color.YELLOW);
        } else {
            imgLight.setImageResource(android.R.drawable.btn_star_big_off);
            txtStatus.setText("TRẠNG THÁI: ĐANG TẮT");
            txtStatus.setTextColor(androidx.pdf.ink.view.colorpalette.model.Color.GRAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}