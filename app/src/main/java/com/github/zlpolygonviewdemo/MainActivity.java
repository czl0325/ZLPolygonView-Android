package com.github.zlpolygonviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.zlpolygonview.ZLPolygonView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ZLPolygonView polygonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        polygonView = findViewById(R.id.polygonview);
        List<Float> values = new ArrayList<>();
        for (int i=0; i<4; i++) {
            values.add((float) (Math.random()*100/100));
        }
        polygonView.setPolygonValues(values);
    }
}
