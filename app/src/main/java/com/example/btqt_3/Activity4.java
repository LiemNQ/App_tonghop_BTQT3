package com.example.btqt_3;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity4 extends AppCompatActivity implements SensorEventListener {

    private final String SERVER_IP = "http://192.168.1.168:8080";

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private TextView txtStatus;
    private Button btnOpenAI;

    // Trạng thái thiết bị
    private boolean isFanOn = false;

    // Cấu hình cảm biến
    private static final float SHAKE_THRESHOLD = 25.0f; // Độ nhạy lắc
    private long lastTime = 0;
    private static final int COOLDOWN_MS = 1000; // Nghỉ 1.5s giữa các lần nhận lệnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);

        txtStatus = findViewById(R.id.txtStatus);
        btnOpenAI = findViewById(R.id.btnOpenAI);

        // Chuyển sang màn hình Camera AI
        btnOpenAI.setOnClickListener(v -> {
            Intent intent = new Intent(Activity4.this, GestureActivity.class);
            startActivity(intent);
        });

        // Khởi tạo Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();

        // Chỉ xử lý nếu đã qua thời gian chờ (Cooldown)
        if ((currentTime - lastTime) < COOLDOWN_MS) return;

        // 1. Logic Lắc máy (Accelerometer) -> Đảo trạng thái Quạt
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            if (acceleration > SHAKE_THRESHOLD) {
                lastTime = currentTime;
                if (!isFanOn) {
                    sendIoTCommand("/fan/on");
                    updateUI("QUẠT: ĐÃ BẬT 🌀");
                    isFanOn = true;
                } else {
                    sendIoTCommand("/fan/off");
                    updateUI("QUẠT: ĐÃ TẮT ⚪");
                    isFanOn = false;
                }
            }
        }

        // 2. Logic Nghiêng máy (Gyroscope) -> Bật/Tắt TV
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float axisY = event.values[1]; // Nghiêng trái/phải

            if (axisY > 4.0f) { // Nghiêng PHẢI để BẬT
                lastTime = currentTime;
                sendIoTCommand("/tv/on");
                updateUI("TV: ĐÃ MỞ 📺");
            } else if (axisY < -4.0f) { // Nghiêng TRÁI để TẮT
                lastTime = currentTime;
                sendIoTCommand("/tv/off");
                updateUI("TV: ĐÃ TẮT ⬛");
            }
        }
    }

    private void sendIoTCommand(String endpoint) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_IP + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    vibrateEffect();
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI(String message) {
        runOnUiThread(() -> {
            txtStatus.setText(message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void vibrateEffect() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(100); // Rung nhẹ 0.1 giây để báo hiệu thành công
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}