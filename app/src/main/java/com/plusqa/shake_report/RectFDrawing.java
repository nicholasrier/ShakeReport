package com.plusqa.shake_report;

import android.graphics.Paint;
import android.graphics.RectF;

public class RectFDrawing extends Drawing {

    RectFDrawing(float x, float y, Paint paint) {

        super();

        RectF rectF = new RectF(x - 200,
                y - 200,
                x + 200,
                y + 200);

        setRectF(rectF);

        setPaint(paint);

        addRect(rectF, Direction.CW);

    }

    @Override
    public boolean contains(float x, float y) {

        return super.getRectF().contains(x, y);

    }

    @Override
    public void offsetDrawing(float offsetX, float offsetY) {

        super.offsetDrawing(offsetX, offsetY);

        reset();

        addRect(getRectF(), Direction.CW);

    }

    @Override
    public void scaleDrawing(float scaleX, float scaleY) {

        super.scaleDrawing(scaleX, scaleY);

        reset();

        addRect(getRectF(), Direction.CW);

    }

}
