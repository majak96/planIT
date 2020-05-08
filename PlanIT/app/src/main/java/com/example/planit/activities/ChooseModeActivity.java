package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.planit.MainActivity;
import com.example.planit.R;

public class ChooseModeActivity extends AppCompatActivity {
    private Button personalModeButton;
    private Button teamsModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        personalModeButton = findViewById(R.id.personalModeButton);
        teamsModeButton = findViewById(R.id.teamsModeButton);

        personalModeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
                intent.putExtra("page", "personal");
                startActivity(intent);
            }
        });
        teamsModeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(ChooseModeActivity.this, MainActivity.class);
                intent.putExtra("page", "teams");
                startActivity(intent);
            }
        });
    }
}
