package com.plusqa.shake_report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.FloatingActionButton;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

public class AnnotationView extends android.support.v7.widget.AppCompatEditText {

    private Paint selectedPaint;

    private TrashCan trashCan;

    private Rect trashCanRect;

    private FloatingActionButton trashCanFAB;

    private Canvas mCanvas;

    private Bitmap  mBitmap;

    private int firstPointerID = -1;

    InputMethodManager imm;

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

    // Previous touch coordinates
    private float prevX, prevY;

    private boolean isDrawingInTrash = false;

    // Scales drawings
    private ScaleGestureDetector mScaleGestureDetector;

    // Should create the trashcan layout and fab programmatically to include with this
    // ... maybe the tools menu as well

    public AnnotationView(Context context) {
        super(context);

        initPaint();

        mScaleGestureDetector = new ScaleGestureDetector(context,
                new DrawingScaleListener());

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


    boolean isNewDrawing;

    boolean firstTouch = true;

    float offsetX, offsetY, scaleX, scaleY;

    int currentPointerId = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        performClick();

        final float x = event.getX();
        final float y = event.getY();
        final int eventAction = event.getAction();

        int index = event.getActionIndex();
        currentPointerId = event.getPointerId(index);

        mScaleGestureDetector.onTouchEvent(event);

        switch (eventAction & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                if (firstTouch) {

                    int firstPointerIndex = event.getActionIndex();
                    firstPointerID = event.getPointerId(firstPointerIndex);

                    firstTouch = false;

                }

                prevX = x;
                prevY = y;

                isNewDrawing = isNewDrawing(x, y);

                if (isNewDrawing && drawings.size() < 50) {

                    makeDrawing(x, y);
                    touchedDrawing.moveTo(x, y);

                    if (toolFlag != DRAW_TOOL) {
//                        trashCanFAB.show();
                    }

                }

                invalidate();

                break;

            case MotionEvent.ACTION_MOVE:

                if (touchedDrawing == null || currentPointerId != firstPointerID) {
                    break;
                }

                if (isNewDrawing && toolFlag == DRAW_TOOL) {

                        touchedDrawing.quadTo(prevX, prevY, x, y);

                } else {

                    touchedDrawing.offsetDrawing(x - prevX, y - prevY);

                    touchedDrawing.addToOffsetXY(offsetX, offsetY);

//                    trashCanHover((int) event.getRawX(), (int) event.getRawY());
                }

                prevX = x;
                prevY = y;

                invalidate();

                break;

            case MotionEvent.ACTION_UP:

                Action action;

                if (touchedDrawing == null) {
                    break;
                }

                if (isDrawingInTrash) {

                    touchedDrawing.delete();

                    action = new DeleteDrawing(touchedDrawing);

                } else {

                    if (isNewDrawing) {

                        action = new MakeDrawing(touchedDrawing);

                    } else {

                        touchedDrawing.saveAdjustment();

                        action = new AdjustDrawing(touchedDrawing);

                    }
                }

                doneActions.add(action);
                undoneActions.clear();

                isNewDrawing = false;
                isDrawingInTrash = false;

                invalidate();

                break;

            case MotionEvent.ACTION_POINTER_UP:

//                trashCanFAB.hide();


                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (currentPointerId == firstPointerID) {
                    firstPointerID = -1;
                }

        }

        firstTouch = true;
        return true;
    }

    // Returns true if placing new drawing
    // If not, returns false and assigns touched drawing
    private boolean isNewDrawing(float x, float y) {

        boolean isNew = true;

        for (Drawing drawing : drawings) {

            if (!drawing.isDeleted() && drawing.contains(x, y)) {

                touchedDrawing = drawing;

                isNew = false;

            }
        }

        return isNew;
    }

    // Creates new drawings of type specified by toolFlag
    private void makeDrawing(float x, float y) {

        switch (toolFlag) {

            case DRAW_TOOL:

                touchedDrawing = new LineDrawing(x, y, selectedPaint);

                break;

            case RECT_TOOL:

                touchedDrawing = new RectFDrawing(x, y, selectedPaint);

                break;

            case OVAL_TOOL:

                touchedDrawing = new OvalDrawing(x, y, selectedPaint);

                break;
        }

        drawings.add(touchedDrawing);

    }

    private void trashCanHover(int x, int y) {
        if (trashCanRect != null) {
            if (trashCanRect.contains(x, y)) {
                if (!isDrawingInTrash) {

                    trashCanFAB.setScaleX(1.3f);
                    trashCanFAB.setScaleY(1.3f);

                    isDrawingInTrash = true;
                }
            } else {
                if (isDrawingInTrash) {

                    trashCanFAB.setScaleX(1);
                    trashCanFAB.setScaleY(1);

                    isDrawingInTrash = false;
                }
            }
        }
    }

