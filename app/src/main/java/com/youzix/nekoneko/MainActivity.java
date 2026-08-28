package com.youzix.nekoneko;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 设置欢迎文本
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("欢迎使用 NekoNeko 应用！");
    }
}
