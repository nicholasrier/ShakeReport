package com.plusqa.shake_report;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;


public class ScreenShotMarkUp extends AppCompatActivity {

    private Toolbar mTopToolbar;

    private ImageViewTouchWithDraw screenShotView;

    FloatingActionButton fabCurrentTool, fabDrawTool, fabShapesTool, fabTextTool;
    FloatingActionButton fabRed, fabGreen, fabBlack;
    LinearLayout fabLayoutDraw, fabLayoutShapes, fabLayoutText;
    LinearLayout fabLayoutRed, fabLayoutGreen, fabLayoutBlack;
    View fabBGLayout;
    boolean isFABOpen=false;
    boolean isKeyboardOpen = false;

    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams RLparams;

    private int drawIconID;
    private int squareIconID;
    private int textIconID;
    private int savedIconID;

    public int currentColor;
    private int selectedColor;

    private boolean isDrawSelected = true;
    private boolean isShapesSelected = false;
    private boolean isTextSelected = false;

    private boolean isShapeSaved = false;
    private boolean isDrawSaved  = false;

    private View.OnTouchListener handleTouch;

    private Paint mPaint;
    private static final int green = Color.parseColor("#51ccc0");
    private static final int red = Color.parseColor("#FF5252");
    private static final int black = Color.parseColor("#000000");
    private static final int blue = Color.parseColor("#519ACC");

    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot_mark_up);

        //Set up toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);


        //load screenshot into screenShotImageView
        screenShotView = new ImageViewTouchWithDraw(this.getApplicationContext(), null);

        screenShotView.setId(View.generateViewId());
        LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        screenShotView.setLayoutParams(lp);
        screenShotView.setAdjustViewBounds(true);
        screenShotView.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorTransBg)
        );
        screenShotView.setScaleType(ImageViewTouchWithDraw.ScaleType.FIT_XY);
        screenShotView.setFitsSystemWindows(false);
//adding view to layout
        LinearLayout screenshotLayout = findViewById(R.id.screenshotLayout);
        screenshotLayout.addView(screenShotView);

        Bitmap screenShotBM = Utils.getBitMap(getApplicationContext(), MainActivity.image_name);
        screenShotView.setImageBitmap(screenShotBM);



        Paint currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setDither(true);
        currentPaint.setColor(green);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(20);

        drawIconID = R.drawable.ic_mode_edit_white_24dp;
        squareIconID = R.drawable.ic_outline_brightness_1_24px;
        textIconID = R.drawable.ic_text_fields_white_24dp;
        //FAB tool menu layouts
        fabLayoutDraw = findViewById(R.id.fabLayout1);
        fabLayoutShapes = findViewById(R.id.fabLayout2);
        fabLayoutText = findViewById(R.id.fabLayout3);
        //FAB color menu layouts
        fabLayoutRed = findViewById(R.id.fabColorLayout_red);
        fabLayoutGreen = findViewById(R.id.fabColorLayout_green);
        fabLayoutBlack = findViewById(R.id.fabColorLayout_black);
        //tool FABs
        fabCurrentTool = findViewById(R.id.fab);
        fabDrawTool = findViewById(R.id.fab1);
        fabShapesTool = findViewById(R.id.fab2);
        fabTextTool = findViewById(R.id.fab3);
        //color FABs
        fabRed = findViewById(R.id.fab_red);
        fabGreen = findViewById(R.id.fab_green);
        fabBlack = findViewById(R.id.fab_black);

        currentColor = green;
        selectedColor = green;

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        relativeLayout = findViewById(R.id.ETLayout);

        fabBGLayout = findViewById(R.id.fabBGLayout);

        fabCurrentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        fabBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFABOpen) {
                    closeFABMenu();
                }
            }
        });
        //set initial color
        selectColor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.screenshot_menu, menu);
        return true;
    }
// Done button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_done) {

            //save edited screenshot  --  casting with imageViewTouch exception
