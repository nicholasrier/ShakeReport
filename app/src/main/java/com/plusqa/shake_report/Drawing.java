package com.plusqa.shake_report;

import android.graphics.Paint;
import android.graphics.Path;


public abstract class Drawing extends Path {

    Paint paint;
    Boolean deleted = false;

    public abstract boolean contains(float x, float y);

    public abstract void offsetDrawing(float offsetX, float offsetY);

    public abstract Drawing copy();

    public void delete() {
        deleted = true;
    }

    void restore() {
        deleted = false;
    }

    boolean isDeleted() {
        return deleted;
    }

    Paint getPaint() {
        return paint;
    }

}
