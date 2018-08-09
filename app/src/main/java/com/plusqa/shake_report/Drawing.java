package com.plusqa.shake_report;

public interface Drawing {

    boolean contains(float x, float y);

    void offsetDrawing(float offsetX, float offsetY);

    Drawing copy();

    void delete();

    void restore();

}
