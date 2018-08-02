package com.plusqa.shake_report;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import java.io.File;
import android.graphics.Rect;
import android.graphics.Point;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String image_name = "screenshot.jpg";
    public static final String log_name = "logcat.txt";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent();
            }
            private void handleShakeEvent() {

                // Save the logCat internally
                Context context = getApplicationContext();
                File log_directory = getDir("logDir", Context.MODE_PRIVATE);
                try {
                    Runtime.getRuntime().exec(new String[]{"logcat", "-df", log_directory.toString() + "/" + log_name});
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Capture screen in a bitmap
                android.view.View view = getWindow().getDecorView();
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();
                Bitmap b1 = view.getDrawingCache();
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, size.x, size.y - statusBarHeight);
                view.destroyDrawingCache();

                // Save bitmap to internal memory
                Utils.saveBitmap(context, image_name, b);

                // Start a new activity to display and markup screenshot
                Intent intent = new Intent(MainActivity.this, ScreenShotMarkUp.class);
                startActivity(intent);
            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

}
