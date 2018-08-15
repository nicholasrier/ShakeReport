package com.plusqa.shake_report;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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


public class ScreenShotMarkUp extends AppCompatActivity {

    private AnnotationView screenShotView;

    FloatingActionButton fabCurrentTool, fabTool1, fabTool2, fabShapeOption, fabTool3, fabDelete;
    FloatingActionButton colorFAB1, colorFAB2, colorFAB3;
    LinearLayout fabLayoutDraw, fabLayoutShapes, fabLayoutShapeOption,
            fabLayoutText, fabCurrentToolLayout, fabLayoutDelete, screenshotLayout;
    LinearLayout fabLayoutRed, fabLayoutGreen, fabLayoutBlack;
    View fabBGLayout;
    boolean isFABOpen = false;

    RelativeLayout relativeLayout;

    private int drawIconID;
    private int squareIconID;
    private int textIconID;
    private int circlesIconID;
    private int shapeIconID;
    private int currentIconID;

    public int currentColor;
    private int selectedColor;
    private int previousColor;
    private int fab1Color;
    private int fab2Color;
    private int fab3Color;

    private boolean isDrawSelected = true;
    private boolean isShapesSelected = false;
    private boolean isTextSelected = false;

    private boolean isShapeSaved = false;
    private boolean isDrawSaved = false;

    boolean isViewInTrash = false;
    boolean isShapeOptionVisible = false;

    private static final int green = Color.parseColor("#51ccc0");
    private static final int red = Color.parseColor("#FF5252");
    private static final int black = Color.parseColor("#000000");
    private static final int blue = Color.parseColor("#519ACC");

    private InputMethodManager imm;
    private int toolIconID1;
    private int toolIconID2;
    private int toolIconID3;

    int orientation;

//    OptionMenuTouchListener shapeOptionTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot_mark_up);

        //Set up toolbar
        Toolbar mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        //load screenshot into screenShotImageView
        screenShotView = new AnnotationView(ScreenShotMarkUp.this);

