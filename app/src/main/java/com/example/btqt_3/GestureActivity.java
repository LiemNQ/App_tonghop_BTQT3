package com.example.btqt_3;

import android.Manifest;
import android.content.pm.PackageManager;import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestureActivity extends AppCompatActivity {
    private static final String TAG = "GestureAI";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView viewFinder;
    private TextView txtGestureResult;
    private ExecutorService cameraExecutor;
    private GestureRecognizer gestureRecognizer;
    private boolean isRecognizerReady = false;
    private final String SERVER_IP = "http://192.168.89.246:8080";
    private String lastCommand = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        // Ánh xạ View chính xác theo XML của bạn
        viewFinder = findViewById(R.id.viewFinder);
        txtGestureResult = findViewById(R.id.txtGestureResult);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 1. Khởi tạo AI trước
        setupGestureRecognizer();

        // 2. Kiểm tra quyền và mở Camera
        if (checkPermission()) {
            startCamera();
        } else {
            requestPermission();
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

                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Log.d("GestureAI", "Gửi lệnh " + endpoint + " thành công!");
                    } else {
                        Log.e("GestureAI", "Lỗi server: " + responseCode);
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void setupGestureRecognizer() {
        cameraExecutor.execute(() -> {
            try {
                BaseOptions baseOptions = BaseOptions.builder()
                        .setModelAssetPath("gesture_recognizer.task")
                        .build();

                GestureRecognizer.GestureRecognizerOptions options =
                        GestureRecognizer.GestureRecognizerOptions.builder()
                                .setBaseOptions(baseOptions)
                                .setRunningMode(RunningMode.LIVE_STREAM)
                                .setResultListener(this::onGestureResult)
                                .build();

                gestureRecognizer = GestureRecognizer.createFromOptions(this, options);
                isRecognizerReady = true;
                Log.d(TAG, "MediaPipe đã sẵn sàng");
            } catch (Exception e) {
                isRecognizerReady = false;
                Log.e(TAG, "LỖI QUAN TRỌNG: Không tìm thấy file gesture_recognizer.task trong assets!");
                runOnUiThread(() -> Toast.makeText(this, "Thiếu file model AI trong assets!", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void onGestureResult(GestureRecognizerResult result, MPImage mpImage) {
        if (result != null && !result.gestures().isEmpty()) {
            String gestureName = result.gestures().get(0).get(0).categoryName();
            runOnUiThread(() -> {
                txtGestureResult.setText("Cử chỉ: " + gestureName);
                processCommand(gestureName);
            });
        }
    }

    private void processCommand(String gesture) {
        // Nếu cử chỉ giống hệt lần trước thì thoát (để không gửi 30 lệnh/giây gây treo server)
        if (gesture.equals(lastCommand)) return;

        lastCommand = gesture; // Lưu lại cử chỉ hiện tại

        switch (gesture) {
            case "Open_Palm": // Xòe tay
                txtGestureResult.setText("Lệnh: BẬT QUẠT");
                sendIoTCommand("/fan/on");
                break;

            case "Closed_Fist": // Nắm tay
                txtGestureResult.setText("Lệnh: TẮT QUẠT");
                sendIoTCommand("/fan/off");
                break;

            case "Victory": // chữ V
                txtGestureResult.setText("Lệnh: BẬT TV");
                sendIoTCommand("/tv/on");
                break;

            case "Pointing_Up": // ngón trỏ hướng lên
                txtGestureResult.setText("Lệnh: TẮT TV");
                sendIoTCommand("/tv/off");
                break;

            case "None":
                lastCommand = ""; // Reset để có thể nhận lệnh tiếp theo
                break;
        }
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if (isRecognizerReady && gestureRecognizer != null) {
                        // SỬA TẠI ĐÂY: Chạy việc lấy bitmap trên Main Thread
                        runOnUiThread(() -> {
                            Bitmap bitmap = viewFinder.getBitmap();
                            if (bitmap != null) {
                                // Sau khi có bitmap, đẩy việc nhận diện AI ra luồng phụ để không treo máy
                                cameraExecutor.execute(() -> {
                                    try {
                                        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
                                        gestureRecognizer.recognizeAsync(mpImage, SystemClock.uptimeMillis());
                                    } catch (Exception e) {
                                        Log.e("GestureAI", "Lỗi nhận diện: " + e.getMessage());
                                    }
                                });
                            }
                        });
                    }
                    image.close();
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi Camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gestureRecognizer != null) gestureRecognizer.close();
        cameraExecutor.shutdown();
    }
}