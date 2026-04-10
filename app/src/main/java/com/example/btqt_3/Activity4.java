package com.example.btqt_3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity4 extends AppCompatActivity {

    // Thay địa chỉ IP này bằng IP máy tính của bạn để test (Ví dụ: 192.168.1.5)
    // private final String SERVER_IP = "http://192.168.31.100:8080";
    private final String SERVER_IP = "http://192.168.89.246:8080";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);

        Button btnFanOn = findViewById(R.id.btnFanOn);
        Button btnFanOff = findViewById(R.id.btnFanOff);
        Button btnTVOn = findViewById(R.id.btnTVOn);
        Button btnTVOff = findViewById(R.id.btnTVOff);
// Trong phương thức onCreate của Activity4.java
        Button btnOpenAI = findViewById(R.id.btnOpenAI);

        btnOpenAI.setOnClickListener(v -> {
            // Chuyển sang màn hình nhận diện cử chỉ tay
            Intent intent = new Intent(Activity4.this, GestureActivity.class);
            startActivity(intent);
        });
        btnFanOn.setOnClickListener(v -> sendIoTCommand("/fan/on"));
        btnFanOff.setOnClickListener(v -> sendIoTCommand("/fan/off"));
        btnTVOn.setOnClickListener(v -> sendIoTCommand("/tv/on"));
        btnTVOff.setOnClickListener(v -> sendIoTCommand("/tv/off"));
    }

    private void sendIoTCommand(String endpoint) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_IP + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000); // Timeout sau 2 giây nếu không thấy thiết bị

                int responseCode = conn.getResponseCode();

                // Hiển thị kết quả lên màn hình
                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi server: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Không kết nối được tới thiết bị!", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}