        screenShotView.setId(View.generateViewId());
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);

        screenShotView.setLayoutParams(lp);
        screenShotView.setBackgroundColor(ContextCompat.getColor(ScreenShotMarkUp.this, R.color.colorTransBg)
        );
        screenShotView.setFitsSystemWindows(false);

        //adding view to layout
        screenshotLayout = findViewById(R.id.screenshotLayout);
        screenshotLayout.addView(screenShotView);


        Bitmap screenShotBM = Utils.getBitMap(getApplicationContext(), MainActivity.image_name);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), screenShotBM);
        screenShotView.setBackground(bitmapDrawable);
        screenShotView.setToolFlag(AnnotationView.DRAW_TOOL);
        final Paint currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setDither(true);
        currentPaint.setColor(green);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(20);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        if (screenShotBM.getWidth() > dm.widthPixels) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (screenShotBM.getHeight() > dm.widthPixels) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        drawIconID = R.drawable.ic_brush_black_24dp;
        squareIconID = R.drawable.ic_sharp_crop_square_white_24px;
        shapeIconID = squareIconID;
        circlesIconID = R.drawable.ic_sharp_bubble_chart_24px;
        textIconID = R.drawable.ic_text_fields_white_24dp;
        currentIconID = drawIconID;
        toolIconID1 = circlesIconID;
        toolIconID2 = squareIconID;
        toolIconID3 = textIconID;

        //FAB tool menu layouts
        fabLayoutDraw = findViewById(R.id.fabLayout1);
        fabLayoutShapes = findViewById(R.id.fabLayout2);
        fabLayoutShapeOption = findViewById(R.id.fabShapesLayout_option);
        fabLayoutText = findViewById(R.id.fabLayout3);
        fabCurrentToolLayout = findViewById(R.id.fabCurrentToolLayout);
        fabLayoutDelete = findViewById(R.id.fabDeleteLayout);

        //FAB color menu layouts
        fabLayoutRed = findViewById(R.id.fabColorLayout_red);
        fabLayoutGreen = findViewById(R.id.fabColorLayout_green);
        fabLayoutBlack = findViewById(R.id.fabColorLayout_black);
        //tool FABs
        fabCurrentTool = findViewById(R.id.fab);
        fabTool1 = findViewById(R.id.fab1);
        fabTool2 = findViewById(R.id.fab2);
        fabShapeOption = findViewById(R.id.fab_shapeOption);
        fabTool3 = findViewById(R.id.fab3);
        fabDelete = findViewById(R.id.fabDelete);
        //color FABs
        colorFAB1 = findViewById(R.id.fab_red);
        colorFAB2 = findViewById(R.id.fab_green);
        colorFAB3 = findViewById(R.id.fab_black);

        currentColor = green;
        selectedColor = green;
        previousColor = green;
        fab1Color = red;
        fab2Color = blue;
        fab3Color = black;

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        relativeLayout = findViewById(R.id.ETLayout);

        fabBGLayout = findViewById(R.id.fabBGLayout);

        fabDelete.hide();

        // Listen for shape hovering over delete tool to animate trash can FAB
        screenShotView.setDeletionListener(new AnnotationView.DeletionListener() {

            @Override
            public void onDeleteFlagChange(Boolean deleteFlag) {

                if (deleteFlag) {

                    fabDelete.setScaleX(1.3f);
                    fabDelete.setScaleY(1.3f);

                } else {

                    fabDelete.setScaleX(1);
                    fabDelete.setScaleY(1);

                }

            }
        });


        // Listen to when user is editing screenshot, so that UI can be hidden/shown appropriately
        screenShotView.setOnAnnotationListener(new AnnotationView.OnAnnotationListener() {
            @Override
            public void onAnnotation(int toolFlag, boolean annotating) {
                if (annotating) {

                    fabCurrentTool.hide();

                    if (toolFlag != AnnotationView.DRAW_TOOL) {

                        fabDelete.show();
                    }

                } else {

                    fabCurrentTool.show();
                    fabDelete.hide();

                }
            }
        });

        fabCurrentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        fabBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFABOpen) {
                    if (!isShapeOptionVisible) {
                        closeFABMenu();
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.screenshot_menu, menu);
        return true;
    }

    // Done button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_undo:

                screenShotView.undo();

                return true;

            case R.id.action_redo:

                screenShotView.redo();

                return true;

            case R.id.action_done:
                Intent intent = new Intent(ScreenShotMarkUp.this, FormatAndSend.class);
                startActivity(intent);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private boolean sendReport() {
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
                        if (responseString.equals("200")) {
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

        CharSequence text = "Report Sent!";
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        return true;
    }

    private void showFABMenu() {
        isFABOpen = true;

        fabCurrentTool.animate().rotationBy(180 - fabCurrentTool.getRotation());
        fabLayoutDraw.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLayoutShapes.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        fabLayoutText.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        fabLayoutRed.animate().translationX(-120);
        fabLayoutGreen.animate().translationX(-240);
        fabLayoutBlack.animate().translationX(-360).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                fabBGLayout.setVisibility(View.VISIBLE);
                fabLayoutDraw.setVisibility(View.VISIBLE);
                fabLayoutShapes.setVisibility(View.VISIBLE);
                fabLayoutText.setVisibility(View.VISIBLE);
                fabLayoutRed.setVisibility(View.VISIBLE);
                fabLayoutGreen.setVisibility(View.VISIBLE);
                fabLayoutBlack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fabShapeOption.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void closeFABMenu() {
        isFABOpen = false;

        fabLayoutRed.animate().translationX(0);
        fabLayoutGreen.animate().translationX(0);
        fabLayoutBlack.animate().translationX(0);
        fabCurrentTool.animate().rotationBy(fabCurrentTool.getRotation() * -1);
        fabLayoutDraw.animate().translationY(0);
        fabLayoutShapes.animate().translationY(0);
        fabLayoutText.animate().translationY(0).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                fabShapeOption.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fabLayoutDraw.setVisibility(View.GONE);
                    fabLayoutShapes.setVisibility(View.GONE);
                    fabLayoutText.setVisibility(View.GONE);
                    fabLayoutRed.setVisibility(View.GONE);
                    fabLayoutGreen.setVisibility(View.GONE);
                    fabLayoutBlack.setVisibility(View.GONE);
                    fabBGLayout.setVisibility(View.GONE);
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
        if (isFABOpen) {
            closeFABMenu();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void tapTool(View v) {
        if (isFABOpen) {
            int iconID;

            if (v == fabTool1) {
                iconID = toolIconID1;
                fabTool1.setImageResource(currentIconID);
                toolIconID1 = currentIconID;
            } else if (v == fabTool2) {
                iconID = toolIconID2;
                fabTool2.setImageResource(currentIconID);
                toolIconID2 = currentIconID;
            } else {
                iconID = toolIconID3;
                fabTool3.setImageResource(currentIconID);
                toolIconID3 = currentIconID;
            }

            currentIconID = iconID;

            isDrawSaved = false;
            isShapeSaved = false;
            isDrawSelected = currentIconID == drawIconID;
            isTextSelected = currentIconID == textIconID;

            isShapesSelected = (currentIconID == squareIconID) || (currentIconID == circlesIconID);

            if (isShapesSelected)
                shapeIconID = (currentIconID == squareIconID) ? squareIconID : circlesIconID;

            closeFABMenu();

            fabCurrentTool.setImageResource(currentIconID);

            if (isDrawSelected) {
                screenShotView.setToolFlag(AnnotationView.DRAW_TOOL);
            } else if (isShapesSelected) {
                if (shapeIconID == squareIconID) {
                    screenShotView.setToolFlag(AnnotationView.RECT_TOOL);
                } else {
                    screenShotView.setToolFlag(AnnotationView.OVAL_TOOL);
                }
            } else {
                screenShotView.setToolFlag(AnnotationView.TEXT_TOOL);
            }
        }
    }


    public void tapColor1(View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab1Color;

        selectColor(view);
        fab1Color = previousColor;
        //set color of actual tool
        screenShotView.setPaint(selectedColor);
    }

    public void tapColor2(View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab2Color;

        selectColor(view);
        fab2Color = previousColor;
        //set color of actual tool
        screenShotView.setPaint(selectedColor);
    }

    public void tapColor3(View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab3Color;

        selectColor(view);
        fab3Color = previousColor;
        //set color of actual tool
        screenShotView.setPaint(selectedColor);
    }

    public void selectColor(final View view) {
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
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_enabled}, // enabled
                        new int[]{-android.R.attr.state_enabled}, // disabled
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_pressed}  // pressed
                };

                int[] colors = new int[]{
                        red,
                        green,
                        black,
                        blue
                };

                ColorStateList myList = new ColorStateList(states, colors);
                int animatedValue = (int) animation.getAnimatedValue();
                fabCurrentTool.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool1.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool2.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabShapeOption.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool3.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                if (selectedColor == fab1Color)
                    colorFAB1.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                else if (selectedColor == fab2Color) {
                    colorFAB2.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                } else if (selectedColor == fab3Color) {
                    colorFAB3.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                }
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
            rotation = 360 - fabCurrentTool.getRotation();
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
                        fabCurrentTool.setImageResource(shapeIconID);
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
                screenShotView.setToolFlag(AnnotationView.DRAW_TOOL);
            }
            if (isShapeSaved) {
                isDrawSelected = false;
                isTextSelected = false;
                isShapesSelected = true;
            }
        }
    }


    public class OptionMenuTouchListener implements View.OnTouchListener {
        boolean doNothing = false;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            final int action = event.getAction();
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            if (isFABOpen) {
                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        isShapeOptionVisible = true;
                        fabLayoutShapeOption.animate().translationX(-130).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator a) {
                                fabLayoutShapeOption.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator a) {
                                if (isShapeOptionVisible) {
                                    fabLayoutShapeOption.setVisibility(View.VISIBLE);
                                } else {
                                    fabLayoutShapeOption.setVisibility(View.GONE);
                                    if (!doNothing) {
                                        tapTool(v);
                                    }

                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator a) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator a) {
                            }
                        });
                        break;

                    case MotionEvent.ACTION_UP:
                        isShapeOptionVisible = false;
                        fabLayoutShapeOption.animate().translationX(0);
                        boolean touchingShapeTool = Utils.isViewInBounds(v, x, y);
                        boolean touchingOption = Utils.isViewInBounds(fabLayoutShapeOption, x, y);

                        if (!touchingShapeTool && touchingOption) {

                            fabShapeOption.setImageResource(shapeIconID);
                            shapeIconID = (shapeIconID == squareIconID) ? circlesIconID : squareIconID;
                            if (v == fabTool1) {
                                fabTool1.setImageResource(shapeIconID);
                                toolIconID1 = shapeIconID;
                            } else if (v == fabTool2) {
                                fabTool2.setImageResource(shapeIconID);
                                toolIconID2 = shapeIconID;
                            } else {
                                fabTool3.setImageResource(shapeIconID);
                                toolIconID3 = shapeIconID;
                            }
                        }
                        doNothing = !touchingShapeTool;
                        break;
                }
            }
            return true;
        }
    }




