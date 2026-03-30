package com.example.btqt_3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Activity3 extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;

    private MediaPlayer mediaPlayer;
    private TextView tvSongName, tvCurrentTime, tvTotalTime;
    private SeekBar songProgressBar;
    private Button btnPrev, btnPlayPause, btnNext;

    private List<Integer> songList = new ArrayList<>();
    private List<String> songNamesList = new ArrayList<>();
    private int currentSongIndex = 0;

    // Dùng Handler để cập nhật thanh tiến trình mỗi giây
    private Handler handler = new Handler();
    private Runnable updateProgressAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        tvSongName = findViewById(R.id.tvSongName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        songProgressBar = findViewById(R.id.songProgressBar);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);

        loadSongsFromRaw();

        if (songList.isEmpty()) {
            tvSongName.setText("Không tìm thấy nhạc trong raw!");
            btnPlayPause.setEnabled(false); // Khóa nút nếu không có nhạc
        } else {
            playSong(currentSongIndex);
        }

        // --- XỬ LÝ CÁC NÚT BẤM ---
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayPause.setText("▶"); // Đổi thành nút Play
                } else {
                    mediaPlayer.start();
                    btnPlayPause.setText("⏸"); // Đổi thành nút Pause
                    updateProgressBar(); // Tiếp tục chạy thanh tiến trình
                }
            }
        });

        btnNext.setOnClickListener(v -> nextSong());
        btnPrev.setOnClickListener(v -> prevSong());

        // --- XỬ LÝ KÉO TUA NHẠC ---
        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress); // Tua nhạc tới vị trí kéo
                    tvCurrentTime.setText(formatTime(progress)); // Cập nhật text thời gian
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Thiết lập cảm biến lắc tay
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void loadSongsFromRaw() {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                songList.add(field.getInt(null));
                songNamesList.add(field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void playSong(int index) {
        if (songList.isEmpty()) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, songList.get(index));

        // Cài đặt tự động chuyển bài khi bài hát chạy hết
        mediaPlayer.setOnCompletionListener(mp -> nextSong());

        mediaPlayer.start();
        btnPlayPause.setText("⏸");
        tvSongName.setText("Đang phát:\n" + songNamesList.get(index));

        // Setup độ dài tối đa cho thanh trượt
        int duration = mediaPlayer.getDuration();
        songProgressBar.setMax(duration);
        tvTotalTime.setText(formatTime(duration));

        updateProgressBar(); // Bắt đầu chạy bộ đếm
    }

    private void nextSong() {
        currentSongIndex++;
        if (currentSongIndex >= songList.size()) currentSongIndex = 0;
        playSong(currentSongIndex);
    }

    private void prevSong() {
        currentSongIndex--;
        if (currentSongIndex < 0) currentSongIndex = songList.size() - 1;
        playSong(currentSongIndex);
    }

    // Hàm cập nhật thanh trượt liên tục
    private void updateProgressBar() {
        // Xóa các task đang chạy trước đó để tránh đụng độ
        if (updateProgressAction != null) {
            handler.removeCallbacks(updateProgressAction);
        }

        updateProgressAction = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    songProgressBar.setProgress(currentPos);
                    tvCurrentTime.setText(formatTime(currentPos));

                    // Cho vòng lặp chạy lại sau mỗi 1000ms (1 giây)
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateProgressAction);
    }

    // Đổi định dạng mili-giây sang mm:ss cho dễ nhìn
    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !songList.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastUpdate) > 1000) {
                float x = event.values[0];
                if (x < -15.0f) {
                    nextSong();
                    Toast.makeText(this, "Chuyển bài", Toast.LENGTH_SHORT).show();
                    lastUpdate = currentTime;
                } else if (x > 15.0f) {
                    prevSong();
                    Toast.makeText(this, "Quay lại", Toast.LENGTH_SHORT).show();
                    lastUpdate = currentTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

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
        // Dừng nhạc khi ẩn app và đổi nút thành Play
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setText("▶");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Dọn dẹp Handler tránh tràn bộ nhớ
        if (handler != null && updateProgressAction != null) {
            handler.removeCallbacks(updateProgressAction);
        }
    }
}