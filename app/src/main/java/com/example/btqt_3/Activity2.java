package com.example.btqt_3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Activity2 extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AudioManager audioManager;
    private long lastUpdate = 0;

    private SeekBar volumeSeekBar;
    private TextView tvVolumePercentage;

    private int maxVolume;
    private int currentStep = 0; // Biến lưu số nấc của thanh trượt (từ 0 đến 20)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        tvVolumePercentage = findViewById(R.id.tvVolumePercentage);

        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentActualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Đặt SeekBar có đúng 20 nấc (Mỗi nấc = 5%)
        volumeSeekBar.setMax(20);

        // Tính toán nấc hiện tại khi vừa mở app
        float initialPercent = ((float) currentActualVolume / maxVolume) * 100f;
        currentStep = Math.round(initialPercent / 5.0f);

        volumeSeekBar.setProgress(currentStep);
        updateVolumeAndUI(); // Cập nhật Text và Âm lượng thật

        // Xử lý kéo vuốt: SeekBar giờ mặc định chỉ nhảy từng nấc 1 (tương đương 5%)
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentStep = progress;
                    updateVolumeAndUI(); // Gọi hàm cập nhật
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Khởi tạo cảm biến
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > 300) {
                float x = event.values[0];
                boolean hasChanged = false;

                if (x < -3.0f) { // Nghiêng PHẢI -> Tăng 1 nấc (5%)
                    currentStep++;
                    hasChanged = true;
                } else if (x > 3.0f) { // Nghiêng TRÁI -> Giảm 1 nấc (5%)
                    currentStep--;
                    hasChanged = true;
                }

                if (hasChanged) {
                    // Chặn giới hạn số nấc từ 0 đến 20
                    if (currentStep > 20) currentStep = 20;
                    if (currentStep < 0) currentStep = 0;

                    // Cập nhật SeekBar (fromUser sẽ là false)
                    volumeSeekBar.setProgress(currentStep);
                    updateVolumeAndUI();

                    lastUpdate = currentTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Hàm chung để xử lý hiển thị % và đổi ra âm lượng thật
    private void updateVolumeAndUI() {
        // 1 nấc = 5%
        int percentage = currentStep * 5;
        tvVolumePercentage.setText(percentage + "%");

        // Quy đổi % sang bậc âm lượng thật của điện thoại
        int actualVolume = Math.round((percentage / 100.0f) * maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, actualVolume, 0);
    }
}