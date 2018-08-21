package com.plusqa.shake_report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class FormatAndSend extends AppCompatActivity
        implements ImageRecyclerViewAdapter.ItemClickListener {

    InputMethodManager imm;
    private List<Bitmap> images;
    private ImageRecyclerViewAdapter adapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format_and_send);

        Toolbar mTopToolbar = findViewById(R.id.format_and_send_toolbar);
        setSupportActionBar(mTopToolbar);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Bitmap screenShotBM = Utils.getBitMap(getApplicationContext(), MainActivity.image_name);

        images = new ArrayList<>();
        images.add(screenShotBM); images.add(screenShotBM);images.add(screenShotBM);

        RecyclerView recyclerView = findViewById(R.id.previewScroll);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(FormatAndSend.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        adapter = new ImageRecyclerViewAdapter(this, images);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        //Tapping outside of fields will clear focus and collapse keyboard
        ConstraintLayout constraintLayout = findViewById(R.id.base_layout);
        constraintLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                deselectView(getCurrentFocus());
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.format_report_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public void deselectView(View v) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        v.clearFocus();
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width,
                height, filter);
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
