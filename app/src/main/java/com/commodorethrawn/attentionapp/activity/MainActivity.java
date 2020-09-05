package com.commodorethrawn.attentionapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.commodorethrawn.attentionapp.R;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences preferences;
    private FirebaseFunctions functions;
    private Button attentionButton;
    private TextView feedbackText;
    private long lastClick;
    private static LightingColorFilter buttonPressFilter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        functions = FirebaseFunctions.getInstance();
        preferences = getSharedPreferences("attentionapp", MODE_PRIVATE);
        feedbackText = findViewById(R.id.feedbackText);
        attentionButton = findViewById(R.id.attention_button);
        attentionButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isFirstLaunch = !preferences.getBoolean("isSetup", false);
        if (isFirstLaunch) {
            Intent intent = new Intent(MainActivity.this, FirstLaunchActivity.class);
            startActivity(intent);
        } else if (preferences.getBoolean("isBoyfriend", false)) {
            finishAndRemoveTask();
        }
    }

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - lastClick > 8000) {
            requestAttention();
            lastClick = System.currentTimeMillis();
            doClickAnimation(attentionButton);
        } else {
            attentionButton.setBackground(getDrawable(R.drawable.my_btn_err));
            Timer timerReset = new Timer();
            timerReset.schedule(new TimerTask() {
                @Override
                public void run() {
                    attentionButton.setBackground(getDrawable(R.drawable.my_btn));
                }
            }, 1000);
        }
    }

    private void doClickAnimation(Button button) {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
        button.getBackground().setColorFilter(buttonPressFilter);
        Timer timerReset = new Timer();
        timerReset.schedule(new TimerTask() {
            @Override
            public void run() {
                attentionButton.getBackground().setColorFilter(null);
            }
        }, 100);
    }

    private void requestAttention() {
        functions
                .getHttpsCallable("requestAttention")
                .call()
                .continueWith(task -> {
                    String result = (String) Objects.requireNonNull(task.getResult()).getData();
                    displayResult(result);
                    return result;
                });
    }

    private void displayResult(String result) {
        feedbackText.setText(result);
        Timer timerReset = new Timer();
        timerReset.schedule(new TimerTask() {
            @Override
            public void run() {
                feedbackText.setText("");
            }
        }, 3000);
    }

}