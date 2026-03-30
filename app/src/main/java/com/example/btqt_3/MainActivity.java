package com.example.btqt_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnApp1 = findViewById(R.id.btnApp1);
        Button btnApp2 = findViewById(R.id.btnApp2);
        Button btnApp3 = findViewById(R.id.btnApp3);
        Button btnApp4 = findViewById(R.id.btnApp4);


        btnApp2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity2.class); // Đổi thành Activity2.class
            startActivity(intent);
        });

        btnApp3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity3.class); // Đổi thành Activity2.class
            startActivity(intent);
        });
    }

}