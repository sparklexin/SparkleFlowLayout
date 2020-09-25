package xin.sparkle.flowlayout.sample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import xin.sparkle.flowlayout.SparkleFlowLayout;

public class MainActivity extends AppCompatActivity {
    int num = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SparkleFlowLayout flowLayout = findViewById(R.id.flow_layout);
        findViewById(R.id.add).setOnClickListener(v -> {
            TextView tv = new TextView(this);
            tv.setText(String.format(Locale.getDefault(), "Hello %s", num++));
            flowLayout.addView(tv);
        });

        findViewById(R.id.clear).setOnClickListener(v -> flowLayout.removeAllViews());
    }
}