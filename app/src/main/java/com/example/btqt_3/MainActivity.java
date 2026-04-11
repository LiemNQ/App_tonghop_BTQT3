package com.example.btqt_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

        CardView cardApp1 = findViewById(R.id.cardApp1);
        CardView cardApp2 = findViewById(R.id.cardApp2);
        CardView cardApp3 = findViewById(R.id.cardApp3);
        CardView cardApp4 = findViewById(R.id.cardApp4);

        cardApp1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity1.class);
            startActivity(intent);
        });

        cardApp2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity2.class);
            startActivity(intent);
        });

        cardApp3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity3.class);
            startActivity(intent);
        });

        cardApp4.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity4.class);
            startActivity(intent);
        });
    }
}