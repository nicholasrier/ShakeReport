package com.plusqa.shake_report;

import android.graphics.Paint;
import android.graphics.RectF;

public class TextDrawing extends Drawing {

    public String text = " ";
    public float x, y;


    TextDrawing(float x, float y, Paint paint) {
        super(x, y, paint);

        this.x = x; this.y = y;

        RectF rectF = new RectF();
        super.setRectF(rectF);
    }

    @Override
    public boolean contains(float x, float y) {
        return false;
    }

}
