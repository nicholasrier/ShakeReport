package com.plusqa.shake_report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;

public class ImageViewDraw extends android.support.v7.widget.AppCompatImageView {

    private Paint selectedPaint;

    private Canvas mCanvas;

    private Bitmap  mBitmap;

    // Designates the tool that is selected
    private int toolFlag;

    // Tool options
    public static final int DRAW_TOOL = 1;
    public static final int RECT_TOOL = 2;
    public static final int OVAL_TOOL = 4;
    public static final int TEXT_TOOL = 8;

    // List of Drawing objects to be drawn in onDraw()
    private ArrayList<Drawing> drawings = new ArrayList<>();

    // List of actions performed by user - recorded in onTouch()
    private ArrayList<Action> doneActions = new ArrayList<>();

    // List of actions that have been reversed by undo()
    private ArrayList<Action> undoneActions = new ArrayList<>();

    // The drawing that is currently being placed / edited
    private Drawing touchedDrawing;

    // The action that is currently being performed
    private Action action;

    // Previous touch coordinates
    private float prevX, prevY;

    private boolean isDrawingInTrash = false;

    private ScaleGestureDetector mScaleGestureDetector;

    public ImageViewDraw(Context context, int defaultColor) {
        super(context);
        initPaint(defaultColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        else {
            mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h,true);
        }
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        for (Drawing drawing : drawings) {
            if (!drawing.isDeleted()) { // "Deleted" drawings are not drawn
                canvas.drawPath(drawing, drawing.getPaint());
            }
        }

        canvas.save();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        performClick();

        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();

        int index = event.getActionIndex();
        int currentPointerId = event.getPointerId(index);
        int firstPointerIndex = -1;
        int firstPointerID = -1;

        boolean isNewDrawing = false;

        mScaleGestureDetector.onTouchEvent(event);

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                prevX = x;
                prevY = y;

                isNewDrawing = isNewDrawing(x, y);

                if (isNewDrawing && drawings.size() < 50) {
                    makeDrawing(x, y);
                }

                firstPointerIndex = event.getActionIndex();
                firstPointerID = event.getPointerId(firstPointerIndex);

                break;

            case MotionEvent.ACTION_MOVE:
                if (touchedDrawing == null || currentPointerId == firstPointerID) break;

                break;

            case MotionEvent.ACTION_UP:



                break;

            case MotionEvent.ACTION_POINTER_UP:



                break;
        }

