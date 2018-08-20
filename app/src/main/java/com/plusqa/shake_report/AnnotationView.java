package com.plusqa.shake_report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class AnnotationView extends FrameLayout {

    private TextDrawing currentText;

    private Paint selectedPaint;

    private RectF deleteArea;

    private Canvas mCanvas;

    private Bitmap  mBitmap;

    boolean textRemoved = false;

    String test = " ";
    float testX, testY;

    private EditText editText;
    OnFocusChangeListener onFocusChangeListenerListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {

                editText.setText(currentText.getText());
                editText.setTextColor(currentText.getPaint().getColor());

            } else {

                Rect newRect = new Rect();
                editText.getDrawingRect(newRect);

                textRemoved = currentText.getText().length() <
                        editText.getText().toString().length();

                if (textRemoved) {
                    currentText.reset();
                    currentText.moveTo(editText.getX(), editText.getY());
                }

                currentText.lineTo(newRect.right, currentText.getY());



            }
        }
    };

    private int firstPointerID = -1;

    ArrayList<TextDrawing> textDrawings =  new ArrayList<>();

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

    private ArrayList<DynamicLayout> textLayouts = new ArrayList<>();

    // The drawing that is currently being placed / edited
    private Drawing touchedDrawing;

    private TextDrawing touchedText;

    DynamicLayout dynamicLayout;

    // Previous touch coordinates
    private float prevX, prevY;

    private boolean deleteFlag = false;
    private boolean wasDrawingInTrash = false;

    // Scales drawings
    private ScaleGestureDetector mScaleGestureDetector;

    public AnnotationView(Context context) {
        this(context, null);

        init(context);

    }

    public AnnotationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    public AnnotationView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);

        init(context);
    }

    public void setDeleteArea(RectF deleteArea) {

        if (deleteArea != null) {
            this.deleteArea = deleteArea;
        }
    }

    private void init(Context context) {

        // Default color
        setPaint(Color.parseColor("#51ccc0"));

        // Default tool
        toolFlag = DRAW_TOOL;

        editText = new EditText(context);
        editText.setOnFocusChangeListener(onFocusChangeListenerListener);
        editText.setVisibility(GONE);
        addView(editText, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        mScaleGestureDetector = new ScaleGestureDetector(context,
                new DrawingScaleListener());

        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setDrawingCacheEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        if (mBitmap == null) { mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); }
        else { mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h,true); }

        mCanvas = new Canvas(mBitmap);
    }

    // You're not just a QA tester, you're an artist
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, null);

        // Draw all not-deleted drawings
        for (Drawing drawing : drawings) {

            if (!drawing.isDeleted()) {

                drawing.draw(canvas);

            }
        }

        canvas.save();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isNewDrawing;

    private boolean firstTouch = true;

    private int currentPointerId = -1;

    private boolean annotating = false;

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

                // Get Id of first touch event for multitouch handling
                if (firstTouch) {

                    firstPointerID = currentPointerId;

                    firstTouch = false;

                }

                prevX = x;
                prevY = y;

                // Touched drawing will be set if isNewDrawing() returns false
                isNewDrawing = isNewDrawing(x, y);

                // Arbitrary limit 50 drawings - should allow value to be modified in init()
                if (isNewDrawing && drawings.size() < 50) {

                    // If no drawing contains touch coordinates, make a new one
                    makeDrawing(x, y);

                    touchedDrawing.moveTo(x, y);
                }

                setAnnotating(true);

                invalidate();


                break;

            // Handles drawing lines and dragging objects around screen
            case MotionEvent.ACTION_MOVE:

                // Prevents multitouch movement issues
                if (touchedDrawing == null || currentPointerId != firstPointerID) {
                    break;
                }

                // Creating a line - can't offset until line is drawn
                if (isNewDrawing && toolFlag == DRAW_TOOL) {

                        touchedDrawing.quadTo(prevX, prevY, x, y);

                } else {

                    // As long as we're not drawing a line - drags drawings
                    touchedDrawing.offsetDrawing(x - prevX, y - prevY);

                    float rX = event.getRawX();
                    float rY = event.getRawY();

                    // Detect if drawing is in delete area
                    if (deleteArea.contains(rX, rY)) {

                        setDeleteFlag(true);
                    } else {

                        setDeleteFlag(false);
                    }
                }

                prevX = x;
                prevY = y;

                invalidate();

                break;

            // Records what action was taken
            case MotionEvent.ACTION_UP:

                if (touchedDrawing == null) {
                    break;
                }

                // To be added to doneActions
                Action action = null;

                // If a new drawing was made, always save a make action
                if (isNewDrawing) {

                    action = new MakeDrawing(touchedDrawing);

                    if (toolFlag == TEXT_TOOL) {

                        editText.setX(x);
                        editText.setY(y);
                        editText.setVisibility(VISIBLE);
                        editText.requestFocus();
                        currentText = (TextDrawing) touchedDrawing;

                    }

                    if (deleteFlag) {

                        doneActions.add(action);

                    }

                }

                if (!deleteFlag && !isNewDrawing) {

                    // Drawings keep track of adjustments in private list
                    touchedDrawing.saveAdjustment();

                    action = new AdjustDrawing(touchedDrawing);

                }

                // If any drawing was deleted, always save a delete action
                if (deleteFlag) {

                    touchedDrawing.delete();

                    action = new DeleteDrawing(touchedDrawing);

                }

                // Record this completed action
                doneActions.add(action);

                // Clear undone actions after completing a new action
                undoneActions.clear();

                // Clear flags
                isNewDrawing = false;
                deleteFlag = false;

                setAnnotating(false);

                invalidate();

                break;

            // Prevents multitouch movement issues
            case MotionEvent.ACTION_POINTER_DOWN:

                if (currentPointerId == firstPointerID) {
                    firstPointerID = -1;
                }

        }

        firstTouch = true;

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Do Code here
        } else {

            test += Character.toString(event.getDisplayLabel());

            TextPaint textPaint = new TextPaint(selectedPaint);

            textPaint.setTextSize(80);

            SpannableStringBuilder base = new SpannableStringBuilder(Character.toString(event.getDisplayLabel()));
            dynamicLayout = new DynamicLayout(base, textPaint, base.length(),
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, true);

        }
        return super.onKeyUp(keyCode, event);
    }

    // Returns true if placing new drawing
    // If not, returns false and assigns touched drawing
    private boolean isNewDrawing(float x, float y) {

        boolean isNew = true;

        for (Drawing drawing : drawings) {

            if (!drawing.isDeleted() && drawing.contains(x, y)) {

                touchedDrawing = drawing;

                isNew = false;

                break;

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

            case TEXT_TOOL:

                touchedDrawing = new TextDrawing(x, y, selectedPaint);

        }

        drawings.add(touchedDrawing);

    }

    // Sets color to draw with
    public void setPaint(int color) {

        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setDither(true);
        selectedPaint.setColor(color);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeJoin(Paint.Join.ROUND);
        selectedPaint.setStrokeCap(Paint.Cap.ROUND);
        selectedPaint.setStrokeWidth(12);

    }

    // Sets the tool (Line, Rect, Oval, Text)
    public void setToolFlag(int toolFlag) {

        if (toolFlag == DRAW_TOOL || toolFlag == RECT_TOOL ||
                toolFlag == OVAL_TOOL || toolFlag == TEXT_TOOL) {

            this.toolFlag = toolFlag;

        } else {

            this.toolFlag = 0;

        }
    }

    // Undoes any drawing actions taken by user
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

    // Redoes any drawing actions taken by user
    public void redo() {

        if (undoneActions.size() > 0) {

            // Get the latest undone action
            Action latestUndoneAction = undoneActions.get(undoneActions.size() - 1);

            // Redo the action
            latestUndoneAction.doAction();

            // Place action in list of done actions
            doneActions.add(latestUndoneAction);

            // Remove from list of undone actions
            undoneActions.remove(latestUndoneAction);

            invalidate();

        }
    }

    // ACTIONS - specific actions that can be taken by user

    private interface Action {

        void undoAction();

        void doAction();
    }

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


    // Allows for scaling of any drawing
    private class DrawingScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float lastSpanX;
        private float lastSpanY;

        float currentSpanX;
        float currentSpanY;

        float shapeMaxHeight;
        float shapeMaxWidth;

        private RectF lastRectF;

        // Arbitrary minimum size
        final float shapeMinSize = 100;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            // Only start scaling if there is a drawing 'selected'
            if (touchedDrawing != null && currentPointerId == firstPointerID) {

                // Get initial values
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

                // Get horizontal and vertical scaling from gesture
                currentSpanX = detector.getCurrentSpanX();
                currentSpanY = detector.getCurrentSpanY();

                // Amount to offset size of bounding RectF by
                float spanXDiff = currentSpanX - lastSpanX;
                float spanYDiff = currentSpanY - lastSpanY;

                // Find out if scaling up or down for each direction
                boolean scalingUpX = spanXDiff > 0;
                boolean scalingUpY = spanYDiff > 0;

                // Upper bounds of possible drawing size
                shapeMaxWidth = mCanvas.getWidth();
                shapeMaxHeight = mCanvas.getHeight();

                // Values to be set and used with drawing.scaleDrawing(float scaleX, float scaleY)
                float scaleX = 0;
                float scaleY = 0;

                // Scaling is ultimately based on this bounding rect for any drawing
                RectF currentRectF = touchedDrawing.getRectF();

                // HORIZONTAL SCALING
                // Don't scale down horizontally if drawing is min width already
                if ((!scalingUpX && (lastRectF.width() >= shapeMinSize)) ||
                        // Don't scale up horizontally if drawing is at max width already
                        (scalingUpX && (lastRectF.width() <= shapeMaxWidth))) {

                    // If this gesture would make drawing larger than max width, set to max width
                    if ((currentRectF.width() + spanXDiff / 2) > shapeMaxWidth) {

                        scaleX = -(currentRectF.width() - shapeMaxWidth);

                    // If this gesture would make drawing smaller than min width, set to min width
                    } else if ((currentRectF.width() + spanXDiff / 2) < shapeMinSize) {

                        scaleX = shapeMinSize - currentRectF.width();

                    // Otherwise, scale according to gesture spanX
                    } else {

                        scaleX =  spanXDiff;
                    }

                }

                // VERTICAL SCALING
                // Don't scale down vertically if drawing is min width already
                if ((!scalingUpY && (lastRectF.height() >= shapeMinSize)) ||
                        // Don't scale up vertically if drawing is at max width already
                        (scalingUpY && (lastRectF.height() <= shapeMaxHeight))) {

                    // If this gesture would make drawing larger than max width, set to max width
                    if ((currentRectF.height() + spanYDiff / 2) > shapeMaxHeight) {

                        scaleY = -(currentRectF.height() - shapeMaxHeight);

                    // If this gesture would make drawing smaller than min width, set to min width
                    } else if ((currentRectF.height() + spanYDiff / 2) < shapeMinSize) {

                        scaleY = shapeMinSize - currentRectF.height();

                    // Otherwise, scale according to gesture spanX
                    } else {

                        scaleY = spanYDiff;

                    }

                }

                // Track previous values
                lastSpanY = currentSpanY;
                lastSpanX = currentSpanX;
                lastRectF = currentRectF;

                // After computing how much to scale by within bounds, scale the current drawing
                touchedDrawing.scaleDrawing(scaleX, scaleY);

            }

            return true;
        }

    }



    /* Listener to communicate when a drawing has
       been dragged into or out of the delete area */

    public void setDeleteFlag(boolean df )
    {
        if (df != deleteFlag)
        {
            deleteFlag = df;
            deleteFlagToggled();
        }
    }

    public interface DeletionListener {

        void onDeleteFlagChange(Boolean deleteFlag);
    }

    private DeletionListener deletionListener;

    public void setDeletionListener(DeletionListener variableChangeListener) {

        this.deletionListener = variableChangeListener;
    }

    private void deleteFlagToggled() {

        if (deletionListener != null)
            deletionListener.onDeleteFlagChange(deleteFlag);
    }



    /* Listener to communicate when any annotation
       is being carried out via touch event */

    public void setAnnotating(boolean a) {
        if (a != annotating) {

            annotating = a;

            annotationToggled();

        }
    }

    public interface OnAnnotationListener {

        void onAnnotation(int toolFlag, boolean annotating, boolean isNewDrawing);

    }

    private OnAnnotationListener onAnnotationListener;

    public void setOnAnnotationListener(OnAnnotationListener onAnnotationListener) {

        this.onAnnotationListener = onAnnotationListener;
    }

    private void annotationToggled() {

        if (onAnnotationListener != null) {

            onAnnotationListener.onAnnotation(toolFlag, annotating, isNewDrawing);

        }

    }

}