//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//
//            final float x = event.getX();
//            final float y = event.getY();
//            int index = event.getActionIndex();
//            int currentPointerId = event.getPointerId(index);
//            currentView = getCurrentFocus();
//            final int action = event.getAction();
//            mScaleGestureDetector.onTouchEvent(event);
//
//            switch (action & MotionEvent.ACTION_MASK) {
//
//                case MotionEvent.ACTION_DOWN:
//                    //accessibility requirement
//                    performClick();
//
//                    fabCurrentTool.hide();
//
//                    //old stuff -- subject to removal
//                    if (isDrawSelected) {
//                        if (dontMove) {
//                            mPath.reset();
//                            mPath.moveTo(x, y);
//                            mX = x;
//                            mY = y;
//                            dontMove = false;
//                            deselectView(currentView);
//                            invalidate();
//                            break;
//                        }
//                    }
//
//                    mX = x;
//                    mY = y;
//
//                    // check if we've touched inside some drawing
////                    if (isTextSelected && (currentView instanceof EditText)) {
////                        // If in an ET already, clear focus. Else, make a new one.
////                        currentView.clearFocus();
////                    } else {}
//
//                    touchedDrawing = getTouchedDrawing(x, y);
//
//
//                    if (!isNewLine && !(currentView instanceof EditText)) {
//                        fabDelete.show();
//                    }
//
//                    invalidate();
//                    firstPointerIndex = event.getActionIndex();
//                    firstPointerID = event.getPointerId(firstPointerIndex);
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                    if (touchedDrawing == null) break;
//
//                    if (currentPointerId == firstPointerID) {
//
//                        // if we are drawing a line, rather than moving an existing drawing
//                        if (isNewLine) {
//                            if (dontMove) {
//                                break;
//                            }
//
//                            float dx = Math.abs(x - mX);
//                            float dy = Math.abs(y - mY);
//
//                            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//                                PointF p = new PointF(x, y);
//                                touchedDrawing.points.add(p);
//                                touchedDrawing.drawTo(x, y, mX, mY);
//                                mX = x;
//                                mY = y;
//                            }
//                        } else { // if it's an existing drawing
//
//                            touchedDrawing.offsetDrawing(x - mX, y - mY);
//
//                            trashcanRect = Utils.getViewRect(fabLayoutDelete);
//
//                            if (trashcanRect != null) {
//                                if (trashcanRect.contains((int) event.getRawX(), (int) event.getRawY())) {
//                                    if (!isDeleteFlag) {
//                                        fabDelete.setScaleX(1.3f);
//                                        fabDelete.setScaleY(1.3f);
//                                        isDeleteFlag = true;
//                                    }
//                                } else {
//                                    if (isDeleteFlag) {
//
//                                        fabDelete.setScaleX(1);
//                                        fabDelete.setScaleY(1);
//                                        isDeleteFlag = false;
//                                    }
//                                }
//                            }
//
//                            mX = x;
//                            mY = y;
//
//                        }
//                    }
//
//                    invalidate();
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    fabCurrentTool.show();
//                    fabDelete.hide();
//
//                    if (touchedDrawing != null) {
//                        if (isDeleteFlag) { // flag drawing for deletion and save action
//
//                            // Revert adjustments when deleting
//                            touchedDrawing.path = editAction.path;
//                            touchedDrawing.rectF = editAction.rectF;
//                            touchedDrawing.paint = editAction.paint;
//                            touchedDrawing.points = editAction.points;
//
//                            // Mark the drawing to be deleted so that it's ignored in onDraw()
//                            touchedDrawing.deleted = true;
//
//                            // Designate that new action is delete
//                            editAction.setEdit(DrawAction.IS_DELETE);
//
//                            // For ease of use in undo/redo
//                            editAction.editedAction = touchedDrawing;
//
//                            // Add the delete action to the list of done actions
//                            actionsList.add(editAction);
//
//
//                        } else if (!isNewDrawing) { // save adjust action
//
//                            // Designate that new action is adjustment
//                            editAction.setEdit(DrawAction.IS_ADJUST);
//
//                            // Save the edited object in the adjust action
//                            editAction.editedAction = touchedDrawing;
//
//                            // Add the adjust action to the list of done actions
//                            actionsList.add(editAction);
//
//                            // If any action is taken, clear redo list
//                            redoList.clear();
//
//                        }
//
//                        isNewDrawing = false;
//                        isDeleteFlag = false;
//                    }
//
//                    editAction = null;
//
//                    invalidate();
//                    break;
//
//                case MotionEvent.ACTION_POINTER_UP:
//
//                    if (touchedDrawing != null && touchedDrawing.isLine()) {
//                        if (dontMove) {
//                            break;
//                        }
//
//                        fabCurrentTool.show();
//                        fabDelete.hide();
//                        firstPointerID = -1;
//                    }
//                    invalidate();
//                    break;
//
//            }
//
//            return true;
//        }


