package com.example.sf.demo1205a;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

//贝塞尔曲线画薄荷叶
public class MainActivity extends AppCompatActivity {
    private LeafAnimView mLeafView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLeafView = findViewById(R.id.leafView);
        //开始画薄荷叶
        findViewById(R.id.start_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLeafView.start();
            }
        });
        //暂停画薄荷叶
        findViewById(R.id.end_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLeafView.pause();
            }
        });

    }
}
