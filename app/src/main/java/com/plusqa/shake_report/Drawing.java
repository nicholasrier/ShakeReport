package com.plusqa.shake_report;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.ArrayList;


public abstract class Drawing extends Path {

    private Paint paint;

    private boolean isDeleted;

    private RectF rectF;

    private float offsetX, offsetY, scaleX, scaleY;

    private ArrayList<Adjustment> doneAdjustments = new ArrayList<>();

    private ArrayList<Adjustment> undoneAdjustments = new ArrayList<>();

    private class Adjustment {

        float offsetX, offsetY, scaleX, scaleY;

        Adjustment(float offsetX, float offsetY, float scaleX, float scaleY) {

            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }
    }

    Drawing(float x, float y, Paint paint) {

        super();

        this.paint = paint;

        moveTo(x, y);
    }

//  -------- Gets and sets --------

    public Paint getPaint() {
        return paint;
    }

    void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    RectF getRectF() {
        return rectF;
    }


//  -------- Abstract methods --------

    public abstract boolean contains(float x, float y);


//  -------- Concrete methods --------


    public void offsetDrawing(float offsetX, float offsetY){

        rectF.offsetTo(rectF.left + offsetX,
                rectF.top + offsetY);

        this.offsetX += offsetX;
        this.offsetY += offsetY;

    }

    public void scaleDrawing(float scaleX, float scaleY) {

        rectF.right += scaleX / 2;
        rectF.left -= scaleX / 2;

        rectF.bottom += scaleY / 2;
        rectF.top -= scaleY / 2;

        this.scaleX += scaleX;
        this.scaleY += scaleY;

    }

    private void clearAdjustment() {

        offsetX = 0;
        offsetY = 0;
        scaleX = 0;
        scaleY = 0;

    }

    public void saveAdjustment() {

        doneAdjustments.add(new Adjustment(offsetX, offsetY, scaleX, scaleY));

        clearAdjustment();

        undoneAdjustments.clear();

    }

    public void redoAdjust() {

        if (!undoneAdjustments.isEmpty()) {

            Adjustment la = undoneAdjustments.get(undoneAdjustments.size() - 1);

            doneAdjustments.add(la);

            undoneAdjustments.remove(la);

            offsetDrawing(la.offsetX, la.offsetY);

            scaleDrawing(la.scaleX, la.scaleY);

            clearAdjustment();

        }
    }

    public void undoAdjust() {

        if (!doneAdjustments.isEmpty()) {

            Adjustment la = doneAdjustments.get(doneAdjustments.size() - 1);

            undoneAdjustments.add(la);

            doneAdjustments.remove(la);

            offsetDrawing(-la.offsetX, -la.offsetY);

            scaleDrawing(-la.scaleX, -la.scaleY);

            clearAdjustment();

        }
    }

    public void delete() {

        isDeleted = true;

        offsetDrawing(-offsetX, -offsetY);

        scaleDrawing(-scaleX, -scaleY);

        clearAdjustment();

    }

    public void redoDelete() {
        isDeleted = true;
    }


    public void undoDelete() {
        isDeleted = false;
    }

}