//        private void placeEditText(EditText et, int x, int y) {
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.leftMargin = x;
//            params.topMargin = y - 60;
//            relativeLayout.addView(et, params);
//        }
//
//        @SuppressLint("ClickableViewAccessibility")
//        private void setupEditText(EditText et, Paint paint, Editable text) {
//
//            // set up the EditText
//
//            if (isNewDrawing) {
//                et.requestFocus();
//                et.setText(" ");
//            } else {
//                et.setText(text);
//            }
//
//            et.setVisibility(VISIBLE);
//            et.setTextColor(paint.getColor());
//            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//            et.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//            et.setDrawingCacheEnabled(true);
//
//            //Handles the keyboard opening and closing while editing image
//            et.setOnFocusChangeListener(new OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) { // if opening keyboard
//                        if (!isTextSelected) {
//                            if (isDrawSelected) {
//                                isDrawSelected = false;
//                                isDrawSaved = true;
//                            }
//                            if (isShapesSelected) {
//                                isShapesSelected = false;
//                                isShapeSaved = true;
//                            }
//                            isTextSelected = true;
//
//                            spinSwitch(true);
//                        }
//
//                    } else { // if closing keyboard
//                        // Return to previous tool
//                        if (isDrawSaved || isShapeSaved) {
//                            //prevents drawing when deselecting text
//                            dontMove = true;
//                            //animate button and switch icons
//                            spinSwitch(false);
//
//                        } else {
//                            // Previous tool was text
//                            if (isTextSelected)
//                                deselectView(v);
//                        }
//                    }
//                }
//            });
//
//            //Initiates text dragging after a threshold is breached
//            et.setOnTouchListener(new View.OnTouchListener() {
//                float x;
//                float y;
//                float prevX;
//                float prevY;
//
//                @Override
//                public boolean onTouch(View v, MotionEvent e) {
//                    x = e.getX();
//                    y = e.getY();
//                    switch (e.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//
//                            prevX = x;
//                            prevY = y;
//
//                            for (DrawAction drawAction : actionsList) {
//                                if (!drawAction.isEdit() && drawAction.isText() &&
//                                        drawAction.editText == v) {
//
//                                    touchedDrawing = drawAction;
//
//                                    isNewLine = false;
//                                    isNewDrawing = false;
//
//                                    Path editPath = new Path(drawAction.path);
//                                    Paint editPaint = new Paint(drawAction.paint);
//                                    RectF editRectF = new RectF(drawAction.rectF);
//
//                                    EditText et = new EditText(ScreenShotMarkUp.this);
//                                    setupEditText(et, drawAction.paint, drawAction.editText.getText());
//
//                                    editAction = new DrawAction(editPath, editPaint, editRectF, DrawAction.IS_TEXT);
//                                    editAction.editText = et;
//                                }
//                            }
//
//                            return false;
//
//                        case MotionEvent.ACTION_UP:
//                            return false;
//
//                        case MotionEvent.ACTION_MOVE:
//                            if (Math.abs(x - prevX) > 10 || Math.abs(y - prevY) > 10) {
//
//                                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//                                v.startDrag(null, shadowBuilder, v, 0);
//                                return true;
//                            }
//                    }
//
//
//                    return false;
//                }
//            });
//
//            // allows user to drop text on screen
//            // listens for when the dragged text leaves the delete bounds
//            relativeLayout.setOnDragListener(new View.OnDragListener() {
//                @Override
//                public boolean onDrag(View mainView, DragEvent e) {
//                    View view = (View) e.getLocalState();
//
//                    switch (e.getAction()) {
//
//                        case DragEvent.ACTION_DROP:
//
//                            view.setX(e.getX() - (view.getWidth() / 2));
//                            view.setY(e.getY() - (view.getHeight() / 2));
//
//                            if (editAction != null && touchedDrawing != null) {
//
//                                editAction.setEdit(DrawAction.IS_ADJUST);
//
//                                editAction.editedAction = touchedDrawing;
//
//                                actionsList.add(editAction);
//
//                                redoList.clear();
//                            }
//
//                            view.invalidate();
//                            mainView.invalidate();
//                            return true;
//
//                        case DragEvent.ACTION_DRAG_STARTED:
//                            fabDelete.show();
//                            return true;
//
//                        case DragEvent.ACTION_DRAG_LOCATION:
//                            Point touchPos = Utils.getTouchPositionFromDragEvent(mainView, e);
//                            if (Utils.isTouchInsideOfView(mainView, touchPos)) {
//                                if (isViewInTrash) {
//                                    fabDelete.setScaleX(1);
//                                    fabDelete.setScaleY(1);
//                                    isViewInTrash = false;
//                                }
//                            }
//                            return false;
//                        case DragEvent.ACTION_DRAG_EXITED:
//                            break;
//
//                        case DragEvent.ACTION_DRAG_ENDED:
//                            mainView.invalidate();
//                            return true;
//
//                        default:
//                            break;
//                    }
//
//                    return true;
//                }
//            });
//
//            //hides delete fab when text finishes drag
//            et.setOnDragListener(new View.OnDragListener() {
//                @Override
//                public boolean onDrag(View v, DragEvent e) {
//                    switch (e.getAction()) {
//                        case DragEvent.ACTION_DROP:
//                            break;
//                        case DragEvent.ACTION_DRAG_STARTED:
//                            break;
//                        case DragEvent.ACTION_DRAG_LOCATION:
//                            return false;
//                        case DragEvent.ACTION_DRAG_EXITED:
//                            break;
//                        case DragEvent.ACTION_DRAG_ENDED:
//                            fabDelete.hide();
//                            break;
//                        default:
//                            break;
//                    }
//
//                    return true;
//                }
//            });
//
//            //Deletes editText when dropped into delete layout
//            //Detects if text is dragged into delete layout
//            fabLayoutDelete.setOnDragListener(new View.OnDragListener() {
//                @Override
//                public boolean onDrag(View mainView, DragEvent e) {
//                    View view = (View) e.getLocalState();
//                    switch (e.getAction()) {
//                        case DragEvent.ACTION_DROP:
//                            if (isViewInTrash) {
//
//                                if (editAction != null && touchedDrawing != null && !isNewDrawing) {
//
//                                    touchedDrawing.path = editAction.path;
//                                    touchedDrawing.rectF = editAction.rectF;
//                                    touchedDrawing.paint = editAction.paint;
//                                    touchedDrawing.editText = editAction.editText;
//
//                                    editAction.setEdit(0);
//                                    editAction.setEdit(DrawAction.IS_DELETE);
//
//                                    editAction.editedAction = touchedDrawing;
//
//                                    touchedDrawing.deleted = true;
//
//                                    actionsList.add(editAction);
//                                }
//
//
//                                relativeLayout.removeView(view);
//
//                                fabDelete.hide();
//                            }
//                            break;
//                        case DragEvent.ACTION_DRAG_STARTED:
//                            break;
//                        case DragEvent.ACTION_DRAG_LOCATION:
//                            Point touchPos = Utils.getTouchPositionFromDragEvent(mainView, e);
//                            if (Utils.isTouchInsideOfView(mainView, touchPos)) {
//                                if (!isViewInTrash) {
//                                    fabDelete.setScaleX(1.3f);
//                                    fabDelete.setScaleY(1.3f);
//                                    isViewInTrash = true;
//                                }
//                            }
//                            break;
//                        case DragEvent.ACTION_DRAG_EXITED:
//                            break;
//                        case DragEvent.ACTION_DRAG_ENDED:
//                            break;
//                        default:
//                            break;
//                    }
//                    return true;
//                }
//            });
//
//            if (isNewDrawing) {
//                // force open the keyboard
//                if (imm != null) {
//                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
//                }
//            }
//
//        }
//
//    }
}




