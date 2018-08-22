package com.plusqa.shake_report;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.View;
import android.widget.EditText;

public class TextDrawing extends Drawing {

    private DynamicLayout layout;
    private String text;
    private float x, y;


    TextDrawing(float x, float y, Paint paint) {
        super(x, y, paint);

        RectF rectF = new RectF();
        super.setRectF(rectF);
    }

    @Override
    public boolean contains(float x, float y) {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawTextOnPath(text, this, x, y, getPaint());

    }

    public String getText() {

        if (text == null) {
            text = " ";
        }

        return text;
    }

    public float getY() {
        return y;
    }
}