        return true;
    }

    // Returns true if placing new drawing
    private boolean isNewDrawing(float x, float y) {

        boolean isNew = true;

        for (Drawing drawing : drawings) {

            if (!drawing.isDeleted() && drawing.contains(x, y)) {

                touchedDrawing = drawing;

                action = new AdjustDrawing(drawing, drawings);

                isNew = false;

            }
        }

        return isNew;
    }

    public void initPaint(int defaultColor) {
        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setDither(true);
        selectedPaint.setColor(defaultColor);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeJoin(Paint.Join.ROUND);
        selectedPaint.setStrokeCap(Paint.Cap.ROUND);
        selectedPaint.setStrokeWidth(12);
    }

    public void setColor(int color) {
        selectedPaint.setColor(color);
    }

    public void setToolFlag(int toolFlag) {
        if (toolFlag == DRAW_TOOL || toolFlag == RECT_TOOL ||
                toolFlag == OVAL_TOOL || toolFlag == TEXT_TOOL) {
            this.toolFlag = toolFlag;
        }
    }

    public void undo() {

        // Get the latest done action
        Action latestAction = doneActions.get(doneActions.size() - 1);

        // Undo the action
        latestAction.undoAction();

        // Place action in list of undone actions
        undoneActions.add(latestAction);

        // Remove from list of done actions
        doneActions.remove(latestAction);
    }

    public void redo() {

        // Get the latest undone action
        Action latestUndoneAction = undoneActions.get(doneActions.size() - 1);

        // Redo the action
        latestUndoneAction.doAction();

        // Place action in list of done actions
        doneActions.add(latestUndoneAction);

        // Remove from list of undone actions
        undoneActions.remove(latestUndoneAction);
    }

    private void makeDrawing(float x, float y) {

        switch (toolFlag) {

            case DRAW_TOOL:

                PointF pF = new PointF(x, y);
                ArrayList<PointF> pointFs = new ArrayList<>();
                pointFs.add(pF);

                touchedDrawing = new LineDrawing(pointFs, selectedPaint);

                break;

            case RECT_TOOL:

                RectF r = new RectF(x - 200,
                        y - 200,
                        x + 200,
                        y + 200);

                touchedDrawing = new RectFDrawing(r, selectedPaint);

                break;

            case OVAL_TOOL:

                RectF o = new RectF(x - 200,
                        y - 200,
                        x + 200,
                        y + 200);

                touchedDrawing = new OvalDrawing(o, selectedPaint);

                break;
        }

        action = new MakeDrawing(touchedDrawing, drawings);

        drawings.add(touchedDrawing);

    }


    // DRAWINGS

    private class RectFDrawing extends Drawing {
        RectF rectF;

        RectFDrawing(RectF rectF, Paint paint) {
            super();
            this.rectF = rectF;
            this.paint = paint;
            addToPath();
        }

        //Copy constructor
        private RectFDrawing(RectFDrawing drawingToCopy) {
            super();
            RectF rectFCopy = new RectF(drawingToCopy.rectF);
            Paint paintCopy = new Paint(drawingToCopy.paint);

            this.rectF = rectFCopy;
            this.paint = paintCopy;
            addToPath();
        }

        @Override
        public boolean contains(float x, float y) {
            return this.rectF.contains(x, y);
        }

        @Override
        public void offsetDrawing(float offsetX, float offsetY) {
            rectF.offsetTo(rectF.left + offsetX,
                    rectF.top + offsetY);
            addToPath();
        }

        @Override
        public Drawing copy() {
            return new RectFDrawing(this);
        }

        private void addToPath() {
            reset();
            addRect(rectF, Path.Direction.CW);
        }

    }

    private class OvalDrawing extends Drawing{
        RectF rectF;

        OvalDrawing(RectF rectF, Paint paint) {
            super();
            this.rectF = rectF;
            this.paint = paint;
            addToPath();
        }

        // Copy constructor
        private OvalDrawing(OvalDrawing drawingToCopy) {
            super();
            RectF rectFCopy = new RectF(drawingToCopy.rectF);
            Paint paintCopy = new Paint(drawingToCopy.paint);

            this.rectF = rectFCopy;
            this.paint = paintCopy;

            addToPath();
        }

        @Override
        public boolean contains(float x, float y) {
            float dx = x - rectF.centerX();
            float dy = y - rectF.centerY();
            float width = rectF.width()/2;
            float height = rectF.height()/2;
            return (dx * dx) / (width * width) + (dy * dy) / (height * height) <= 1;
        }

        @Override
        public void offsetDrawing(float offsetX, float offsetY) {
            rectF.offsetTo(rectF.left + offsetX,
                    rectF.top + offsetY);
            addToPath();
        }

        @Override
        public Drawing copy() {
            return new OvalDrawing(this);
        }

        private void addToPath() {
            reset();
            addOval(rectF, Path.Direction.CW);
        }
    }

    private class LineDrawing extends Drawing {

        ArrayList<PointF> points;

        LineDrawing(ArrayList<PointF> points, Paint paint) {
            super();
            this.points = points;
            this.paint = paint;
        }

        // Copy constructor
        private LineDrawing(LineDrawing drawingToCopy) {
            super();

            ArrayList<PointF> pointsCopy = new ArrayList<>();

            for (PointF p : this.points) {
                if (p != null) {
                    pointsCopy.add(new PointF(p.x, p.y));
                }
            }

            Paint paintCopy = new Paint(this.paint);

            this.points = pointsCopy;
            this.paint = paintCopy;
        }

        @Override
        public boolean contains(float x, float y) {
            boolean inside = false;
            RectF bounds = new RectF(x - 65,y - 65,
                    x + 65,y + 65);
            for (PointF p : points ) {
                if (bounds.contains(p.x, p.y)) {
                    inside = true;
                }
            }
            return inside;
        }

        @Override
        public void offsetDrawing(float offsetX, float offsetY) {
            for (PointF p : points ) {
                p.set(p.x += offsetX, p.y += offsetY);
            }
            offset(offsetX, offsetY);
        }

        @Override
        public Drawing copy() {
            return new LineDrawing(this);
        }

    }



    // ACTIONS

    private class MakeDrawing implements Action {

        Drawing drawing;
        ArrayList<Drawing> drawings;
        int index;


        MakeDrawing(Drawing drawing, ArrayList<Drawing> drawings) {

            this.drawing = drawing;
            this.drawings = drawings;

        }

        @Override
        public void undoAction() {
            index = drawings.indexOf(drawing);
            drawings.remove(drawing);
        }


        @Override
        public void doAction() {
            drawings.add(index, drawing);
        }

    }

    private class AdjustDrawing implements Action {

        Drawing drawing;
        Drawing savedState;
        ArrayList<Drawing> drawings;

        AdjustDrawing(Drawing drawing, ArrayList<Drawing> drawings) {
            this.drawing = drawing;
            this.drawings = drawings;
            savedState = drawing.copy();
        }

        @Override
        public void undoAction() {
            drawings.set(drawings.indexOf(drawing), savedState);
        }

        @Override
        public void doAction() {
            drawings.set(drawings.indexOf(savedState), drawing);
        }

    }

    private class DeleteDrawing implements Action {

        Drawing drawing;

        DeleteDrawing(Drawing drawing) {
            this.drawing = drawing;
        }

        @Override
        public void undoAction() {
            drawing.restore();
        }

        @Override
        public void doAction() {
            drawing.delete();
        }
    }


}
