package com.example.btqt_3;

import android.content.Context;
import android.graphics.Color;
import android.hardware.*;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Activity1 extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor, accelerometer;

    private ImageView imgLight;
    private TextView txtStatus, txtSensorValue;

    private boolean isLightOn = false;

    // FLASH
    private CameraManager cameraManager;
    private String cameraId;

    // RUNG
    private Vibrator vibrator;

    // ÂM THANH
    //private MediaPlayer soundOn, soundOff;

    // BLINK
    private boolean isBlinking = false;
    private Handler handler = new Handler();

    // PROXIMITY CONTROL
    private long lastToggleTime = 0;
    private static final int TOGGLE_DELAY = 1000;

    // SHAKE
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        imgLight = findViewById(R.id.imgLight);
        txtStatus = findViewById(R.id.txtStatus);
        txtSensorValue = findViewById(R.id.txtSensorValue);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // FLASH
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            Toast.makeText(this, "Không có flash", Toast.LENGTH_SHORT).show();
        }

        // RUNG
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // ÂM THANH (bạn cần thêm file vào res/raw)
        //soundOn = MediaPlayer.create(this, R.raw.on_sound);
        //soundOff = MediaPlayer.create(this, R.raw.off_sound);
    }

    // ===== SENSOR =====
    @Override
    public void onSensorChanged(SensorEvent event) {

        // ===== PROXIMITY =====
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            float distance = event.values[0];
            txtSensorValue.setText("Khoảng cách: " + distance);

            long currentTime = System.currentTimeMillis();

            if (distance < proximitySensor.getMaximumRange()
                    && currentTime - lastToggleTime > TOGGLE_DELAY) {

                toggleLight();
                lastToggleTime = currentTime;
            }
        }

        // ===== SHAKE =====
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            long currentTime = System.currentTimeMillis();

            if (acceleration > SHAKE_THRESHOLD
                    && currentTime - lastShakeTime > 1000) {

                toggleLight();
                lastShakeTime = currentTime;
            }
        }
    }

    // ===== TOGGLE =====
    private void toggleLight() {
        isLightOn = !isLightOn;

        if (isBlinking) stopBlinking();

        setFlash(isLightOn);

        if (isLightOn) {
            imgLight.setImageResource(android.R.drawable.btn_star_big_on);
            txtStatus.setText("FLASH: ON");
            txtStatus.setTextColor(Color.YELLOW);
            vibrate();
            //playSound(soundOn);
        } else {
            imgLight.setImageResource(android.R.drawable.btn_star_big_off);
            txtStatus.setText("FLASH: OFF");
            txtStatus.setTextColor(Color.GRAY);
            vibrate();
            //playSound(soundOff);
        }
    }

    // ===== FLASH =====
    private void setFlash(boolean state) {
        try {
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, state);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ===== BLINK MODE =====
    private void startBlinking() {
        isBlinking = true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isBlinking) return;

                isLightOn = !isLightOn;
                setFlash(isLightOn);

                handler.postDelayed(this, 300);
            }
        });
    }

    private void stopBlinking() {
        isBlinking = false;
    }

    // ===== RUNG =====
    private void vibrate() {
        if (vibrator != null) {
            vibrator.vibrate(100);
        }
    }

    // ===== ÂM THANH =====
//    private void playSound(MediaPlayer mp) {
//        if (mp != null) {
//            mp.start();
//        }
//    }

    // ===== LIFECYCLE =====
    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (proximitySensor != null)
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

            if (accelerometer != null)
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        setFlash(false);
        stopBlinking();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}