package com.plusqa.shake_report;

import android.graphics.Paint;
import android.graphics.RectF;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;

public class TextDrawing extends Drawing {

    private DynamicLayout layout;
    private String text;


    TextDrawing(float x, float y, Paint paint) {
        super(x, y, paint);

        layout = new DynamicLayout("a", new TextPaint(paint), 1, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

        RectF rectF = new RectF();
        super.setRectF(rectF);
    }

    @Override
    public boolean contains(float x, float y) {
        return false;
    }

}
