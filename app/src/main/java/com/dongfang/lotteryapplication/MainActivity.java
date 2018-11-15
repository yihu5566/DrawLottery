package com.dongfang.lotteryapplication;

import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button btnStop;
    private LotteryView lotteryView;
    private Button btnStart;
    private String[] str = {"奖品1", "奖品2", "奖品3", "奖品4", "奖品5", "奖品6", "奖品7", "奖品8",
            "奖品9", "奖品10", "奖品11", "奖品12", "奖品13", "奖品14", "奖品15", "奖品16"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lotteryView = findViewById(R.id.lottery_view);
        btnStop = findViewById(R.id.btn_stop);
        btnStart = findViewById(R.id.btn_start);

        SurfaceHolder holder = lotteryView.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtil.d("MainActivity--调用surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtil.d("MainActivity--调用surfaceChanged");

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtil.d("MainActivity--调用surfaceDestroyed");

            }
        });
        //设置元数据
        lotteryView.setAwardList(Arrays.asList(str));

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lotteryView.stopLottery();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                lotteryView.startLottery(LotteryView.IS_LOTTERYING);
            }
        });
    }
}