    private void initPaint() {

        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setDither(true);
        selectedPaint.setColor(Color.parseColor("#51ccc0"));
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

        } else {

            this.toolFlag = 0;

        }
    }

    public void undo() {

        if (doneActions.size() > 0) {

            // Get the latest done action
            Action latestAction = doneActions.get(doneActions.size() - 1);

            // Undo the action
            latestAction.undoAction();

            // Place action in list of undone actions
            undoneActions.add(latestAction);

            // Remove from list of done actions
            doneActions.remove(latestAction);

            invalidate();

        }
    }

    public void redo() {

        if (undoneActions.size() > 0) {

            // Get the latest undone action
            Action latestUndoneAction = undoneActions.get(doneActions.size() - 1);

            // Redo the action
            latestUndoneAction.doAction();

            // Place action in list of done actions
            doneActions.add(latestUndoneAction);

            // Remove from list of undone actions
            undoneActions.remove(latestUndoneAction);

            invalidate();

        }
    }

    // ACTIONS

    private class MakeDrawing implements Action {

        Drawing drawing;
        int index;


        MakeDrawing(Drawing drawing) {
            this.drawing = drawing;
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

        AdjustDrawing(Drawing drawing) {
            this.drawing = drawing;
        }

        @Override
        public void undoAction() {
            drawing.undoAdjust();
        }

        @Override
        public void doAction() {
            drawing.redoAdjust();
        }

    }

    private class DeleteDrawing implements Action {

        Drawing drawing;

        DeleteDrawing(Drawing drawing) {
            this.drawing = drawing;
        }

        @Override
        public void undoAction() {
            drawing.undoDelete();
        }

        @Override
        public void doAction() {
            drawing.redoDelete();
        }
    }


    private class DrawingScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float lastSpanX;
        private float lastSpanY;
        private RectF lastRectF;
        float currentSpanX;
        float currentSpanY;
        final float shapeMinSize = 100;
        float shapeMaxHeight;
        float shapeMaxWidth;
        float canvasHeight;
        float canvasWidth;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            if (touchedDrawing != null && currentPointerId == firstPointerID) {
                lastSpanX = detector.getCurrentSpanX();
                lastSpanY = detector.getCurrentSpanY();
                lastRectF = new RectF(touchedDrawing.getRectF());

                return true;

            } else {

                return false;

            }

        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (touchedDrawing != null) {

                currentSpanX = detector.getCurrentSpanX();
                currentSpanY = detector.getCurrentSpanY();

                float spanXDiff = currentSpanX - lastSpanX;
                float spanYDiff = currentSpanY - lastSpanY;

                boolean scalingUpX = spanXDiff > 0;
                boolean scalingUpY = spanYDiff > 0;

                canvasHeight = mCanvas.getHeight();
                canvasWidth = mCanvas.getWidth();

                shapeMaxWidth = canvasWidth;
                shapeMaxHeight = canvasHeight;

                float scaleX = 0;
                float scaleY = 0;

                RectF currentRectF = touchedDrawing.getRectF();

                if ((!scalingUpX && (lastRectF.width() >= shapeMinSize)) ||
                        (scalingUpX && (lastRectF.width() <= shapeMaxWidth))) {

                    if ((currentRectF.width() + spanXDiff / 2) > shapeMaxWidth) {

                        scaleX = -(currentRectF.width() - shapeMaxWidth);

                    } else if ((currentRectF.width() + spanXDiff / 2) < shapeMinSize) {

                        scaleX = shapeMinSize - currentRectF.width();

                    } else {

                        scaleX =  spanXDiff;
                    }

                }

                if ((!scalingUpY && (lastRectF.height() >= shapeMinSize)) ||
                        (scalingUpY && (lastRectF.height() <= shapeMaxHeight))) {

                    if ((currentRectF.height() + spanYDiff / 2) > shapeMaxHeight) {

                        scaleY = -(currentRectF.height() - shapeMaxHeight);

                    } else if ((currentRectF.height() + spanYDiff / 2) < shapeMinSize) {

                        scaleY = shapeMinSize - currentRectF.height();

                    } else {

                        scaleY = spanYDiff;

                    }

                }

                lastSpanY = currentSpanY;
                lastSpanX = currentSpanX;

                lastRectF = currentRectF;

                touchedDrawing.scaleDrawing(scaleX, scaleY);

                touchedDrawing.addToScaleXY(scaleX, scaleY);

            }

            return true;
        }

    }

}
