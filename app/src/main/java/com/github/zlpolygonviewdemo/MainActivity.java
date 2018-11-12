package com.github.zlpolygonviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.github.zlpolygonview.ZLPolygonView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ZLPolygonView polygonView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit);
        polygonView = findViewById(R.id.polygonview);
        List<Float> values = new ArrayList<>();
        for (int i=0; i<4; i++) {
            values.add((float) (Math.random()*50/100+0.5));
        }
        polygonView.setPolygonValues(values);
        polygonView.setOnClickPolygonListeren(new ZLPolygonView.onClickPolygonListeren() {
            @Override
            public void onClickPolygon(MotionEvent event, int index) {
                View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);

                new ValuePopupWindow(MainActivity.this).showAtLocation(rootView,
                        Gravity.TOP|Gravity.LEFT,
                        (int)event.getRawX(), (int)event.getRawY());
            }
        });
    }

    public void clickReload(View view) {
        int number = Integer.parseInt(editText.getText().toString());
        List<Float> values = new ArrayList<>();
        for (int i=0; i<number; i++) {
            values.add((float) (Math.random()*50/100+0.5));
        }
        polygonView.setPolygonValues(values);
    }
}