//            Bitmap editedBitmap =((BitmapDrawable)screenShotView.getDrawable()).getBitmap();
//            Utils.saveBitmap(this.getApplicationContext(), "EditedScreenShot", editedBitmap);

            //grab log from internal storage
            String logcatString = Utils.readLogFromInternalMemory(this.getApplicationContext()).toString();

            // Post to server
            try {
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String URL = "http://172.16.1.170:3001/v1/logs.json";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("Title", "ShakeReport_log");
                jsonBody.put("Logcat", logcatString);
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";

                        CharSequence text = "";
                        Context context = getApplicationContext();
                        int duration = Toast.LENGTH_SHORT;

                        if (null != response) {
                            responseString = String.valueOf(response.statusCode);
                            if(responseString.equals("200")) {
                                text = "Report Sent!";
                            } else {
                                text = "Report did not send: " + responseString;
                            }
                        } else {
                            text = "Report did not send";
                        }
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                requestQueue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            return true;
            CharSequence text = "Report Sent!";
            int duration = Toast.LENGTH_SHORT;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int color) {
        currentColor = color;
    }

    public boolean isDrawSelected () {
        return isDrawSelected;
    }

    public boolean isTextSelected () {
        return isTextSelected;
    }

    public boolean isShapesSelected () {
        return isShapesSelected;
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabLayoutDraw.setVisibility(View.VISIBLE);
        fabLayoutShapes.setVisibility(View.VISIBLE);
        fabLayoutText.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);
        fabLayoutRed.setVisibility(View.VISIBLE);
        fabLayoutGreen.setVisibility(View.VISIBLE);
        fabLayoutBlack.setVisibility(View.VISIBLE);

        fabCurrentTool.animate().rotationBy(180 - fabCurrentTool.getRotation());
        fabLayoutDraw.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLayoutShapes.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        fabLayoutText.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        fabLayoutRed.animate().translationX(-120);
        fabLayoutGreen.animate().translationX(-240);
        fabLayoutBlack.animate().translationX(-360).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                fabLayoutDraw.setVisibility(View.VISIBLE);
                fabLayoutShapes.setVisibility(View.VISIBLE);
                fabLayoutText.setVisibility(View.VISIBLE);
                fabLayoutRed.setVisibility(View.VISIBLE);
                fabLayoutGreen.setVisibility(View.VISIBLE);
                fabLayoutBlack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void closeFABMenu(){
        isFABOpen=false;


        fabBGLayout.setVisibility(View.GONE);

        fabLayoutRed.animate().translationX(0);
        fabLayoutGreen.animate().translationX(0);
        fabLayoutBlack.animate().translationX(0);
        fabCurrentTool.animate().rotationBy(fabCurrentTool.getRotation()*-1);
        fabLayoutDraw.animate().translationY(0);
        fabLayoutShapes.animate().translationY(0);
        fabLayoutText.animate().translationY(0).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(!isFABOpen){
                    fabLayoutDraw.setVisibility(View.GONE);
                    fabLayoutShapes.setVisibility(View.GONE);
                    fabLayoutText.setVisibility(View.GONE);
                    fabLayoutRed.setVisibility(View.GONE);
                    fabLayoutGreen.setVisibility(View.GONE);
                    fabLayoutBlack.setVisibility(View.GONE);

                    if (!isTextSelected) {
                        deselectView(getCurrentFocus());
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }

    public void tapDraw(View view) {
        if(isFABOpen){
            isDrawSaved = false;
            isShapeSaved = false;
            isDrawSelected = false;
            isTextSelected = false;
            isShapesSelected = false;
            isDrawSelected = true;
            closeFABMenu();
            fabCurrentTool.setImageResource(drawIconID);
        }
    }

    public void tapText(View view) {
        if(isFABOpen) {
            isDrawSelected = false;
            isTextSelected = false;
            isShapesSelected = false;
            isDrawSaved = false;
            isShapeSaved = false;
            isTextSelected = true;

            closeFABMenu();
            fabCurrentTool.setImageResource(textIconID);
        }
    }

    public void tapShapes (View view) {
        if(isFABOpen) {
            isDrawSaved = false;
            isShapeSaved = false;
            isDrawSelected = false;
            isTextSelected = false;
            isShapesSelected = false;
            isShapesSelected = true;
            closeFABMenu();
            fabCurrentTool.setImageResource(squareIconID);

        }
    }

    public void tapRed (View view) {
        //set color of FABs
        selectedColor = red;
        selectColor();
        //set color of actual tool
        screenShotView.setSelectedPaint(red);
    }

    public void tapBlack (View view) {
        //set color of FABs
        selectedColor = black;
        selectColor();
        //set color of actual tool
        screenShotView.setSelectedPaint(black);
    }

    public void tapGreen (View view) {
        //set color of FABs
        selectedColor = green;
        selectColor();
        //set color of actual tool
        screenShotView.setSelectedPaint(green);
    }

    public void selectColor() {
        //implement later -- use four colors - don't display active color in color options

        ObjectAnimator animator = ObjectAnimator.ofInt(fabCurrentTool,
                                                      "backgroundTint",
                                                       currentColor,
                                                       selectedColor);
        animator.setDuration(2000L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int[][] states = new int[][] {
                        new int[] { android.R.attr.state_enabled}, // enabled
                        new int[] {-android.R.attr.state_enabled}, // disabled
                        new int[] {-android.R.attr.state_checked}, // unchecked
                        new int[] { android.R.attr.state_pressed}  // pressed
                };

                int[] colors = new int[] {
                        R.color.color_red,
                        Color.parseColor("#51ccc0"),
                        R.color.color_black
                };

                ColorStateList myList = new ColorStateList(states, colors);
                int animatedValue = (int) animation.getAnimatedValue();
                fabCurrentTool.setBackgroundTintList(ColorStateList.valueOf( animatedValue));
                fabDrawTool.setBackgroundTintList(ColorStateList.valueOf( animatedValue));
                fabShapesTool.setBackgroundTintList(ColorStateList.valueOf( animatedValue));
                fabTextTool.setBackgroundTintList(ColorStateList.valueOf( animatedValue));
            }
        });
        animator.start();

        currentColor = selectedColor;
    }

    public void deselectView(View v) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        v.clearFocus();
    }

    //Spins fab when switching between text and tools
    public void spinSwitch(final boolean hasFocus) {

        float rotation;
        if (hasFocus) //keyboard is opening, spin
            rotation = 360 - fabCurrentTool.getRotation() ;
        else //keyboard closing, un-rotate
            rotation = -fabCurrentTool.getRotation();

        fabCurrentTool.animate().rotationBy(rotation).setListener(new Animator.AnimatorListener() {
            @Override // timing button animation with keyboard movement
            public void onAnimationStart(Animator animation) {
                if (hasFocus) {
                    if (isTextSelected)
                        fabCurrentTool.setImageResource(textIconID);
                } else {
                    if (isDrawSaved)
                        fabCurrentTool.setImageResource(drawIconID);

                    if (isShapeSaved)
                        fabCurrentTool.setImageResource(squareIconID);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!hasFocus) {
                    if (isDrawSaved || isShapeSaved) {
                        deselectView(getCurrentFocus());
                        isDrawSaved = false;
                        isShapeSaved = false;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (!hasFocus) {
            if (isDrawSaved) {
                isShapesSelected = false;
                isTextSelected = false;
                isDrawSelected = true;
            }
            if (isShapeSaved) {
                isDrawSelected = false;
                isTextSelected = false;
                isShapesSelected = true;
            }
        }
    }



    public class ImageViewTouchWithDraw extends android.support.v7.widget.AppCompatImageView {

        private Bitmap  mBitmap;
        private Canvas mCanvas;
        private Path    mPath;
        int firstPointerIndex = -1;
        int firstPointerID = -1;
        Context context;
        private static final int CIRCLES_LIMIT = 10;
        private Paint selectedPaint;
        private CircleArea lastCA;
        private ArrayList<EditText> editTextList = new ArrayList<>(10);

        /** All available circles */
        private HashSet<CircleArea> mCircles = new HashSet<>(CIRCLES_LIMIT);
        private SparseArray<CircleArea> mCirclePointer = new SparseArray<>(CIRCLES_LIMIT);

        public Paint getSelectedPaint() {
            return selectedPaint;
        }

        public void setSelectedPaint(int color) {
            this.selectedPaint = new Paint();
            selectedPaint.setAntiAlias(true);
            selectedPaint.setDither(true);
            selectedPaint.setColor(color);
            selectedPaint.setStyle(Paint.Style.STROKE);
            selectedPaint.setStrokeJoin(Paint.Join.ROUND);
            selectedPaint.setStrokeCap(Paint.Cap.ROUND);
            selectedPaint.setStrokeWidth(12);
        }

        /** Stores data about single circle */
        private class CircleArea {
            float radius;
            float centerX;
            float centerY;
            Paint paint;

            CircleArea(float centerX, float centerY, float radius, Paint paint) {
                this.radius = radius;
                this.centerX = centerX;
                this.centerY = centerY;
                this.paint = paint;
            }

            @Override
            public String toString() {
                return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
            }
        }

        public ImageViewTouchWithDraw(Context c, AttributeSet attrs) {
            super(c, attrs);
            context=c;
            mPath = new Path();
            setSelectedPaint(green);
            mScaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());

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
            //Draw free-draws

            canvas.drawPath(mPath, selectedPaint);

            //Draw circles
            for (CircleArea circle : mCircles) {
                canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, circle.paint);
            }

        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;
        private EditText ET;
        private boolean dontMove = false;
        private ScaleGestureDetector mScaleGestureDetector;
        private float mScaleFactor = 1.0f;
        private float previousScaleFactor = 1.0f;
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            CircleArea touchedCircle;
            final float x = event.getX();
            final float y = event.getY();
            int index = event.getActionIndex();
            int currentPointerId = event.getPointerId(index);
            final View currentView = getCurrentFocus();
            final int action = event.getAction();


            mScaleGestureDetector.onTouchEvent(event);

            switch (action & MotionEvent.ACTION_MASK) {
//                if (isDrawSelected) {
//
//                } else if (isShapesSelected) {
//
//                } else if (isTextSelected) {
//
//                }

                case MotionEvent.ACTION_DOWN:

                    if (isDrawSelected) {
                        if (dontMove) {
                            mPath.reset();
                            mPath.moveTo(x, y);
                            mX = x;
                            mY = y;
                            dontMove = false;
                            deselectView(currentView);
                            invalidate();
                            break;
                        }
                        mPath.reset();
                        mPath.moveTo(x, y);
                        mX = x;
                        mY = y;

                    } else if (isShapesSelected) {
                        mCirclePointer.clear();
                        mX = x;
                        mY = y;
                        // check if we've touched inside some circle
                        touchedCircle = obtainTouchedCircle(x, y);
                        mCirclePointer.put(event.getPointerId(0), touchedCircle);

                    } else if (isTextSelected) {
                        // If in an ET already, clear focus. Else, make a new one.
                        if (currentView instanceof EditText) {

//                            deselectView(currentView);
                            currentView.clearFocus();

                        } else {
                            // make an EditText and put it in a layout
                            ET = new EditText(getApplicationContext());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = (int) x;
                            params.topMargin = (int) y - 60;
                            relativeLayout.addView(ET, params);

                            // set up the EditText
                            ET.requestFocus();
                            ET.setVisibility(VISIBLE);
                            ET.setTextColor(selectedPaint.getColor());
                            ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            ET.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                            ET.setText(" ");
                            ET.setTag(x + " " + y);
                            ET.setOnLongClickListener(new EditText.OnLongClickListener() {

                                @Override
                                public boolean onLongClick(View v) {
//                                    v.startDrag(dragData,  // the data to be dragged
//                                            myShadow,  // the drag shadow builder
//                                            null,      // no need to use local data
//                                            0          // flags (not currently used, set to 0)
//                                    );
                                    return false;
                                }
                            });

                            ET.setOnFocusChangeListener(new OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) { // if opening keyboard
                                        if (!isTextSelected) {
                                            if (isDrawSelected) {
                                                isDrawSelected = false;
                                                isDrawSaved = true;
                                            }
                                            if (isShapesSelected) {
                                                isShapesSelected = false;
                                                isShapeSaved = true;
                                            }
                                            isTextSelected = true;

                                            spinSwitch(true);
                                        }

                                    } else { // if closing keyboard
                                        // Return to previous tool
                                        if (isDrawSaved || isShapeSaved) {
                                            //prevents drawing when deselecting text
                                            dontMove = true;
                                            //animate button and switch icons
                                            spinSwitch(false);

                                        } else {
                                            // Previous tool was text
                                            if (isTextSelected)
                                                deselectView(v);
                                        }

                                    }
                                }
                            });

                            // force open the keyboard
                            if (imm != null) {
                                imm.showSoftInput(ET, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }

                    }

                    invalidate();
                    firstPointerIndex = event.getActionIndex();
                    firstPointerID = event.getPointerId(firstPointerIndex);
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (isDrawSelected) {
                        if (dontMove) {
                            break;
                        }
                        if (currentPointerId == firstPointerID) {
                            float dx = Math.abs(x - mX);
                            float dy = Math.abs(y - mY);
                            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                                mX = x;
                                mY = y;
                            }
                        }

                    } else if (isShapesSelected) {
                        if (currentPointerId == firstPointerID) {
                            final int pointerCount = event.getPointerCount();
//                            for (index = 0; index < pointerCount; index++) {
                            // Some pointer has moved, search it by pointer id
                            currentPointerId = event.getPointerId(index);

                            touchedCircle = mCirclePointer.get(currentPointerId);
                            float centerX = touchedCircle.centerX;
                            float centerY = touchedCircle.centerY;
                            if (null != touchedCircle) {

                                float offsetX = 0;
                                float offsetY = 0;
                                if ((x > mX + 4) || (x < mX - 4))
                                    offsetX = x - mX;

                                if ((y > mY + 4) || (y < mY - 4))
                                    offsetY = y - mY;

                                touchedCircle.centerX = touchedCircle.centerX + offsetX;
                                touchedCircle.centerY = touchedCircle.centerY + offsetY;
                                mX = x;
                                mY = y;
                            }
//                            }
                        }
                    } else if (isTextSelected) {

                    }

                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:

                    if (isDrawSelected) {
                        if (dontMove) {
                            break;
                        }
                        mPath.lineTo(mX, mY);
                        mCanvas.drawPath(mPath, selectedPaint);
                        mPath.reset();

                    } else if (isShapesSelected) {
                        mCirclePointer.clear();
                    } else if (isTextSelected) {

                    }


                    invalidate();
                    break;

                case MotionEvent.ACTION_POINTER_UP:

                    if (isDrawSelected) {
                        if (dontMove) {
                            break;
                        }
                        firstPointerID = -1;
                    } else if (isShapesSelected) {
                        currentPointerId = event.getPointerId(index);

                        mCirclePointer.remove(currentPointerId);
                    } else if (isTextSelected) {

                    }

            }

            return true;
        }

        private CircleArea obtainTouchedCircle(final float xTouch, final float yTouch) {
            CircleArea touchedCircle = getTouchedCircle(xTouch, yTouch);

            if (null == touchedCircle) {
                touchedCircle = new CircleArea(xTouch, yTouch, 200, selectedPaint);

                if (mCircles.size() == CIRCLES_LIMIT) {
                    // remove last circle
                    mCircles.remove(lastCA);
                }

                lastCA = touchedCircle;
                mCircles.add(touchedCircle);
            }

            return touchedCircle;
        }
        private CircleArea getTouchedCircle(final float xTouch, final float yTouch) {
            CircleArea touched = null;

            for (CircleArea circle : mCircles) {
                if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                    touched = circle;
                    break;
                }
            }

            return touched;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector){

                if (isShapesSelected) {
                    CircleArea touchedCircle = getTouchedCircle(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());

                    if (touchedCircle != null) {

//                        mScaleFactor = Math.max(0.8f, Math.min(mScaleFactor, 1.2f));
                        //max and min radius condition
//                        if ((mScaleFactor < (previousScaleFactor - 0.05f)) || (mScaleFactor > (previousScaleFactor + 0.05f)))
                            if ((scaleGestureDetector.getCurrentSpan() <= touchedCircle.radius * 2) &&
                                (mCanvas.getWidth() >= touchedCircle.radius * 2) &&
                                (touchedCircle.radius >= 100)) {
                                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                                touchedCircle.radius *= mScaleFactor;
                            }

                    }
                }
//                previousScaleFactor = mScaleFactor;
                return true;
            }
        }
    }
}




